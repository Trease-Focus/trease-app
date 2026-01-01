package neth.iecal.trease.ui.components.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import neth.iecal.trease.models.FocusStats
import neth.iecal.trease.utils.getHourOfDay
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun HourlyFocusChart(stats: List<FocusStats>) {
    val hourlyData = remember(stats) {
        val buckets =  MutableList(24) { 0L }
        stats.filter { !it.isFailed }.forEach { stat ->
            val hour = stat.completedOn.getHourOfDay()
            buckets[hour] += (stat.duration / 60) // Add minutes
        }
        buckets.toList()
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Productivity by Hour",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 24.dp).align(Alignment.CenterHorizontally)
        )

        HourlyBarChartCanvas(hourlyData)
    }
}

@Composable
fun HourlyBarChartCanvas(data: List<Long>) {
    val textMeasurer = rememberTextMeasurer()

    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelSmall

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 40.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
    ) {
        if (size.width <= 0 || size.height <= 0) return@Canvas

        val maxVal = data.maxOfOrNull { it } ?: 0L

        val yMax = max(60L, (ceil(maxVal / 60.0) * 60).toLong())

        val chartHeight = size.height
        val chartWidth = size.width


        val barWidth = (chartWidth / 24) * 0.6f
        val spacing = (chartWidth / 24)

        val steps = 4
        val stepHeight = chartHeight / steps
        val stepValue = yMax / steps

        for (i in 0..steps) {
            val y = chartHeight - (i * stepHeight)
            val value = i * stepValue


            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(chartWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            // Y-Label
            if (i > 0) {
                val label = if (value >= 60) "${value / 60}h" else "${value}m"

                val textLayoutResult = textMeasurer.measure(
                    text = label,
                    style = labelStyle.copy(color = labelColor)
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = -35.dp.toPx(),
                        y = y - (textLayoutResult.size.height / 2)
                    )
                )
            }
        }


        data.forEachIndexed { hour, minutes ->
            val x = (hour * spacing) + (spacing / 2)
            val barHeight = (minutes / yMax.toFloat()) * chartHeight

            // Track (Background Bar)
            drawRoundRect(
                color = trackColor.copy(alpha = 0.3f),
                topLeft = Offset(x - barWidth / 2, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(2.dp.toPx()) // Slightly sharper corners for thinner bars
            )

            // Active Bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x - barWidth / 2, chartHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx())
            )

            if (hour % 6 == 0) {
                val timeLabel = when(hour) {
                    0 -> "12am"
                    12 -> "12pm"
                    else -> "${hour % 12}${if (hour < 12) "am" else "pm"}"
                }

                val textLayoutResult = textMeasurer.measure(
                    text = timeLabel,
                    style = labelStyle.copy(color = labelColor)
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = x - (textLayoutResult.size.width / 2),
                        y = chartHeight + 8.dp.toPx()
                    )
                )
            }
        }
    }
}