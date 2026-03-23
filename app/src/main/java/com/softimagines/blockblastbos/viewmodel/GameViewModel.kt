package com.softimagines.blockblastbos.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.softimagines.blockblastbos.R
import com.softimagines.blockblastbos.data.datastore.HighScoreDataStore
import com.softimagines.blockblastbos.data.model.*
import com.softimagines.blockblastbos.ui.theme.GameTheme
import com.softimagines.blockblastbos.ui.theme.ThemeProvider
import com.softimagines.blockblastbos.util.ShapeGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Stack
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = HighScoreDataStore(application)
    private val context = application.applicationContext

    private var soundPool: SoundPool? = null
    private var soundPlace: Int = 0
    private var soundClear: Int = 0
    private var soundCombo: Int = 0
    private var soundGameOver: Int = 0

    var currentGameMode by mutableStateOf(GameMode.CLASSIC)
        private set
    var timeLeft by mutableIntStateOf(180)
    private var timerJob: Job? = null

    val coins: StateFlow<Int> = dataStore.coinsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    var showDailyRewardDialog by mutableStateOf(false)
        private set

    var currentLevelIndex by mutableIntStateOf(0)
    val currentLevelData get() = LevelProvider.levels[currentLevelIndex]
    var isLevelComplete by mutableStateOf(false)

    var gridState by mutableStateOf(GridState())
    var currentScore by mutableIntStateOf(0)
    val highScore: StateFlow<Int> = dataStore.highScoreFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    var availableShapes = mutableStateListOf<GameShape>().apply { addAll(ShapeGenerator.getThreeRandomShapes()) }
    
    var isGameOver by mutableStateOf(false)
    var canRevive by mutableStateOf(true)
    var isGameOverAnimPlaying by mutableStateOf(false)
    var bossHealth by mutableIntStateOf(100)

    private val historyStack = Stack<GameHistory>()
    var canUndo by mutableStateOf(false)
    var comboCount by mutableIntStateOf(0)

    val REVIVE_COST = 500
    val POWERUP_COST = 200

    var draggingShape by mutableStateOf<GameShape?>(null)
    var dragPreviewPosition by mutableStateOf<Position?>(null)
    var currentTheme by mutableStateOf(ThemeProvider.NeonCyber)
    private val _particles = mutableStateListOf<Particle>()
    val particles: List<Particle> get() = _particles

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    init {
        checkDailyReward()
        initSoundPool()
    }

    private fun checkDailyReward() {
        viewModelScope.launch {
            val lastDate = dataStore.lastRewardDateFlow.first()
            if (dataStore.isDailyRewardAvailable(lastDate)) {
                showDailyRewardDialog = true
            }
        }
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        soundPool = SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build()
        soundPlace = soundPool?.load(context, R.raw.place, 1) ?: 0
        soundClear = soundPool?.load(context, R.raw.clear, 1) ?: 0
        soundCombo = soundPool?.load(context, R.raw.combo, 1) ?: 0
        soundGameOver = soundPool?.load(context, R.raw.game_over, 1) ?: 0
    }

    private fun playSound(soundId: Int) { if (soundId != 0) soundPool?.play(soundId, 1f, 1f, 0, 0, 1f) }

    fun claimDailyReward() {
        viewModelScope.launch {
            dataStore.addCoins(100); dataStore.updateLastRewardDate(); showDailyRewardDialog = false; vibrate(100)
        }
    }

    fun usePowerUp(type: PowerUpType) {
        viewModelScope.launch {
            if (dataStore.spendCoins(POWERUP_COST)) {
                when (type) {
                    PowerUpType.ROTATE -> rotateAvailableShapes()
                    PowerUpType.EXTRA_BLOCK -> { availableShapes.clear(); availableShapes.addAll(ShapeGenerator.getThreeRandomShapes()) }
                    else -> {}
                }; vibrate(60)
            }
        }
    }

    fun undo() {
        if (historyStack.isNotEmpty()) {
            val lastState = historyStack.pop()
            gridState = lastState.gridState; currentScore = lastState.score
            availableShapes.clear(); availableShapes.addAll(lastState.availableShapes)
            comboCount = lastState.comboCount; canUndo = historyStack.isNotEmpty(); vibrate(30)
        }
    }

    fun reviveWithCoins() {
        viewModelScope.launch { if (dataStore.spendCoins(REVIVE_COST)) revive() }
    }

    fun revive() {
        isGameOver = false; canRevive = false
        val start = (gridState.size / 2) - 2; val newCells = gridState.cells.map { it.toMutableList() }
        for (r in start until start + 4) for (c in start until start + 4) newCells[r][c] = Cell()
        gridState = gridState.copy(cells = newCells.map { it.toList() })
        availableShapes.clear(); availableShapes.addAll(ShapeGenerator.getThreeRandomShapes())
        if (currentGameMode == GameMode.TIME_ATTACK) { timeLeft += 60; startTimer() }
        vibrate(200)
    }

    fun nextLevel() {
        if (currentLevelIndex < LevelProvider.levels.size - 1) {
            currentLevelIndex++
            loadLevel(currentLevelIndex)
        } else {
            setGameMode(GameMode.CLASSIC)
        }
    }

    fun onBlockPlaced(shape: GameShape, position: Position): Boolean {
        // Cari indeks berdasarkan ID unik objek balok
        val actualIndex = availableShapes.indexOfFirst { it.id == shape.id }
        if (actualIndex == -1) return false
        
        if (canPlaceShape(shape, position)) {
            saveToHistory(); placeShape(shape, position); vibrate(40); updateScore(shape.blocks.size); playSound(soundPlace)
            val clearedCount = checkAndClearLines()
            if (clearedCount > 0) {
                comboCount++; playSound(if (comboCount > 1) soundCombo else soundClear)
                viewModelScope.launch { dataStore.addCoins(clearedCount * 10) }
                if (currentGameMode == GameMode.BOSS_BATTLE) bossHealth -= (clearedCount * 10)
            } else comboCount = 0
            if (currentGameMode == GameMode.PUZZLE && currentScore >= currentLevelData.targetScore) { isLevelComplete = true; vibrate(300) }
            
            // Hapus balok menggunakan indeks asli yang baru ditemukan
            availableShapes.removeAt(actualIndex)
            
            if (availableShapes.isEmpty()) availableShapes.addAll(ShapeGenerator.getThreeRandomShapes())
            if (isGameFinished() && !isLevelComplete) { if (currentGameMode == GameMode.ZEN) clearRandomArea() else triggerGameOverAnimation() }
            if (currentGameMode == GameMode.BOSS_BATTLE && !isGameOver) bossMove()
            return true
        }
        return false
    }

    private fun triggerGameOverAnimation() {
        if (isGameOverAnimPlaying) return
        isGameOverAnimPlaying = true; playSound(soundGameOver)
        viewModelScope.launch {
            val size = gridState.size
            for (r in size - 1 downTo 0) {
                val newCells = gridState.cells.map { it.toMutableList() }
                for (c in 0 until size) newCells[r][c] = Cell(true, Color.DarkGray)
                gridState = gridState.copy(cells = newCells.map { it.toList() })
                vibrate(20); delay(100)
            }
            delay(500); isGameOver = true; isGameOverAnimPlaying = false; dataStore.saveHighScore(currentScore)
        }
    }

    fun setGameMode(mode: GameMode) {
        currentGameMode = mode
        if (mode == GameMode.PUZZLE) { currentLevelIndex = 0; loadLevel(0) } else resetGame()
        if (mode == GameMode.TIME_ATTACK) startTimer()
    }

    private fun loadLevel(index: Int) {
        val level = LevelProvider.levels[index]
        gridState = GridState()
        val newCells = gridState.cells.map { it.toMutableList() }
        level.initialGrid.forEach { pos -> newCells[pos.row][pos.col] = Cell(true, Color.Gray) }
        gridState = gridState.copy(cells = newCells.map { it.toList() })
        currentScore = 0; isLevelComplete = false; isGameOver = false; canRevive = true; comboCount = 0; historyStack.clear(); canUndo = false; availableShapes.clear(); availableShapes.addAll(ShapeGenerator.getThreeRandomShapes()); vibrate(50)
    }

    private fun startTimer() {
        timerJob?.cancel(); timeLeft = 180
        timerJob = viewModelScope.launch {
            while (timeLeft > 0 && !isGameOver) { delay(1000); timeLeft-- }
            if (timeLeft <= 0) triggerGameOverAnimation()
        }
    }

    private fun bossMove() {
        viewModelScope.launch {
            delay(500); val r = Random.nextInt(gridState.size); val c = Random.nextInt(gridState.size)
            if (!gridState.cells[r][c].isOccupied) {
                val newCells = gridState.cells.map { it.toMutableList() }; newCells[r][c] = Cell(true, Color.Gray); gridState = gridState.copy(cells = newCells.map { it.toList() }); vibrate(20)
            }
        }
    }

    private fun saveToHistory() {
        historyStack.push(GameHistory(gridState, currentScore, availableShapes.toList(), comboCount))
        if (historyStack.size > 5) historyStack.removeAt(0); canUndo = true
    }

    private fun rotateAvailableShapes() {
        val rotatedShapes = availableShapes.map { shape ->
            val rotated = shape.blocks.map { Position(it.col, -it.row) }
            val minR = rotated.minOf { it.row }; val minC = rotated.minOf { it.col }
            GameShape(rotated.map { Position(it.row - minR, it.col - minC) }, shape.color)
        }
        availableShapes.clear(); availableShapes.addAll(rotatedShapes)
    }

    private fun checkAndClearLines(): Int {
        val cells = gridState.cells; val rows = (0 until gridState.size).filter { r -> cells[r].all { it.isOccupied } }; val cols = (0 until gridState.size).filter { c -> (0 until gridState.size).all { r -> cells[r][c].isOccupied } }
        if (rows.isNotEmpty() || cols.isNotEmpty()) {
            vibrate(100); triggerLineClearParticles(rows, cols); val nC = gridState.cells.map { it.toMutableList() }
            rows.forEach { r -> (0 until gridState.size).forEach { c -> nC[r][c] = Cell() } }; cols.forEach { c -> (0 until gridState.size).forEach { r -> nC[r][c] = Cell() } }
            gridState = gridState.copy(cells = nC.map { it.toList() }); val t = rows.size + cols.size; updateScore(t * 50); return t
        }
        return 0
    }

    private fun canPlaceShape(s: GameShape, p: Position): Boolean = s.blocks.all { b -> val r = p.row + b.row; val c = p.col + b.col; r in 0 until gridState.size && c in 0 until gridState.size && !gridState.cells[r][c].isOccupied }
    private fun placeShape(s: GameShape, p: Position) { val nC = gridState.cells.map { it.toMutableList() }; s.blocks.forEach { b -> nC[p.row + b.row][p.col + b.col] = Cell(true, s.color) }; gridState = gridState.copy(cells = nC.map { it.toList() }) }
    private fun triggerLineClearParticles(rows: List<Int>, cols: List<Int>) { viewModelScope.launch { val cs = 100f; rows.forEach { r -> repeat(20) { _particles.add(createParticle(r * cs, Random.nextFloat() * 8 * cs)) } }; cols.forEach { c -> repeat(20) { _particles.add(createParticle(Random.nextFloat() * 8 * cs, c * cs)) } }; delay(1000); _particles.clear() } }
    private fun createParticle(x: Float, y: Float) = Particle(System.nanoTime() + Random.nextLong(), Offset(y, x), Offset(Random.nextFloat() * 14 - 7, Random.nextFloat() * 14 - 7), currentTheme.particleColor, 1f, Random.nextFloat() * 15 + 5)
    fun updateDragPreview(s: GameShape?, p: Position?) { draggingShape = s; dragPreviewPosition = if (s != null && p != null && canPlaceShape(s, p)) p else null }
    private fun updateScore(p: Int) { currentScore += p }
    private fun isGameFinished(): Boolean = availableShapes.none { s -> (0 until gridState.size).any { r -> (0 until gridState.size).any { c -> canPlaceShape(s, Position(r, c)) } } }
    fun resetGame() { gridState = GridState(); currentScore = 0; availableShapes.clear(); availableShapes.addAll(ShapeGenerator.getThreeRandomShapes()); isGameOver = false; comboCount = 0; historyStack.clear(); canUndo = false; bossHealth = 100; canRevive = true; if (currentGameMode == GameMode.TIME_ATTACK) startTimer(); vibrate(30) }
    fun changeTheme(t: GameTheme) { currentTheme = t }
    private fun vibrate(s: Long) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(s, VibrationEffect.DEFAULT_AMPLITUDE)) else vibrator.vibrate(s) }
    private fun clearRandomArea() { val rS = Random.nextInt(gridState.size - 4); val cS = Random.nextInt(gridState.size - 4); val nC = gridState.cells.map { it.toMutableList() }; for (r in rS until rS + 4) for (c in cS until cS + 4) nC[r][c] = Cell(); gridState = gridState.copy(cells = nC.map { it.toList() }); vibrate(150) }
    override fun onCleared() { super.onCleared(); soundPool?.release() }
}
