package neth.iecal.trease.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.datetime.*
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

@Composable
fun FullScreenGarden(navController: NavHostController) {
    val viewModel = viewModel { GardenViewModel() }

    var selectedDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    LaunchedEffect(selectedDate) {
        val isoFormat = "${selectedDate.year}-${selectedDate.month.number.toString().padStart(2, '0')}"
        viewModel.updateTreeStats(isoFormat)
    }

    val statsList by viewModel.stats.collectAsStateWithLifecycle()
    val pngTreeList by viewModel.pngTreeList.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val totalFocus by viewModel.totalFocus.collectAsStateWithLifecycle()

    var showTopRight by remember { mutableStateOf(true) }
    var showBottomLeft by remember { mutableStateOf(true) }
    var showBottomRight by remember { mutableStateOf(true) }

    val isAllHidden = !showTopRight && !showBottomLeft && !showBottomRight

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ZoomableMapLayer {
                Box(Modifier.size(1500.dp)) {
                    IsometricForest(pngTreeList)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                GlassHudWidget(
                    modifier = Modifier.align(Alignment.TopStart),
                    onClose = null
                ) {
                    MiniMonthSelector(selectedDate) { selectedDate = it }
                }

                if (showTopRight) {
                    GlassHudWidget(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClose = { showTopRight = false }
                    ) {
                        MiniResourceCounter(totalFocus, streak)
                    }
                }

                if (showBottomLeft) {
                    GlassHudWidget(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .width(220.dp)  // Tiny width
                            .height(140.dp), // Tiny height
                        onClose = { showBottomLeft = false }
                    ) {
                        Column {
                            Text("HISTORY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontSize = 8.sp)
                            Box(Modifier.weight(1f)) { WeeklyActivityChart(statsList) }
                        }
                    }
                }

                if (showBottomRight) {
                    GlassHudWidget(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .width(220.dp)
                            .height(140.dp),
                        onClose = { showBottomRight = false }
                    ) {
                        Column {
                            Text("PEAK TIME", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontSize = 8.sp)
                            Box(Modifier.weight(1f)) { HourlyFocusChart(statsList) }
                        }
                    }
                }

                if (isAllHidden) {
                    FloatingActionButton(
                        onClick = {
                            showTopRight = true
                            showBottomLeft = true
                            showBottomRight = true
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Text("o")
                    }
                }
            }
        }
    }
}


@Composable
private fun GlassHudWidget(
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), // High transparency
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
            modifier = Modifier.wrapContentSize()
        ) {
            Box(Modifier.padding(12.dp)) {
                content()
            }
        }

        // The "X" Close Button (Overlay)
        if (onClose != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp) // Slight overhang
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text("X")
            }
        }
    }
}

@Composable
fun ZoomableMapLayer(content: @Composable () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    val newOffset = offset + pan
                    offset = newOffset
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun MiniMonthSelector(date: LocalDate, onMonthChange: (LocalDate) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = { onMonthChange(date.minus(DatePeriod(months = 1))) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(painterResource(Res.drawable.outline_arrow_back_ios_24), null, modifier = Modifier.size(12.dp))
        }

        Text(
            text = "${date.month.name.take(3)} '${date.year.toString().takeLast(2)}",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(
            onClick = { onMonthChange(date.plus(DatePeriod(months = 1))) },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(painterResource(Res.drawable.outline_arrow_forward_ios_24), null, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
fun MiniResourceCounter(focus: Int, streak: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(horizontalAlignment = Alignment.End) {
            Text("$focus", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("min", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
        }
        Box(Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.onSurface.copy(0.1f)))
        Column(horizontalAlignment = Alignment.End) {
            Text("$streak", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text("days", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}