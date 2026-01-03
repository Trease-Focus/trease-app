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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isCompact = maxHeight < 180.dp || maxWidth < 260.dp

        Column(Modifier.fillMaxSize()) {
            val headerStyle = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium
            val headerPadding = if (isCompact) 4.dp else 24.dp
            val headerText = if (isCompact) "Peak Hours" else "Productivity by Hour"

            Text(
                text = headerText,
                style = headerStyle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = headerPadding)
                    .align(Alignment.CenterHorizontally)
            )

            HourlyBarChartCanvas(
                data = hourlyData,
                isCompact = isCompact
            )
        }
    }
}

@Composable
fun HourlyBarChartCanvas(
    data: List<Long>,
    isCompact: Boolean
) {
    val textMeasurer = rememberTextMeasurer()

    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val labelStyle = if (isCompact)
        TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Medium)
    else
        MaterialTheme.typography.labelSmall

    val leftPadding = if (isCompact) 24.dp else 40.dp
    val bottomPadding = if (isCompact) 16.dp else 32.dp
    val barCornerRadius = if (isCompact) 1.5.dp else 3.dp

    val gridSteps = if (isCompact) 2 else 4

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val leftPadPx = leftPadding.toPx()
        val botPadPx = bottomPadding.toPx()

        val chartWidth = width - leftPadPx
        val chartHeight = height - botPadPx

        if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

        val maxVal = data.maxOfOrNull { it } ?: 0L
        val yMax = max(60L, (ceil(maxVal / 60.0) * 60).toLong())

        val stepHeight = chartHeight / gridSteps
        val stepValue = yMax / gridSteps

        for (i in 0..gridSteps) {
            val y = chartHeight - (i * stepHeight)
            val value = i * stepValue

            drawLine(
                color = gridColor,
                start = Offset(leftPadPx, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            if (i > 0) {
                val label = if (value >= 60) "${value / 60}h" else "${value}m"

                val textResult = textMeasurer.measure(label, labelStyle.copy(color = labelColor))

                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        x = (leftPadPx - textResult.size.width) - 4.dp.toPx(),
                        y = y - (textResult.size.height / 2)
                    )
                )
            }
        }

        val barSpaceAllocated = chartWidth / 24
        val fillRatio = if (isCompact) 0.75f else 0.6f
        val barWidth = barSpaceAllocated * fillRatio
        val halfBarWidth = barWidth / 2

        data.forEachIndexed { hour, minutes ->
            val centerX = leftPadPx + (hour * barSpaceAllocated) + (barSpaceAllocated / 2)
            val barHeight = (minutes / yMax.toFloat()) * chartHeight

            drawRoundRect(
                color = trackColor.copy(alpha = 0.3f),
                topLeft = Offset(centerX - halfBarWidth, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(barCornerRadius.toPx())
            )

            if (barHeight > 0) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(centerX - halfBarWidth, chartHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barCornerRadius.toPx())
                )
            }

            // Show every 6 hours (0, 6, 12, 18) to prevent clutter
            if (hour % 6 == 0) {
                val timeLabel = when(hour) {
                    0 -> "12am"
                    12 -> "12pm"
                    else -> "${hour % 12}${if (isCompact) "" else if (hour < 12) "am" else "pm"}"
                    // In compact mode, just show "6", "12" etc to save space, or keep AM/PM if it fits.
                    // Let's keep it simple: "6" for compact, "6am" for full
                }

                // If compact, simplify text further
                val finalLabel = if (isCompact && timeLabel.length > 2) timeLabel.filter { it.isDigit() } else timeLabel

                val textResult = textMeasurer.measure(finalLabel, labelStyle.copy(color = labelColor))

                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        x = centerX - (textResult.size.width / 2),
                        y = chartHeight + 6.dp.toPx()
                    )
                )
            }
        }
    }
}