package com.softimagines.blockblastbos.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import com.softimagines.blockblastbos.data.model.GameShape
import com.softimagines.blockblastbos.data.model.Position
import kotlin.math.roundToInt

@Composable
fun NextBlocks(
    shapes: List<GameShape>,
    gridCoordinates: LayoutCoordinates?,
    gridSize: Int,
    onDragUpdate: (GameShape?, Position?) -> Unit,
    onShapePlaced: (GameShape, Position, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        shapes.forEachIndexed { index, shape ->
            key(shape.id) {
                DraggableShape(
                    shape = shape,
                    gridCoordinates = gridCoordinates,
                    gridSize = gridSize,
                    onDragUpdate = onDragUpdate,
                    onDragEnd = { position -> 
                        onShapePlaced(shape, position, index)
                    }
                )
            }
        }
    }
}

@Composable
fun DraggableShape(
    shape: GameShape,
    gridCoordinates: LayoutCoordinates?,
    gridSize: Int,
    onDragUpdate: (GameShape?, Position?) -> Unit,
    onDragEnd: (Position) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var itemCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var isOverGrid by remember { mutableStateOf(false) }

    val density = androidx.compose.ui.platform.LocalDensity.current
    val boxSizePx = with(density) { 80.dp.toPx() }
    val cellDisplaySize = boxSizePx / 4
    
    val liftOffsetPx = -300f 

    val maxRow = remember(shape.blocks) { shape.blocks.maxOfOrNull { it.row }?.plus(1) ?: 1 }
    val maxCol = remember(shape.blocks) { shape.blocks.maxOfOrNull { it.col }?.plus(1) ?: 1 }

    val gridCellSizePx = if (gridCoordinates != null && gridCoordinates.size.width > 0) {
        gridCoordinates.size.width.toFloat() / gridSize
    } else 0f

    val draggingScale by animateFloatAsState(
        targetValue = if (isDragging) {
            if (isOverGrid && gridCellSizePx > 0) gridCellSizePx / cellDisplaySize else 1.2f
        } else 0.7f,
        label = "scale"
    )

    // Pengali sensitivitas
    val sensitivityMultiplier = 1.5f

    Box(
        modifier = Modifier
            .size(80.dp)
            .onGloballyPositioned { itemCoordinates = it }
            .graphicsLayer {
                translationX = offsetX
                translationY = if (isDragging) offsetY + liftOffsetPx else offsetY
                scaleX = draggingScale
                scaleY = draggingScale
            }
            .pointerInput(shape.id) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        if (gridCoordinates != null && itemCoordinates != null && gridCellSizePx > 0) {
                            val gridPos = gridCoordinates.positionInWindow()
                            val itemPos = itemCoordinates!!.positionInWindow()
                            
                            val centerX = itemPos.x + offsetX + boxSizePx / 2
                            val centerY = itemPos.y + offsetY + liftOffsetPx + boxSizePx / 2
                            
                            // Snapping yang lebih akurat: kurangi setengah ukuran grid block
                            val dropX = (centerX - gridPos.x) - (maxCol * gridCellSizePx / 2f)
                            val dropY = (centerY - gridPos.y) - (maxRow * gridCellSizePx / 2f)
                            
                            val col = (dropX / gridCellSizePx).roundToInt()
                            val row = (dropY / gridCellSizePx).roundToInt()
                            onDragEnd(Position(row, col))
                        }
                        onDragUpdate(null, null)
                        offsetX = 0f
                        offsetY = 0f
                        isOverGrid = false
                    },
                    onDragCancel = {
                        isDragging = false
                        onDragUpdate(null, null)
                        offsetX = 0f
                        offsetY = 0f
                        isOverGrid = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x * sensitivityMultiplier
                        offsetY += dragAmount.y * sensitivityMultiplier
                        
                        if (gridCoordinates != null && itemCoordinates != null && gridCellSizePx > 0) {
                            val gridPos = gridCoordinates.positionInWindow()
                            val itemPos = itemCoordinates!!.positionInWindow()
                            
                            val centerX = itemPos.x + offsetX + boxSizePx / 2
                            val centerY = itemPos.y + offsetY + liftOffsetPx + boxSizePx / 2
                            
                            val margin = gridCellSizePx * 1.5f
                            isOverGrid = centerX > gridPos.x - margin && 
                                         centerX < gridPos.x + gridCoordinates.size.width + margin &&
                                         centerY > gridPos.y - margin && 
                                         centerY < gridPos.y + gridCoordinates.size.height + margin

                            // Hitung row/col pratinjau dengan logika yang sama
                            val dropX = (centerX - gridPos.x) - (maxCol * gridCellSizePx / 2f)
                            val dropY = (centerY - gridPos.y) - (maxRow * gridCellSizePx / 2f)
                            
                            val col = (dropX / gridCellSizePx).roundToInt()
                            val row = (dropY / gridCellSizePx).roundToInt()
                            onDragUpdate(shape, Position(row, col))
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellDisplaySizeLocal = size.minDimension / 4
            val startX = (size.width - (maxCol * cellDisplaySizeLocal)) / 2
            val startY = (size.height - (maxRow * cellDisplaySizeLocal)) / 2

            shape.blocks.forEach { block ->
                draw3DBlock(
                    color = shape.color,
                    offset = Offset(
                        startX + block.col * cellDisplaySizeLocal, 
                        startY + block.row * cellDisplaySizeLocal
                    ),
                    size = cellDisplaySizeLocal - 2
                )
            }
        }
    }
}
