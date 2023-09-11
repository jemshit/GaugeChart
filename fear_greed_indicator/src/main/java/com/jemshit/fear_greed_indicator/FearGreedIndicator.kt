package com.jemshit.fear_greed_indicator

import android.graphics.PointF
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// @improve: turn parameters into classes with builder
@Composable
fun FearGreedIndicator(modifier: Modifier = Modifier.background(Color.LightGray),
                       width: Dp,
                       height: Dp,

                       arcLength: Float = 0.51f,
                       arcDrawStyle: Stroke = Stroke(36f, cap = StrokeCap.Butt),
                       arcColors: List<Color> = listOf(Color.Red,
                                                       Color.Yellow,
                                                       Color.Green),

                       steps: List<Int> = listOf(0, 50, 100),
                       stepsPercentSign: Boolean = false,
                       stepsPaint: android.graphics.Paint? = null,

                       stepMarkers: Boolean = true,
                       stepMarkerColor: Color = Color.White,
                       stepMarkerWidth: Float = 7f,

                       currentStep: Int? = 69,
                       indicatorBottomMargin: Float = 0f,
                       currentStepPaint: android.graphics.Paint? = null,
                       currentStepBgColors: Map<Pair<Int, Int>, Pair<Color, Color>>? = null,
                       currentStepTextColors: Map<Pair<Int, Int>, Color>? = null,

                       indicatorBellyColor: Color = Color.DarkGray,
                       indicatorBellyBorderColor: Color = Color.LightGray,
                       indicatorNeedleColor: Color = Color.Gray) {
    // validation
    if (arcLength < 0f || arcLength > 1f) {
        return
    }
    if (arcColors.isEmpty()) {
        return
    }
    if (steps.find { it !in 0..100 } != null) {
        return
    }
    if (currentStep != null && currentStep !in 0..100) {
        return
    }

    // remember
    val stepAngles = remember(arcLength) {
        getAnglesOfSteps(arcLength)
    }
    val stepColors: List<Color> = remember(currentStepBgColors, arcColors) {
        val stepColorPairs = currentStepBgColors ?: generateStepColorPairs(arcColors)
        getStepBgColors(stepColorPairs)
    }
    val stepTextColors: List<Color> = remember(stepColors, currentStepTextColors) {
        if (currentStepTextColors != null) {
            getStepTextColors(currentStepTextColors)
        } else {
            getStepTextColors(stepColors)
        }
    }

    // @improve
    // adjust canvas location/size for arc
    val totalHeight = with(LocalDensity.current) {
        remember(height, width) {
            kotlin.math.max(height.toPx(), width.toPx())
        }
    }
    val canvasPadding = with(LocalDensity.current) {
        remember {
            60.dp.toPx()
        }
    }
    val canvasOffset = remember(canvasPadding) {
        Offset(canvasPadding, canvasPadding)
    }
    val canvasSizeBigger = with(LocalDensity.current) {
        remember(width, canvasPadding, totalHeight) {
            Size(width.toPx() - canvasPadding * 2,
                 totalHeight - canvasPadding * 2)
        }
    }
    /*val canvasSizeReal = with(LocalDensity.current) {
        remember(width, canvasPadding, height) {
            Size(width.toPx() - canvasPadding * 2,
                 height.toPx() - canvasPadding * 2)
        }
    }*/
    val canvasCenter = remember(canvasSizeBigger, canvasOffset) {
        canvasSizeBigger.center + canvasOffset
    }
    /*val arcFitsIntoCanvas = with(LocalDensity.current) {
        remember(height, width, arcLength) {
            height.toPx() / width.toPx() >= arcLength
        }
    }*/

    // half of arc should be on bottom side of 180°, other half is above 180°
    // gradient calculation starts at 3 o-clock, so we have to draw around 180°
    val arcLengthTransformed = remember(arcLength) {
        360f * arcLength
    }
    val arcStartAngle = remember(arcLengthTransformed) {
        180f - (arcLengthTransformed / 2f)
    }
    val arcBrush = remember(canvasCenter, arcLength, arcColors) {
        createArcBrush(canvasCenter, arcLength, arcColors)
    }
    val arcWidth: Float = arcDrawStyle.width
    val arcWidthAdjusted = with(LocalDensity.current) {
        remember(arcWidth) {
            kotlin.math.max(arcWidth, 14.dp.toPx())
        }
    }
    val arcOffset = remember(arcWidth) {
        Offset(arcWidth / 2f, arcWidth / 2f)
    }

    // animate
    val currentStepState = animateIntAsState(targetValue = currentStep ?: 0,
                                             animationSpec = tween(750, 0, LinearEasing),
                                             label = "currentStepState")

    // @improve: works best for square, good for landscape, bad for portrait size
    Canvas(Modifier
                   .requiredWidth(width)
                   .requiredHeight(height)
                   .then(modifier)) {

        // we drew around 180° (because of gradient brush), now we rotate to move to 270°
        rotate(90f, pivot = Offset(size.width / 2f, totalHeight / 2f)) {
            drawArc(brush = arcBrush,
                    startAngle = arcStartAngle,
                    sweepAngle = arcLengthTransformed,
                    useCenter = false,
                    topLeft = arcOffset + canvasOffset,
                    size = Size(canvasSizeBigger.width - arcOffset.x * 2,
                                canvasSizeBigger.height - arcOffset.y * 2),
                    style = arcDrawStyle)
        }

        if (steps.isNotEmpty()) {
            drawStepTexts(Size(size.width, totalHeight),
                          canvasOffset,
                          arcLength,
                          arcWidth,

                          steps,
                          stepsPercentSign,
                          stepsPaint)

            if (stepMarkers) {
                drawStepMarkers(canvasSizeBigger,
                                totalHeight,
                                stepAngles.filterIndexed { index, _ ->
                                    steps
                                            .filter { it in 1..99 }
                                            .contains(index)
                                },
                                arcWidth,

                                stepMarkerColor,
                                stepMarkerWidth)
            }
        }

        if (currentStep != null) {
            val currentStepCenter = getCurrentStepCircleCenter(
                    canvasSizeBigger,
                    totalHeight,
                    canvasOffset,
                    stepAngles[currentStepState.value]
            )
            val currentStepCircleRadius =
                kotlin.math.min(arcWidthAdjusted + 6.dp.toPx(), 18.dp.toPx())

            drawCurrentStep(stepsPercentSign,
                            currentStepState.value,
                            currentStepCircleRadius,
                            currentStepPaint,
                            currentStepCenter,

                            stepColors,
                            stepTextColors)

            val indicatorBellyRadius = kotlin.math.min(arcWidthAdjusted + 4.dp.toPx(), 15.dp.toPx())
            drawIndicator(arcLength,

                          currentStepCircleRadius,
                          currentStepCenter,
                          stepAngles[currentStepState.value],

                          indicatorBottomMargin,
                          indicatorBellyRadius,
                          indicatorBellyColor,
                          indicatorBellyBorderColor,
                          indicatorNeedleColor)
        }

    }
}

private fun generateStepColorPairs(arcColors: List<Color>)
        : Map<Pair<Int, Int>, Pair<Color, Color>> {
    if (arcColors.size == 1) {
        return mapOf(Pair(0, 100) to Pair(arcColors[0], arcColors[0]))
    }

    val stepColorPairs = mutableMapOf<Pair<Int, Int>, Pair<Color, Color>>()
    val distance = (100f / (arcColors.size - 1)).toInt()
    var start = 0
    var colorIndex = 0
    while (start < 100) {
        val end = kotlin.math.min(100, start + distance)
        stepColorPairs[Pair(start, end)] = Pair(arcColors[colorIndex], arcColors[colorIndex + 1])
        start = end + 1
        colorIndex += 1
    }
    return stepColorPairs
}

private fun getStepBgColors(stepBgColorPairs: Map<Pair<Int, Int>, Pair<Color, Color>>)
        : List<Color> {
    return (0..100).toList()
            .map { step ->
                val stepColorPair = stepBgColorPairs
                                            .asIterable()
                                            .firstOrNull { entry ->
                                                step in (entry.key.first)..(entry.key.second)
                                            }
                                            ?.toPair()
                                    ?: (Pair(0, 100) to Pair(Color.Black, Color.Black))

                val stepPair = stepColorPair.first
                val colorPair = stepColorPair.second
                Color(
                        ColorUtils.blendARGB(colorPair.first.toArgb(),
                                             colorPair.second.toArgb(),
                                             (step - stepPair.first).toFloat() / (stepPair.second - stepPair.first))
                )
            }
}

private fun getStepTextColors(stepTextColorPairs: Map<Pair<Int, Int>, Color>)
        : List<Color> {
    return (0..100).toList()
            .map { step ->
                stepTextColorPairs
                        .asIterable()
                        .firstOrNull { entry ->
                            step in (entry.key.first)..(entry.key.second)
                        }
                        ?.value
                ?: Color.White
            }
}

private fun getStepTextColors(stepColors: List<Color>)
        : List<Color> {
    return stepColors.map { color ->
        color.generateOnColor()
    }
}


private fun createArcBrush(canvasCenter: Offset,
                           arcLength: Float,
                           arcColors: List<Color>)
        : Brush {
    return if (arcColors.size == 1) {
        // single color
        SolidColor(arcColors[0])
    } else {
        // gradient color
        val colorAndStops = mutableListOf<Pair<Float, Color>>()
        // first point
        colorAndStops.add(0f to Color.Transparent)

        // middle colors and stops
        val halfLength = arcLength / 2f
        val gradientLeftStart = 0.5f - halfLength
        val gradientRightStop = 0.5f + halfLength
        val step: Float = (gradientRightStop - gradientLeftStart) / (arcColors.size - 1)
        var nextPoint = gradientLeftStart + step
        colorAndStops.add(gradientLeftStart to arcColors.first())
        repeat(arcColors.size - 2) { index ->
            colorAndStops.add(nextPoint to arcColors[index + 1])
            nextPoint += step
        }
        colorAndStops.add(gradientRightStop to arcColors.last())

        // last point
        colorAndStops.add(1f to Color.Transparent)

        Brush.sweepGradient(*(colorAndStops.toTypedArray()),
                            center = canvasCenter)
    }
}

/** each index contains angle for that index: Map<step, angle>*/
private fun getAnglesOfSteps(arcLength: Float)
        : List<Double> {
    return (0..100).toList()
            .map { step ->
                if (step <= 50) {
                    val stepMultiplier = 1 - step / 50f
                    90.0 + 180.0 * arcLength * stepMultiplier
                } else {
                    val stepMultiplier = step / 50f - 1
                    90.0 - 180.0 * arcLength * stepMultiplier
                }
            }
}

// @improve: use angles to draw step texts
private fun DrawScope.drawStepTexts(totalSize: Size,
                                    canvasOffset: Offset,
                                    arcLength: Float,
                                    arcWidth: Float,

                                    steps: List<Int>,
                                    stepsPercentSign: Boolean,
                                    stepsPaint: android.graphics.Paint?) {

    // above arc is rotated 90°, to make same arc we rotate this Path by changing startAngle
    val arcLengthTransformed = 360f * arcLength
    val arcStartAngleRotated = 270f - (arcLengthTransformed / 2f)
    val arcWidthAdjusted = if (arcWidth < 30f) 30f else arcWidth

    val marginLeft = canvasOffset.x + arcWidth + 8.dp.toPx()
    val marginTop = canvasOffset.y + arcWidth + arcWidthAdjusted / 2f + 8.dp.toPx()
    val marginRight = canvasOffset.x + arcWidth + 8.dp.toPx()
    val marginBottom = canvasOffset.y + arcWidth + 8.dp.toPx()

    // this contains Path for arc
    val arcPaths = Path().apply {
        addArc(Rect(left = marginLeft,
                    top = marginTop,
                    right = totalSize.width - marginRight,
                    bottom = totalSize.height - marginBottom),
               arcStartAngleRotated,
               arcLengthTransformed)
    }

    // select few Points from arcPaths
    val arcPathsMeasure = android.graphics.PathMeasure(arcPaths.asAndroidPath(), false)
    val arcPathsLength = arcPathsMeasure.length
    val selectedPoints = mutableMapOf<Int, PointF>()

    // fill 0,1,2,..100 with Points
    val selectedPointsLength = 101 // assumes arcPathsLength is at least 101 points
    val tempXY = floatArrayOf(0f, 0f)
    if (arcPathsLength >= selectedPointsLength) {
        for (pointIndex in 0 until selectedPointsLength) {
            // #getPosTan only exists on non-compose PathMeasure
            arcPathsMeasure.getPosTan((arcPathsLength * pointIndex) / (selectedPointsLength - 1),
                                      tempXY,
                                      null)
            selectedPoints[pointIndex] = PointF(tempXY[0], tempXY[1])
        }
    }

    // test draw Arc Path and selected Points
    /*drawPath(arcPaths,
             SolidColor(Color.Black),
             style = Stroke(width = arcWidthAdjusted, cap = StrokeCap.Round))*/
    /*drawPoints(selectedPoints.values.map { Offset(it.x, it.y) },
               PointMode.Points,
               SolidColor(Color.Red),
               10f,
               cap = StrokeCap.Round)*/

    // paint
    val textPaint = stepsPaint ?: Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 12.dp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER // updated below accordingly
        color = android.graphics.Color.GRAY
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        style = android.graphics.Paint.Style.FILL
    }

    // draw
    if (selectedPoints.size >= steps.size) {
        drawIntoCanvas { canvas ->
            steps.forEachIndexed { index, step ->
                // if Point for text exists, draw it
                selectedPoints[steps[index]]?.let { point ->
                    if (step == 50
                        || (arcLength == 1f && step == 100)
                        || (arcLength == 1f && step == 0)
                    ) {
                        textPaint.textAlign = android.graphics.Paint.Align.CENTER
                    } else if (step < 50) {
                        // [0,50) texts start from point
                        textPaint.textAlign = android.graphics.Paint.Align.LEFT
                    } else {
                        // (50,100] texts end at point
                        textPaint.textAlign = android.graphics.Paint.Align.RIGHT
                    }

                    canvas.nativeCanvas.drawText(
                            if (stepsPercentSign) "$step%" else "$step",
                            point.x,
                            point.y,
                            textPaint
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawStepMarkers(canvasSizeBigger: Size,
                                      totalHeight: Float,
                                      angles: List<Double>,
                                      arcWidth: Float,

                                      stepMarkerColor: Color,
                                      stepMarkerWidth: Float) {
    for (angle in angles) {
        // - for classical cartesian angles (counter-clockwise)
        val theta = (-angle).toRadian().toFloat()
        val startRadius = canvasSizeBigger.width / 2 - arcWidth
        val endRadius = canvasSizeBigger.width / 2
        val center = Offset(size.width / 2f, totalHeight / 2f)
        val startPos = center + Offset(cos(theta) * startRadius,
                                       sin(theta) * startRadius)
        val endPos = center + Offset(cos(theta) * endRadius,
                                     sin(theta) * endRadius)
        drawLine(
                color = stepMarkerColor,
                start = startPos,
                end = endPos,
                strokeWidth = stepMarkerWidth,
                cap = StrokeCap.Butt
        )
    }
}

private fun DrawScope.getCurrentStepCircleCenter(canvasSizeReal: Size,
                                                 totalHeight: Float,
                                                 canvasOffset: Offset,
                                                 currentStepAngle: Double)
        : Offset {
    // - for classical cartesian angles (counter-clockwise)
    val theta = (-currentStepAngle).toRadian().toFloat()
    val xOffset = canvasSizeReal.width / 2 + canvasOffset.x * .60f
    val yOffset = canvasSizeReal.height / 2 + canvasOffset.y * .60f
    val center = Offset(size.width / 2f, totalHeight / 2f)
    return center + Offset(cos(theta) * xOffset,
                           sin(theta) * yOffset)
}

private fun DrawScope.drawCurrentStep(stepsPercentSign: Boolean,
                                      currentStep: Int,
                                      currentStepCircleRadius: Float,
                                      currentStepPaint: android.graphics.Paint?,
                                      circleCenter: Offset,

                                      stepColors: List<Color>,
                                      stepTextColors: List<Color>) {

    val textPaint = currentStepPaint ?: Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 15.dp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        style = android.graphics.Paint.Style.FILL
    }
    textPaint.color = stepTextColors[currentStep].toArgb()


    drawCircle(stepColors[currentStep],
               radius = currentStepCircleRadius,
               center = circleCenter)

    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawText(
                if (stepsPercentSign) "$currentStep%" else "$currentStep",
                circleCenter.x,
                circleCenter.y + (textPaint.textSize / 3f),
                textPaint
        )
    }

}

private fun DrawScope.drawIndicator(arcLength: Float,

                                    currentStepCircleRadius: Float,
                                    currentStepCenter: Offset,
                                    currentStepAngle: Double,

                                    indicatorBottomMargin: Float,
                                    indicatorBellyRadius: Float,
                                    indicatorBellyColor: Color,
                                    indicatorBellyBorderColor: Color,
                                    indicatorNeedleColor: Color) {
    val indicatorCenter = if (size.height < size.width) {
        Offset(size.width / 2f, size.height - (indicatorBellyRadius + indicatorBottomMargin))
    } else {
        if (arcLength <= 0.6f) {
            center - Offset(0f, indicatorBellyRadius / 2)
        } else {
            center
        }
    }
    // needle
    // - angle to get classical cartesian angle (counter-clockwise)
    val currentStepAngleRadian = -(currentStepAngle).toRadian().toFloat()
    // cos(90) = 1, sin(90)=0. When at 90, x should move but not y
    val offsetAngleRadian = (90.0 - (-currentStepAngle)).toRadian().toFloat()

    val needleStartWidth = indicatorBellyRadius - 2.dp.toPx()
    val needleEndWidth = 2.dp.toPx()
    val currentStepCenterDistance = currentStepCircleRadius + 3.dp.toPx()
    val currentStepOffset =
        currentStepCenter - Offset(cos(currentStepAngleRadian) * currentStepCenterDistance,
                                   sin(currentStepAngleRadian) * currentStepCenterDistance)

    val triangleStartOffset =
        indicatorCenter - Offset(cos(offsetAngleRadian) * needleStartWidth / 2f,
                                 -sin(offsetAngleRadian) * needleStartWidth / 2f)

    val triangleCenter1Offset =
        currentStepOffset - Offset(cos(offsetAngleRadian) * needleEndWidth / 2f,
                                   -sin(offsetAngleRadian) * needleEndWidth / 2f)
    val triangleCenter2Offset =
        currentStepOffset + Offset(cos(offsetAngleRadian) * needleEndWidth / 2f,
                                   -sin(offsetAngleRadian) * needleEndWidth / 2f)

    val triangleEndOffset = indicatorCenter + Offset(cos(offsetAngleRadian) * needleStartWidth / 2f,
                                                     -sin(offsetAngleRadian) * needleStartWidth / 2f)

    val trianglePath = Path().apply {
        moveTo(triangleStartOffset.x, triangleStartOffset.y)
        lineTo(triangleCenter1Offset.x, triangleCenter1Offset.y)
        lineTo(triangleCenter2Offset.x, triangleCenter2Offset.y)
        lineTo(triangleEndOffset.x, triangleEndOffset.y)
        close()
    }

    drawPath(trianglePath, indicatorNeedleColor)


    // belly
    drawCircle(indicatorBellyColor,
               radius = indicatorBellyRadius,
               center = indicatorCenter)

    drawCircle(indicatorBellyBorderColor,
               radius = indicatorBellyRadius,
               center = indicatorCenter,
               style = Stroke(width = 2.dp.toPx()))

}


// region Previews
@Preview(group = "arc", name = "25", showBackground = true)
@Composable
private fun PreviewArc25() {
    FearGreedIndicator(width = 370.dp,
                       height = 200.dp,

                       arcLength = 0.25f,
                       arcColors = listOf(Color.Red, Color.Green),
                       currentStep = 10)
}

@Preview(group = "arc", name = "50", showBackground = true)
@Composable
private fun PreviewArc50() {
    FearGreedIndicator(width = 370.dp,
                       height = 200.dp,

                       arcLength = 0.5f,
                       arcColors = listOf(Color.Red, Color.Green),
                       currentStep = 50)
}

@Preview(group = "arc", name = "60", showBackground = true)
@Composable
private fun PreviewArc60() {
    FearGreedIndicator(width = 370.dp,
                       height = 200.dp,

                       arcLength = 0.6f,
                       steps = listOf(0, 25, 50, 75, 100),
                       currentStep = 50)
}

@Preview(group = "arc", name = "100", showBackground = true)
@Composable
private fun PreviewArc100() {
    FearGreedIndicator(width = 370.dp,
                       height = 370.dp,

                       arcLength = 1f,
                       steps = listOf(25, 50, 75, 100),
                       currentStep = 45)
}

@Preview(group = "step", name = "0", showBackground = true)
@Composable
private fun PreviewStep0() {
    FearGreedIndicator(width = 370.dp,
                       height = 200.dp,

                       arcLength = 0.51f,
                       currentStep = 0)
}

@Preview(group = "step", name = "75", showBackground = true)
@Composable
private fun PreviewStep75() {
    FearGreedIndicator(width = 370.dp,
                       height = 200.dp,

                       arcLength = 0.51f,
                       arcDrawStyle = Stroke(18f, cap = StrokeCap.Round),
                       currentStep = 75)
}

@Preview(group = "stepText", name = "sign", showBackground = true)
@Composable
private fun PreviewStepTextSign() {
    FearGreedIndicator(width = 370.dp,
                       height = 200.dp,

                       arcLength = 0.51f,
                       arcDrawStyle = Stroke(18f, cap = StrokeCap.Round),
                       stepsPercentSign = true,
                       currentStep = 75)
}

@Preview(group = "color", name = "single", showBackground = true)
@Composable
private fun PreviewColorSingle() {
    FearGreedIndicator(width = 370.dp,
                       height = 200.dp,

                       arcLength = 0.9f,
                       arcColors = listOf(Color.Blue),
                       steps = listOf(20, 50, 85),
                       currentStep = 0)
}

@Preview(group = "color", name = "multi", showBackground = true)
@Composable
private fun PreviewColorMulti() {
    FearGreedIndicator(width = 370.dp,
                       height = 370.dp,

                       arcLength = 0.80f,
                       arcDrawStyle = Stroke(45f, cap = StrokeCap.Butt),
                       arcColors = listOf(Color.Red,
                                          Color.Yellow,
                                          Color.Green,
                                          Color.Blue,
                                          Color.Black),
                       steps = listOf(0, 25, 50, 75),
                       currentStep = 100)
}

@Preview(group = "error", name = "1", showBackground = true)
@Composable
private fun PreviewError1() {
    FearGreedIndicator(width = 370.dp,
                       height = 200.dp,

                       arcLength = 11f)
}

@Preview(group = "animate", name = "0To50", showBackground = true)
@Composable
private fun PreviewAnimate50To25() {
    var step by remember { mutableIntStateOf(50) }
    LaunchedEffect(Unit) {
        delay(1000)
        step = 25
    }
    FearGreedIndicator(width = 370.dp,
                       height = 250.dp,

                       arcLength = 0.60f,
                       arcDrawStyle = Stroke(45f, cap = StrokeCap.Butt),
                       arcColors = listOf(Color.Red,
                                          Color.Yellow,
                                          Color.Green,
                                          Color.Blue,
                                          Color.Black),
                       steps = listOf(0, 25, 50, 75),
                       currentStep = step)
}
// endregion