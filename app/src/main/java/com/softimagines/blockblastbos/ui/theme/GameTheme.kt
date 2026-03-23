package com.softimagines.blockblastbos.ui.theme

import androidx.compose.ui.graphics.Color

data class GameTheme(
    val name: String,
    val background: Color,
    val gridBackground: Color,
    val emptyCellColor: Color,
    val blockColors: List<Color>,
    val particleColor: Color = Color.White,
    val lottieResId: Int? = null // Placeholder for Lottie animations
)

object ThemeProvider {
    val NeonCyber = GameTheme(
        name = "Neon Cyber",
        background = Color(0xFF0D0221),
        gridBackground = Color(0xFF1B065E),
        emptyCellColor = Color(0xFF240B36),
        blockColors = listOf(
            Color(0xFF00F5FF), // Cyan
            Color(0xFFFF00E4), // Magenta
            Color(0xFFADFF00), // Lime
            Color(0xFFFFEE00)  // Yellow
        ),
        particleColor = Color(0xFF00F5FF)
    )

    val WoodenForest = GameTheme(
        name = "Wooden Forest",
        background = Color(0xFF2D5A27),
        gridBackground = Color(0xFF1B3C15),
        emptyCellColor = Color(0xFF3E2723).copy(alpha = 0.5f),
        blockColors = listOf(
            Color(0xFF8B4513), // SaddleBrown
            Color(0xFFA0522D), // Sienna
            Color(0xFFCD853F), // Peru
            Color(0xFFDEB887)  // BurlyWood
        ),
        particleColor = Color(0xFF8B4513)
    )

    val Candy = GameTheme(
        name = "Candy",
        background = Color(0xFFFFC0CB),
        gridBackground = Color(0xFFFFB6C1),
        emptyCellColor = Color(0xFFFFF0F5),
        blockColors = listOf(
            Color(0xFFFF69B4), // HotPink
            Color(0xFFBA55D3), // MediumOrchid
            Color(0xFF00CED1), // DarkTurquoise
            Color(0xFFFFA500)  // Orange
        ),
        particleColor = Color.White
    )

    val Galaxy = GameTheme(
        name = "Galaxy",
        background = Color(0xFF0B0B1E),
        gridBackground = Color(0xFF161633),
        emptyCellColor = Color(0xFF1C1C3F),
        blockColors = listOf(
            Color(0xFFE0B0FF), // Mauve
            Color(0xFF9400D3), // DarkViolet
            Color(0xFF4B0082), // Indigo
            Color(0xFF000080)  // Navy
        ),
        particleColor = Color(0xFFFFFACD)
    )

    val allThemes = listOf(NeonCyber, WoodenForest, Candy, Galaxy)
}
