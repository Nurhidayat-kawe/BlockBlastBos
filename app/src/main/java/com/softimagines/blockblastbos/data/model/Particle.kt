package com.softimagines.blockblastbos.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class Particle(
    val id: Long,
    val position: Offset,
    val velocity: Offset,
    val color: Color,
    val alpha: Float = 1f,
    val size: Float = 10f,
    val life: Float = 1f // 1.0 down to 0.0
)
