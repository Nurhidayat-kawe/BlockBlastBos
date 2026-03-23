package com.softimagines.blockblastbos.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softimagines.blockblastbos.data.model.GameMode
import com.softimagines.blockblastbos.viewmodel.GameViewModel

@Composable
fun MainMenu(
    viewModel: GameViewModel,
    onStartGame: (GameMode) -> Unit
) {
    val highScore by viewModel.highScore.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val theme = viewModel.currentTheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(theme.background, Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "BLOCK BLAST",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Text(
                text = "BOS EDITION",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Yellow,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("HIGHSCORE", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Text(highScore.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    Text("🪙 $coins COINS", color = Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            MenuButton("PLAY CLASSIC", Color(0xFF4CAF50)) { onStartGame(GameMode.CLASSIC) }
            MenuButton("BOSS BATTLE", Color(0xFFF44336)) { onStartGame(GameMode.BOSS_BATTLE) }
            MenuButton("TIME ATTACK", Color(0xFF2196F3)) { onStartGame(GameMode.TIME_ATTACK) }
            MenuButton("ZEN MODE", Color(0xFF9C27B0)) { onStartGame(GameMode.ZEN) }
            MenuButton("PUZZLE LEVELS", Color(0xFFFF9800)) { onStartGame(GameMode.PUZZLE) }
        }
    }
}

@Composable
fun MenuButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}
