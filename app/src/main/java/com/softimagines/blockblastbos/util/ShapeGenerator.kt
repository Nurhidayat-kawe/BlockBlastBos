package com.softimagines.blockblastbos.util

import androidx.compose.ui.graphics.Color
import com.softimagines.blockblastbos.data.model.GameShape
import kotlin.random.Random

object ShapeGenerator {
    private val colorList = listOf(
        Color(0xFF00BCD4), Color(0xFFFFEB3B), Color(0xFF9C27B0),
        Color(0xFFFF9800), Color(0xFF3F51B5), Color(0xFF4CAF50), Color(0xFFF44336)
    )

    fun getRandomShape(): GameShape {
        val blocks = when (Random.nextInt(7)) {
            0 -> GameShape.IShapeBlocks
            1 -> GameShape.OShapeBlocks
            2 -> GameShape.TShapeBlocks
            3 -> GameShape.LShapeBlocks
            4 -> GameShape.JShapeBlocks
            5 -> GameShape.SShapeBlocks
            else -> GameShape.ZShapeBlocks
        }
        return GameShape(blocks, colorList[Random.nextInt(colorList.size)])
    }

    fun getThreeRandomShapes(): List<GameShape> {
        return List(3) { getRandomShape() }
    }
}
