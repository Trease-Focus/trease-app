package neth.iecal.trease.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.coroutines.delay
import neth.iecal.trease.Constants
import neth.iecal.trease.PlatformVideoPlayer
import neth.iecal.trease.models.TimerStatus
import neth.iecal.trease.models.TreeData
import neth.iecal.trease.utils.getCachedVideoPath
import neth.iecal.trease.viewmodels.HomeScreenViewModel


private data class PlayerState(
    val status: TimerStatus,
    val tree: TreeData?,
    val seed: Int
)
@Composable
fun TreeGrowthPlayer(
    viewModel: HomeScreenViewModel,
    scale: Float,
) {
    var isReady by remember { mutableStateOf(false) }
    val statePlayer = rememberVideoPlayerState()
    val crntStatus by viewModel.timerStatus.collectAsStateWithLifecycle()

    val selectedTree by viewModel.selectedTree.collectAsStateWithLifecycle()
    val selectedSeed by viewModel.currentTreeSeedVariant.collectAsStateWithLifecycle()
    val selectedMinutes by viewModel.selectedMinutes.collectAsStateWithLifecycle()

    val combinedState = remember(crntStatus, selectedTree,selectedSeed) {
        PlayerState(crntStatus, selectedTree, selectedSeed)
    }
    val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine), // 4s slow breath
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    LaunchedEffect(selectedTree, selectedSeed) {
        val remoteUrl = "${Constants.cdn}/video/${selectedTree?.id}_${selectedSeed}.webm"

        println("loading tree $remoteUrl")
        try {
            val localPath = getCachedVideoPath(remoteUrl, selectedTree?.id ?: "tree",selectedSeed)
            statePlayer.openUri(localPath)
            isReady = true

            val originalDurationMs = 30_000L
            println("duration ${statePlayer.durationText}")
            val targetDurationMs = selectedMinutes * 60_000L
            val stretchFactor = targetDurationMs.toDouble() / originalDurationMs

            val playChunkMs = 100L
            val pauseChunkMs = ((playChunkMs * stretchFactor) - playChunkMs).toLong().coerceAtLeast(0)

            // Monotonic video loop (no animation logic inside here anymore)
            while (true) {
                statePlayer.play()
                delay(playChunkMs)
                statePlayer.pause()
                delay(pauseChunkMs)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().scale(scale),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = combinedState,
            transitionSpec = {
                fadeIn(tween(800)) togetherWith fadeOut(tween(800))
            },
            modifier = Modifier.fillMaxSize(),
            label = "StateTransition"
        ) { status ->
            when (status.status) {
                TimerStatus.Running -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PlatformVideoPlayer(
                            statePlayer,
                            Modifier.fillMaxSize()
                        )

                        Canvas(modifier = Modifier.size(350.dp)) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFF9C4).copy(alpha = glowAlpha), // Soft Sunlight Yellow
                                        Color(0xFFFFD54F).copy(alpha = glowAlpha * 0.5f), // Warm Amber
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = (size.minDimension / 2) * glowScale
                                ),
                                radius = (size.minDimension / 2) * glowScale,
                                center = center
                            )
                        }
                    }
                }

                TimerStatus.POST_QUIT, TimerStatus.HAS_QUIT -> {
                    AsyncImage(
                        model = "${Constants.cdn}/images/weathered_grid.png",
                        contentDescription = status.tree?.id,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        filterQuality = FilterQuality.High
                    )
                }
                else -> {
                    Box(Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = "${Constants.cdn}/images/${status.tree?.id}_0_grid.png",
                            contentDescription = status.tree?.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            filterQuality = FilterQuality.High
                        )

                        if (!isReady) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

            }
        }
    }
}