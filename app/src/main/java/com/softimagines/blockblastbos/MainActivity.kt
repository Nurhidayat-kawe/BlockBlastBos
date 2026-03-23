package com.softimagines.blockblastbos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.ads.MobileAds
import com.softimagines.blockblastbos.ui.game.AboutPrivacyScreen
import com.softimagines.blockblastbos.ui.game.GameScreen
import com.softimagines.blockblastbos.ui.game.MainMenu
import com.softimagines.blockblastbos.ui.theme.BlockBlastTheme
import com.softimagines.blockblastbos.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize AdMob
        MobileAds.initialize(this) {}
        
        setContent {
            BlockBlastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val viewModel: GameViewModel = viewModel()
                    var currentScreen by remember { mutableStateOf("menu") }

                    BackHandler(enabled = currentScreen != "menu") {
                        currentScreen = "menu"
                    }

                    when (currentScreen) {
                        "menu" -> {
                            MainMenu(
                                viewModel = viewModel,
                                onStartGame = { mode ->
                                    viewModel.setGameMode(mode)
                                    currentScreen = "game"
                                },
                                onAboutClick = { currentScreen = "about" }
                            )
                        }
                        "game" -> {
                            GameScreen(
                                viewModel = viewModel,
                                onBackToMenu = { currentScreen = "menu" }
                            )
                        }
                        "about" -> {
                            AboutPrivacyScreen(
                                onBack = { currentScreen = "menu" }
                            )
                        }
                    }
                }
            }
        }
    }
}
