package neth.iecal.trease.ui.components.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import neth.iecal.trease.models.FocusStats
import neth.iecal.trease.utils.toLocalDate
import kotlin.collections.filter
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.time.Clock

@Composable
fun WeeklyActivityChart(allStats: List<FocusStats>) {
    val timeZone = TimeZone.currentSystemDefault()
    val today = Clock.System.todayIn(timeZone)

    val oldestDate = allStats.minOfOrNull { it.completedOn.toLocalDate() } ?: today
    val daysDiff = today.daysUntil(oldestDate).let { abs(it) }
    val totalWeeks = (daysDiff / 7) + 1

    val statsByDay = remember(allStats) {
        allStats.filter { !it.isFailed }.groupBy { it.completedOn.toLocalDate() }
            .mapValues { (_, stats) -> stats.sumOf { it.duration } / 60 } // Sum duration in mins
    }

    val pagerState = rememberPagerState(pageCount = { totalWeeks })

    Column(Modifier.fillMaxSize()) {
        val currentWeekOffset = pagerState.currentPage
        val endOfWeek = today.minus(DatePeriod(days = currentWeekOffset * 7))
        val startOfWeek = endOfWeek.minus(DatePeriod(days = 6))

        Text(
            text = "${startOfWeek.month.name} ${startOfWeek.day} - ${endOfWeek.month.name} ${endOfWeek.dayOfMonth}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { page ->
            // Calculate date range for this specific page
            val pageEndDate = today.minus(DatePeriod(days = page * 7))
            val weekDates = (0..6).map { i -> pageEndDate.minus(DatePeriod(days = 6 - i)) }

            // Extract data for this week (filling 0 for empty days)
            val weekData = weekDates.map { date ->
                date to (statsByDay[date] ?: 0L)
            }

            WeeklyBarChartCanvas(weekData)
        }
    }
}

@Composable
fun WeeklyBarChartCanvas(data: List<Pair<LocalDate, Long>>) {
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
        // Guard against 0-size crashes (e.g., during initial layout pass)
        if (size.width <= 0 || size.height <= 0) return@Canvas

        val maxVal = data.maxOfOrNull { it.second } ?: 0L
        val yMax = max(60L, (ceil(maxVal / 60.0) * 60).toLong())

        val chartHeight = size.height
        val chartWidth = size.width
        val barWidth = (chartWidth / data.size) * 0.6f
        val spacing = (chartWidth / data.size)

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

        data.forEachIndexed { index, (date, minutes) ->
            val x = (index * spacing) + (spacing / 2)

            val barHeight = (minutes / yMax.toFloat()) * chartHeight

            drawRoundRect(
                color = trackColor.copy(alpha = 0.3f),
                topLeft = Offset(x - barWidth / 2, 0f),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x - barWidth / 2, chartHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            val dayName = date.dayOfWeek.name.take(3)

            val textLayoutResult = textMeasurer.measure(
                text = dayName,
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