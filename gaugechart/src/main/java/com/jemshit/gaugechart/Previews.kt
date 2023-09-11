package com.jemshit.gaugechart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


@Preview(group = "arc", name = "25", showBackground = true)
@Composable
private fun PreviewArc25() {
    GaugeChart(width = 370.dp,
               height = 200.dp,

               arcLength = 0.25f,
               arcColors = listOf(Color.Red, Color.Green),
               currentStep = 10
    )
}

@Preview(group = "arc", name = "50", showBackground = true)
@Composable
private fun PreviewArc50() {
    GaugeChart(width = 370.dp,
               height = 200.dp,

               arcLength = 0.5f,
               arcColors = listOf(Color.Red, Color.Green),
               currentStep = 50
    )
}

@Preview(group = "arc", name = "60", showBackground = true)
@Composable
private fun PreviewArc60() {
    GaugeChart(width = 370.dp,
               height = 200.dp,

               arcLength = 0.6f,
               steps = listOf(0, 25, 50, 75, 100),
               currentStep = 50
    )
}

@Preview(group = "arc", name = "100", showBackground = true)
@Composable
private fun PreviewArc100() {
    GaugeChart(width = 370.dp,
               height = 370.dp,

               arcLength = 1f,
               steps = listOf(25, 50, 75, 100),
               currentStep = 45
    )
}

@Preview(group = "step", name = "0", showBackground = true)
@Composable
private fun PreviewStep0() {
    GaugeChart(width = 370.dp,
               height = 200.dp,

               arcLength = 0.51f,
               currentStep = 0
    )
}

@Preview(group = "step", name = "75", showBackground = true)
@Composable
private fun PreviewStep75() {
    GaugeChart(width = 370.dp,
               height = 200.dp,

               arcLength = 0.51f,
               arcDrawStyle = Stroke(18f, cap = StrokeCap.Round),
               currentStep = 75
    )
}

@Preview(group = "stepText", name = "sign", showBackground = true)
@Composable
private fun PreviewStepTextSign() {
    GaugeChart(width = 370.dp,
               height = 200.dp,

               arcLength = 0.51f,
               arcDrawStyle = Stroke(18f, cap = StrokeCap.Round),
               stepsPercentSign = true,
               currentStep = 75
    )
}

@Preview(group = "color", name = "single", showBackground = true)
@Composable
private fun PreviewColorSingle() {
    GaugeChart(width = 370.dp,
               height = 200.dp,

               arcLength = 0.5f,
               arcColors = listOf(Color.Blue),
               steps = listOf(20, 50, 85),
               currentStep = 0
    )
}

@Preview(group = "color", name = "multi", showBackground = true)
@Composable
private fun PreviewColorMulti() {
    GaugeChart(width = 370.dp,
               height = 370.dp,

               arcLength = 0.80f,
               arcDrawStyle = Stroke(45f, cap = StrokeCap.Butt),
               arcColors = listOf(Color.Red,
                                  Color.Yellow,
                                  Color.Green,
                                  Color.Blue,
                                  Color.Black),
               steps = listOf(0, 25, 50, 75),
               currentStep = 100
    )
}

@Preview(group = "color", name = "stepMarker", showBackground = true)
@Composable
private fun PreviewColorStepMarker() {
    GaugeChart(width = 370.dp,
               height = 200.dp,

               arcLength = 0.51f,
               arcDrawStyle = Stroke(38f, cap = StrokeCap.Butt),
               arcColors = listOf(Color.Red,
                                  Color.Yellow,
                                  Color.Green),
               steps = listOf(0, 25, 50, 75),
               stepMarkerColor = Color.Red,
               currentStep = 36
    )
}

@Preview(group = "color", name = "step", showBackground = true)
@Composable
private fun PreviewColorStep() {
    GaugeChart(width = 370.dp,
               height = 200.dp,

               arcLength = 0.51f,
               arcDrawStyle = Stroke(38f, cap = StrokeCap.Butt),
               arcColors = listOf(Color.Red,
                                  Color.Yellow,
                                  Color.Green),
               steps = listOf(0, 50),
               currentStep = 36,
               currentStepBgColors = mapOf(Pair(0, 50) to Pair(Color.Blue, Color.Cyan),
                                           Pair(50, 100) to Pair(Color.Cyan, Color.Black))
    )
}

@Preview(group = "error", name = "1", showBackground = true)
@Composable
private fun PreviewError1() {
    GaugeChart(width = 370.dp,
               height = 200.dp,

               arcLength = 11f
    )
}

@Preview(group = "animate", name = "0To50", showBackground = true)
@Composable
private fun PreviewAnimate50To25() {
    var step by remember { mutableIntStateOf(50) }
    LaunchedEffect(Unit) {
        delay(1000)
        step = 25
    }
    GaugeChart(width = 370.dp,
               height = 250.dp,

               arcLength = 0.60f,
               arcDrawStyle = Stroke(45f, cap = StrokeCap.Butt),
               arcColors = listOf(Color.Red,
                                  Color.Yellow,
                                  Color.Green,
                                  Color.Blue,
                                  Color.Black),
               steps = listOf(0, 25, 50, 75),
               currentStep = step
    )
}
