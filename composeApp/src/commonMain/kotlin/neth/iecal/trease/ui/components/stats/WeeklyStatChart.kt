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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*
import neth.iecal.trease.models.FocusStats
import neth.iecal.trease.utils.toLocalDate
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun WeeklyActivityChart(allStats: List<FocusStats>) {
    val timeZone = TimeZone.currentSystemDefault()
    val today = Clock.System.todayIn(timeZone)

    val oldestDate = allStats.minOfOrNull { it.completedOn.toLocalDate() } ?: today
    val daysDiff = today.daysUntil(oldestDate).let { abs(it) }
    val totalWeeks = (daysDiff / 7) + 1

    val statsByDay = remember(allStats) {
        allStats.filter { !it.isFailed }.groupBy { it.completedOn.toLocalDate() }
            .mapValues { (_, stats) -> stats.sumOf { it.duration } / 60 }
    }

    val pagerState = rememberPagerState(pageCount = { totalWeeks })

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isCompact = maxHeight < 180.dp || maxWidth < 260.dp

        Column(Modifier.fillMaxSize()) {
            val currentWeekOffset = pagerState.currentPage
            val endOfWeek = today.minus(DatePeriod(days = currentWeekOffset * 7))
            val startOfWeek = endOfWeek.minus(DatePeriod(days = 6))

            // In compact mode, use smaller text and tighter padding
            val headerStyle = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium
            val headerPadding = if (isCompact) 4.dp else 16.dp

            Text(
                text = if (isCompact) {
                    // Abbreviated header for mini widgets: "Oct 12 - 19"
                    "${startOfWeek.month.name.take(3)} ${startOfWeek.dayOfMonth}-${endOfWeek.dayOfMonth}"
                } else {
                    "${startOfWeek.month.name} ${startOfWeek.dayOfMonth} - ${endOfWeek.month.name} ${endOfWeek.dayOfMonth}"
                },
                style = headerStyle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier
                    .padding(bottom = headerPadding)
                    .align(Alignment.CenterHorizontally)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val pageEndDate = today.minus(DatePeriod(days = page * 7))
                val weekDates = (0..6).map { i -> pageEndDate.minus(DatePeriod(days = 6 - i)) }

                val weekData = weekDates.map { date ->
                    date to (statsByDay[date] ?: 0L)
                }

                WeeklyBarChartCanvas(
                    data = weekData,
                    isCompact = isCompact
                )
            }
        }
    }
}

@Composable
fun WeeklyBarChartCanvas(
    data: List<Pair<LocalDate, Long>>,
    isCompact: Boolean
) {
    val textMeasurer = rememberTextMeasurer()

    // Colors
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Typography & Dimensions based on Compact Mode
    val labelStyle = if (isCompact)
        TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Medium)
    else
        MaterialTheme.typography.labelSmall

    val leftPadding = if (isCompact) 24.dp else 40.dp
    val bottomPadding = if (isCompact) 16.dp else 32.dp
    val barCornerRadius = if (isCompact) 3.dp else 6.dp

    // Reduce grid noise in tiny widgets (only 2 steps vs 4 steps)
    val gridSteps = if (isCompact) 2 else 4

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Convert Dp to Px for internal calculations
        val leftPadPx = leftPadding.toPx()
        val botPadPx = bottomPadding.toPx()

        // Effective Chart Area
        val chartWidth = width - leftPadPx
        val chartHeight = height - botPadPx

        if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

        // Calculate Y-Axis Scale
        val maxVal = data.maxOfOrNull { it.second } ?: 0L
        // Ensure nice rounding (e.g. always multiples of 60 mins)
        val yMax = max(60L, (ceil(maxVal / 60.0) * 60).toLong())

        val stepHeight = chartHeight / gridSteps
        val stepValue = yMax / gridSteps

        for (i in 0..gridSteps) {
            val y = chartHeight - (i * stepHeight)
            val value = i * stepValue

            // Grid Line
            drawLine(
                color = gridColor,
                start = Offset(leftPadPx, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            // Y-Axis Label
            // Don't draw 0 or intermediate labels in compact mode to save space, just Max and Mid?
            // Actually, showing 0 is usually implicit. Let's show > 0.
            if (i > 0) {
                val label = if (value >= 60) "${value / 60}h" else "${value}m"

                val textResult = textMeasurer.measure(label, labelStyle.copy(color = labelColor))

                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        x = (leftPadPx - textResult.size.width) - 4.dp.toPx(), // Right align to axis
                        y = y - (textResult.size.height / 2)
                    )
                )
            }
        }

        val barSpaceAllocated = chartWidth / data.size
        val barWidth = barSpaceAllocated * 0.65f // 65% width, 35% gap
        val halfBarWidth = barWidth / 2

        data.forEachIndexed { index, (date, minutes) ->
            val centerX = leftPadPx + (index * barSpaceAllocated) + (barSpaceAllocated / 2)

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

            val dayName = if (isCompact) {
                // Just first letter "M", "T" for compact
                date.dayOfWeek.name.take(1)
            } else {
                // "Mon", "Tue" for full
                date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            }

            val textResult = textMeasurer.measure(dayName, labelStyle.copy(color = labelColor))

            drawText(
                textLayoutResult = textResult,
                topLeft = Offset(
                    x = centerX - (textResult.size.width / 2),
                    y = chartHeight + 6.dp.toPx() // Padding below chart
                )
            )
        }
    }
}