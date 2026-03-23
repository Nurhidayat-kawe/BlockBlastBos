package com.softimagines.blockblastbos.data.model

import androidx.compose.ui.graphics.Color

data class Cell(
    val isOccupied: Boolean = false,
    val color: Color = Color.Transparent
)

data class GridState(
    val size: Int = 8, // Standard Block Blast is 8x8
    val cells: List<List<Cell>> = List(size) { List(size) { Cell() } }
)
