package com.softimagines.blockblastbos.data.model

import com.softimagines.blockblastbos.ui.theme.GameTheme

data class GameHistory(
    val gridState: GridState,
    val score: Int,
    val availableShapes: List<GameShape>,
    val comboCount: Int
)

enum class PowerUpType {
    BOMB,       // Clear 3x3 area
    COLOR_BOMB, // Clear all blocks of a specific color
    EXTRA_BLOCK, // Replace current shapes
    FREEZE,     // Not used yet but planned
    ROTATE      // Rotate available shapes
}

data class PowerUp(
    val type: PowerUpType,
    val count: Int = 3
)
