package com.jemshit.gaugechart

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance


typealias Degree = Double
typealias Radian = Double

fun Degree.toRadian(): Radian = this / 180 * Math.PI

fun Color.generateOnColor()
        : Color {
    return if (luminance() > 0.5f) {
        Color.Black.copy(alpha = .8f)
    } else {
        Color.White
    }
}
