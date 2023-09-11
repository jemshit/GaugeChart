package com.jemshit.gaugechart

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance


internal typealias Degree = Double
internal typealias Radian = Double

fun Degree.toRadian(): Radian = this / 180 * Math.PI

internal fun Color.generateOnColor()
        : Color {
    return if (luminance() > 0.5f) {
        Color.Black.copy(alpha = .8f)
    } else {
        Color.White
    }
}
