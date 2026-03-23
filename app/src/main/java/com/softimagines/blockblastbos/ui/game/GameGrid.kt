package com.softimagines.blockblastbos.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.softimagines.blockblastbos.data.model.GameShape
import com.softimagines.blockblastbos.data.model.GridState
import com.softimagines.blockblastbos.data.model.Particle
import com.softimagines.blockblastbos.data.model.Position

@Composable
fun GameGrid(
    gridState: GridState,
    particles: List<Particle>,
    ghostShape: GameShape?,
    ghostPosition: Position?,
    onGridPositioned: (LayoutCoordinates) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .onGloballyPositioned { onGridPositioned(it) }
    ) {
        val cellSize = maxWidth / gridState.size
        val cellPadding = 2.dp

        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizePx = cellSize.toPx()
            val paddingPx = cellPadding.toPx()
            val innerSizePx = sizePx - (paddingPx * 2)

            // 1. Draw Grid Slots (Empty Background)
            for (r in 0 until gridState.size) {
                for (c in 0 until gridState.size) {
                    val x = c * sizePx + paddingPx
                    val y = r * sizePx + paddingPx
                    
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.05f),
                        topLeft = Offset(x, y),
                        size = Size(innerSizePx, innerSizePx),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
            }

            // 2. Draw Ghost Shadow (Preview)
            if (ghostShape != null && ghostPosition != null) {
                ghostShape.blocks.forEach { block ->
                    val r = ghostPosition.row + block.row
                    val c = ghostPosition.col + block.col
                    if (r in 0 until gridState.size && c in 0 until gridState.size) {
                        drawRoundRect(
                            color = ghostShape.color.copy(alpha = 0.3f),
                            topLeft = Offset(c * sizePx + paddingPx, r * sizePx + paddingPx),
                            size = Size(innerSizePx, innerSizePx),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }
            }

            // 3. Draw Occupied Cells (3D Style)
            for (r in 0 until gridState.size) {
                for (c in 0 until gridState.size) {
                    val cell = gridState.cells[r][c]
                    if (cell.isOccupied) {
                        draw3DBlock(
                            color = cell.color,
                            offset = Offset(c * sizePx + paddingPx, r * sizePx + paddingPx),
                            size = innerSizePx
                        )
                    }
                }
            }

            // 4. Draw Particles
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size * (p.life),
                    center = p.position
                )
            }
        }
    }
}

fun DrawScope.draw3DBlock(color: Color, offset: Offset, size: Float) {
    val cornerRadius = 4.dp.toPx()
    val bevel = size * 0.15f

    // Main Face
    drawRoundRect(
        color = color,
        topLeft = offset,
        size = Size(size, size),
        cornerRadius = CornerRadius(cornerRadius)
    )

    // Highlight (Top and Left)
    val highlightPath = Path().apply {
        moveTo(offset.x, offset.y + size)
        lineTo(offset.x, offset.y)
        lineTo(offset.x + size, offset.y)
        lineTo(offset.x + size - bevel, offset.y + bevel)
        lineTo(offset.x + bevel, offset.y + bevel)
        lineTo(offset.x + bevel, offset.y + size - bevel)
        close()
    }
    drawPath(highlightPath, Color.White.copy(alpha = 0.3f))

    // Shadow (Bottom and Right)
    val shadowPath = Path().apply {
        moveTo(offset.x + size, offset.y)
        lineTo(offset.x + size, offset.y + size)
        lineTo(offset.x, offset.y + size)
        lineTo(offset.x + bevel, offset.y + size - bevel)
        lineTo(offset.x + size - bevel, offset.y + size - bevel)
        lineTo(offset.x + size - bevel, offset.y + bevel)
        close()
    }
    drawPath(shadowPath, Color.Black.copy(alpha = 0.3f))

    // Gloss effect
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
            startY = offset.y,
            endY = offset.y + size / 2
        ),
        topLeft = offset,
        size = Size(size, size),
        cornerRadius = CornerRadius(cornerRadius)
    )

    // Outer border for crisp look
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.2f),
        topLeft = offset,
        size = Size(size, size),
        cornerRadius = CornerRadius(cornerRadius),
        style = Stroke(width = 1.dp.toPx())
    )
}
