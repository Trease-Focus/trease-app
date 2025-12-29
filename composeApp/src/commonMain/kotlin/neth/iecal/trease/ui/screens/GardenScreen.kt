package neth.iecal.trease.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import neth.iecal.trease.models.FocusStats
import neth.iecal.trease.ui.components.IsometricForest
import neth.iecal.trease.utils.getDayLabel
import neth.iecal.trease.utils.getHourOfDay
import neth.iecal.trease.viewmodels.GardenViewModel
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.time.Instant

@Composable
fun GardenScreen(navController: NavHostController) {
    val viewModel = viewModel { GardenViewModel() }

    // 1. Load Data
    val statsList by viewModel.stats.collectAsStateWithLifecycle()
    val treeIds by viewModel.treeList.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Header
            HeaderSection(treeIds)

            KeyMetricsRow(statsList)

            StatsCard(
                title = "Focus History",
                subtitle = "Last 7 sessions by duration (minutes)"
            ) {
                ActivityBarChart(statsList)
            }

            StatsCard(
                title = "Peak Productivity",
                subtitle = "When you are most active"
            ) {
                TimeOfDayChart(statsList)
            }

            SecondaryMetricsGrid(statsList)

            StatsCard(
                title = "Tree Health",
                subtitle = "Success vs Withered Ratio"
            ) {
                EfficiencyDonutChart(statsList)
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun HeaderSection(treeIds: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(300.dp)
                .padding(top = 16.dp)
        ) {
            IsometricForest(treeIds)
        }
        Text(
            text = "Your Garden", // Dynamic month could go here
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun KeyMetricsRow(stats: List<FocusStats>) {
    val totalSeconds = remember(stats) { stats.sumOf { it.duration } }
    val totalHours = (totalSeconds / 3600.0).toFloat()

    //Calculate Streak (consecutive days)
    // This is a simplified streak calculation for the UI demo
    val streak = remember(stats) {
        if (stats.isEmpty()) 0 else 1
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Total Focus",
            value = (round(totalHours * 10) / 10).toString(),
            subValue = "hours",
            iconColor = MaterialTheme.colorScheme.primary
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Current Streak",
            value = "$streak",
            subValue = "days",
            iconColor = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun SecondaryMetricsGrid(stats: List<FocusStats>) {
    val validStats = stats.filter { !it.isFailed }

    val avgDuration = if (validStats.isNotEmpty()) {
        (validStats.map { it.duration }.average() / 60).roundToInt()
    } else 0

    val longestSession = if (validStats.isNotEmpty()) {
        (validStats.maxOf { it.duration } / 60).toInt()
    } else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MiniMetricCard(
            modifier = Modifier.weight(1f),
            label = "Avg Session",
            value = "${avgDuration}m"
        )
        MiniMetricCard(
            modifier = Modifier.weight(1f),
            label = "Best Session",
            value = "${longestSession}m"
        )
        MiniMetricCard(
            modifier = Modifier.weight(1f),
            label = "Total Trees",
            value = "${validStats.size}"
        )
    }
}

@Composable
fun MetricCard(modifier: Modifier = Modifier, label: String, value: String, subValue: String, iconColor: Color) {
    Surface(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(iconColor))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MiniMetricCard(modifier: Modifier = Modifier, label: String, value: String) {
    Surface(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatsCard(title: String, subtitle: String? = null, content: @Composable BoxScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().height(240.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Box(
                modifier = Modifier.padding(24.dp),
                content = content
            )
        }
    }
}

// --- Component: History Graph ---
@Composable
fun ActivityBarChart(allStats: List<FocusStats>) {
    // Take last 7 items for the graph
    val recentStats = remember(allStats) { allStats.sortedBy { it.completedOn }.takeLast(7) }

    if (recentStats.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Complete a session to see history", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val failColor = MaterialTheme.colorScheme.error
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Text configuration (simple size approximation for canvas)
    val textPaint = MaterialTheme.typography.labelSmall

    Column(Modifier.fillMaxSize()) {
        // The Graph
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val maxDuration = recentStats.maxOfOrNull { it.duration }?.toFloat() ?: 1f
            // Add 10% headroom
            val displayMax = maxDuration * 1.1f

            val barWidth = size.width / (recentStats.size * 2 + 1) // Dynamic width
            val spacing = size.width / recentStats.size

            recentStats.forEachIndexed { index, stat ->
                val xOffset = (spacing * index) + (spacing / 2) - (barWidth / 2)

                // Calculate height based on Seconds
                val barHeight = (stat.duration / displayMax) * size.height

                // 1. Draw Track (Background)
                drawRoundRect(
                    color = trackColor.copy(alpha = 0.5f),
                    topLeft = Offset(xOffset, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(barWidth / 2)
                )

                // 2. Draw Active Bar
                drawRoundRect(
                    color = if (stat.isFailed) failColor.copy(alpha = 0.8f) else primaryColor,
                    topLeft = Offset(xOffset, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2)
                )
            }
        }

        // The X-Axis Labels (Outside Canvas for easier Layout)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            recentStats.forEach { stat ->
                Text(
                    text = stat.completedOn.getDayLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }
        }
    }
}

@Composable
fun TimeOfDayChart(stats: List<FocusStats>) {
    // Morning: 5-11, Afternoon: 12-17, Evening: 18-4
    val distribution = remember(stats) {
        val counts = mutableMapOf("Morning" to 0, "Afternoon" to 0, "Evening" to 0)
        stats.forEach {
            val hour = it.completedOn.getHourOfDay()
            when (hour) {
                in 5..11 -> counts["Morning"] = counts["Morning"]!! + 1
                in 12..17 -> counts["Afternoon"] = counts["Afternoon"]!! + 1
                else -> counts["Evening"] = counts["Evening"]!! + 1
            }
        }
        val total = stats.size.toFloat().coerceAtLeast(1f)
        counts.mapValues { it.value / total }
    }

    if (stats.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data yet", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        TimeDistributionRow("Morning", distribution["Morning"] ?: 0f, MaterialTheme.colorScheme.secondary)
        TimeDistributionRow("Afternoon", distribution["Afternoon"] ?: 0f, MaterialTheme.colorScheme.primary)
        TimeDistributionRow("Evening", distribution["Evening"] ?: 0f, MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
fun TimeDistributionRow(label: String, percentage: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            modifier = Modifier.width(70.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${(percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(35.dp),
            textAlign = TextAlign.End
        )
    }
}

// --- Component: Efficiency Donut (Reused & Updated) ---
@Composable
fun EfficiencyDonutChart(stats: List<FocusStats>) {
    val total = stats.size
    val failed = stats.count { it.isFailed }
    val success = total - failed

    val successColor = MaterialTheme.colorScheme.primary
    val failColor = MaterialTheme.colorScheme.error
    val emptyColor = MaterialTheme.colorScheme.surfaceContainerHigh

    if (total == 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Start planting to see health", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 16.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2

                drawCircle(color = emptyColor, radius = radius, style = Stroke(width = strokeWidth))

                val successSweep = (success.toFloat() / total) * 360f
                drawArc(
                    color = successColor,
                    startAngle = -90f,
                    sweepAngle = successSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${((success.toFloat()/total)*100).toInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricLegendItem(color = successColor, text = "Thriving ($success)")
            MetricLegendItem(color = failColor, text = "Withered ($failed)")
        }
    }
}

@Composable
fun MetricLegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}