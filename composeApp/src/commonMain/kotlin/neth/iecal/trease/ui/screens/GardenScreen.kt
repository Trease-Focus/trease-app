package neth.iecal.trease.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import neth.iecal.trease.models.FocusStats
import neth.iecal.trease.ui.components.stats.EfficiencyDonutChart
import neth.iecal.trease.ui.components.stats.HourlyFocusChart
import neth.iecal.trease.ui.components.IsometricForest
import neth.iecal.trease.ui.components.stats.WeeklyActivityChart
import neth.iecal.trease.viewmodels.GardenViewModel
import org.jetbrains.compose.resources.painterResource
import trease.composeapp.generated.resources.Res
import trease.composeapp.generated.resources.outline_arrow_back_ios_24
import trease.composeapp.generated.resources.outline_arrow_forward_ios_24
import kotlin.math.roundToInt

@Composable
fun GardenScreen(navController: NavHostController) {
    val viewModel = viewModel { GardenViewModel() }

    var selectedDate by remember {
        mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    LaunchedEffect(selectedDate) {
        val isoFormat = "${selectedDate.year}-${selectedDate.month.number.toString().padStart(2, '0')}"
        viewModel.updateTreeStats(isoFormat)
    }

    val statsList by viewModel.stats.collectAsStateWithLifecycle()
    val pngTreeList by viewModel.pngTreeList.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val totalFocus by viewModel.totalFocus.collectAsStateWithLifecycle()

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
            HeaderSection(
                pngList = pngTreeList,
                currentDate = selectedDate,
                onMonthChange = { newDate -> selectedDate = newDate }
            )

            KeyMetricsRow(totalFocus,streak)

            StatsCard(
                title = "Focus History",
                subtitle = "Last 7 sessions by duration (minutes)"
            ) {
                WeeklyActivityChart(statsList)
            }

            StatsCard(
                title = "Peak Productivity",
                subtitle = "When you are most active"
            ) {
                HourlyFocusChart(statsList)
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
fun HeaderSection(
    pngList: List<String>,
    currentDate: LocalDate,
    onMonthChange: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(300.dp)
                .padding(top = 16.dp)
        ) {
            IsometricForest(pngList)
        }

        MonthSelector(
            currentDate = currentDate,
            onMonthChange = onMonthChange
        )
    }
}

@Composable
fun MonthSelector(
    currentDate: LocalDate,
    onMonthChange: (LocalDate) -> Unit
) {
    fun changeMonth(monthsToAdd: Int) {
        onMonthChange(currentDate.plus(DatePeriod(months = monthsToAdd)))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Previous Month Button
        IconButton(onClick = { changeMonth(-1) }) {
            Icon(
                painter = painterResource(Res.drawable.outline_arrow_back_ios_24),
                contentDescription = "Previous Month",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .width(200.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (dragAmount < -20) {
                            changeMonth(1)
                        } else if (dragAmount > 20) {
                            changeMonth(-1)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentDate,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "Month Animation"
            ) { date ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = date.month.name.lowercase().replaceFirstChar { it.uppercase() },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Next Month Button
        IconButton(onClick = { changeMonth(1) }) {
            Icon(
                painter = painterResource(Res.drawable.outline_arrow_forward_ios_24),
                contentDescription = "Previous Month",
                tint = MaterialTheme.colorScheme.primary
            )

        }
    }
}

@Composable
private fun KeyMetricsRow(totalMins: Int, streak:Int) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            label = "Total Focus",
            value = (totalMins).toString(),
            subValue = "mins",
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
private fun SecondaryMetricsGrid(
    stats: List<FocusStats>,
) {
    val validStats = remember(stats) { stats.filter { !it.isFailed } }

    val avgDuration = if (validStats.isNotEmpty()) {
        (validStats.map { it.duration }.average() / 60.0).roundToInt()
    } else 0

    val longestSession = if (validStats.isNotEmpty()) {
        (validStats.maxOf { it.duration } / 60.0).roundToInt()
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
private fun MetricCard(modifier: Modifier = Modifier, label: String, value: String, subValue: String, iconColor: Color) {
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
private fun MiniMetricCard(modifier: Modifier = Modifier, label: String, value: String) {
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
private fun StatsCard(title: String, subtitle: String? = null, content: @Composable BoxScope.() -> Unit) {
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



