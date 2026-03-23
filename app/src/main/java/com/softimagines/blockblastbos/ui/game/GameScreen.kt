package com.softimagines.blockblastbos.ui.game

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.softimagines.blockblastbos.data.model.GameMode
import com.softimagines.blockblastbos.data.model.PowerUpType
import com.softimagines.blockblastbos.ui.theme.ThemeProvider
import com.softimagines.blockblastbos.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    onBackToMenu: () -> Unit
) {
    val theme = viewModel.currentTheme
    val gridState = viewModel.gridState
    val currentScore = viewModel.currentScore
    val highScore by viewModel.highScore.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val availableShapes = viewModel.availableShapes
    val isGameOver = viewModel.isGameOver
    val canRevive = viewModel.canRevive
    val particles = viewModel.particles
    val comboCount = viewModel.comboCount
    val gameMode = viewModel.currentGameMode
    val timeLeft = viewModel.timeLeft
    val bossHealth = viewModel.bossHealth
    
    val isLevelComplete = viewModel.isLevelComplete
    val levelData = viewModel.currentLevelData
    val currentLevelIndex = viewModel.currentLevelIndex
    val showDailyReward = viewModel.showDailyRewardDialog

    val ghostShape = viewModel.draggingShape
    val ghostPosition = viewModel.dragPreviewPosition
    
    val context = LocalContext.current
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    
    LaunchedEffect(isGameOver) {
        if (isGameOver && canRevive) {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(context, "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) { rewardedAd = null }
                override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
            })
        }
    }

    fun showRewardedAd() {
        rewardedAd?.let { ad ->
            ad.show(context as Activity) { _ ->
                viewModel.revive()
                rewardedAd = null
            }
        }
    }

    var gridCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Bar: Economy, Home & Themes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tombol Home
                IconButton(
                    onClick = onBackToMenu,
                    modifier = Modifier.size(32.dp).background(Color.White.copy(0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.3f)), shape = CircleShape) {
                    Text("🪙 $coins", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            ThemeSelector(onThemeSelected = { viewModel.changeTheme(it) }, currentThemeName = theme.name)
        }

        // 2. Info HUD (Level/Time/Boss)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (gameMode == GameMode.PUZZLE) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LEVEL ${currentLevelIndex + 1}", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Target: ${levelData.targetScore}", color = Color.White.copy(0.7f), fontSize = 12.sp)
                }
            } else if (gameMode == GameMode.TIME_ATTACK) {
                Text("TIME: ${timeLeft}s", color = if (timeLeft < 30) Color.Red else Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
            } else if (gameMode == GameMode.BOSS_BATTLE) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BOSS HP: $bossHealth%", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = { bossHealth / 100f }, modifier = Modifier.width(150.dp).height(8.dp).clip(RoundedCornerShape(4.dp)), color = Color.Red, trackColor = Color.White.copy(alpha = 0.2f))
                }
            } else {
                Text(gameMode.name, color = Color.White.copy(0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 3. Score & Combo
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (comboCount > 0) Text("COMBO x$comboCount 🔥", color = Color.Yellow, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Text(currentScore.toString(), color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Black)
        }

        // 4. Game Grid
        GameGrid(gridState = gridState, particles = particles, ghostShape = ghostShape, ghostPosition = ghostPosition, onGridPositioned = { gridCoordinates = it }, modifier = Modifier.fillMaxWidth().weight(1f, fill = false))

        // 5. Power-ups (Cost Coins)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            PowerUpButton("Undo", viewModel.canUndo) { viewModel.undo() }
            PowerUpButton("Rotate (200)", coins >= viewModel.POWERUP_COST) { viewModel.usePowerUp(PowerUpType.ROTATE) }
            PowerUpButton("Refresh (200)", coins >= viewModel.POWERUP_COST) { viewModel.usePowerUp(PowerUpType.EXTRA_BLOCK) }
        }

        // 6. Next Area
        Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color.Black.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
            NextBlocks(shapes = availableShapes, gridCoordinates = gridCoordinates, gridSize = gridState.size, onDragUpdate = { s, p -> viewModel.updateDragPreview(s, p) }, onShapePlaced = { s, p, i -> viewModel.onBlockPlaced(s, p) })
        }

        Text(
            text = "team developer by: SoftImagines",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
    }

    // --- Overlays ---
    if (showDailyReward) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Hadiah Harian! 🎁") },
            text = { Text("Selamat datang kembali! Kamu mendapatkan 100 koin gratis hari ini.") },
            confirmButton = { Button(onClick = { viewModel.claimDailyReward() }) { Text("Ambil Koin") } }
        )
    }

    if (isLevelComplete) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("🎉 Level Selesai!") },
            text = { Text("Kamu berhasil! Lanjut ke tantangan berikutnya?") },
            confirmButton = { Button(onClick = { viewModel.nextLevel() }) { Text("Lanjut ke Level ${currentLevelIndex + 2}") } }
        )
    }

    if (isGameOver) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Game Over 💀") },
            text = { Text("Skor: $currentScore. Pilih cara untuk lanjut:") },
            confirmButton = {
                Column {
                    if (coins >= viewModel.REVIVE_COST) {
                        Button(onClick = { viewModel.reviveWithCoins() }, modifier = Modifier.fillMaxWidth()) {
                            Text("💰 Revive (${viewModel.REVIVE_COST} Koin)")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (canRevive) {
                        Button(onClick = { showRewardedAd() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                            Text("📺 Revive (Iklan)")
                        }
                    }
                }
            },
            dismissButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = { viewModel.resetGame() }) { Text("Main Lagi") }
                    TextButton(onClick = onBackToMenu) { Text("Ke Menu Utama") }
                }
            }
        )
    }
}

@Composable
fun PowerUpButton(l: String, e: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = e, modifier = Modifier.height(36.dp), contentPadding = PaddingValues(horizontal = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))) { Text(l, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun ThemeSelector(onThemeSelected: (com.softimagines.blockblastbos.ui.theme.GameTheme) -> Unit, currentThemeName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ThemeProvider.allThemes.forEach { theme ->
            Box(modifier = Modifier.padding(2.dp).size(26.dp).clip(CircleShape).background(theme.gridBackground).clickable { onThemeSelected(theme) }.then(if (theme.name == currentThemeName) Modifier.background(Color.White.copy(alpha = 0.5f)) else Modifier), contentAlignment = Alignment.Center) { Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(theme.blockColors[0])) }
        }
    }
}
