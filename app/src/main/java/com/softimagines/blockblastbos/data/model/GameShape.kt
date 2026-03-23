package com.softimagines.blockblastbos.data.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

open class GameShape(
    val blocks: List<Position>,
    val color: Color,
    val id: String = UUID.randomUUID().toString() // Tambahkan ID unik
) {
    companion object {
        val IShapeBlocks = listOf(Position(0, 0), Position(1, 0), Position(2, 0), Position(3, 0))
        val OShapeBlocks = listOf(Position(0, 0), Position(0, 1), Position(1, 0), Position(1, 1))
        val TShapeBlocks = listOf(Position(0, 1), Position(1, 0), Position(1, 1), Position(1, 2))
        val LShapeBlocks = listOf(Position(0, 0), Position(1, 0), Position(2, 0), Position(2, 1))
        val JShapeBlocks = listOf(Position(0, 1), Position(1, 1), Position(2, 1), Position(2, 0))
        val SShapeBlocks = listOf(Position(0, 1), Position(0, 2), Position(1, 0), Position(1, 1))
        val ZShapeBlocks = listOf(Position(0, 0), Position(0, 1), Position(1, 1), Position(1, 2))
    }
}
