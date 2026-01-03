package neth.iecal.trease.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import kotlinx.coroutines.isActive
import neth.iecal.trease.Constants
import neth.iecal.trease.PlatformVideoPlayer
import neth.iecal.trease.models.TimerStatus
import neth.iecal.trease.models.TreeData
import neth.iecal.trease.utils.getCachedVideoPath
import neth.iecal.trease.utils.mmssToSeconds
import neth.iecal.trease.viewmodels.HomeScreenViewModel

// Helper for animation state
private data class OverlayState(
    val status: TimerStatus,
    val tree: TreeData?,
    val seed: Int
)

@Composable
fun TreeGrowthPlayer(
    viewModel: HomeScreenViewModel,
    scale: Float,
) {
    var isVideoLoaded by remember { mutableStateOf(false) }
    val statePlayer = rememberVideoPlayerState()
    val crntStatus by viewModel.timerStatus.collectAsStateWithLifecycle()

    val selectedMinutes by viewModel.selectedMinutes.collectAsStateWithLifecycle()
    val selectedTree by viewModel.selectedTree.collectAsStateWithLifecycle()
    val selectedSeed by viewModel.currentTreeSeedVariant.collectAsStateWithLifecycle()

    LaunchedEffect(selectedTree, selectedSeed) {
        isVideoLoaded = false
        val remoteUrl = "${Constants.cdn}/video/${selectedTree?.id}_${selectedSeed}.webm"

        try {
            val localPath = getCachedVideoPath(remoteUrl, selectedTree?.id ?: "tree", selectedSeed)
            statePlayer.openUri(localPath)
            isVideoLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LaunchedEffect(crntStatus, isVideoLoaded, selectedMinutes) {
        if (!isVideoLoaded) return@LaunchedEffect

        if (crntStatus == TimerStatus.Running) {
            statePlayer.play()
            delay(600) // Let engine warm up

            val videoDurationMs = mmssToSeconds(statePlayer.durationText) * 1000
            val targetDurationMs = (selectedMinutes ?: 1) * 60_000L // e.g., 25 mins -> 1,500,000ms
            val stretchFactor = (targetDurationMs.toDouble() / videoDurationMs).coerceAtLeast(1.0)

            val playChunk = 50L
            // Formula: TotalStepTime = Play * Factor. Pause = Total - Play.
            val pauseChunk = ((playChunk * stretchFactor) - playChunk).toLong().coerceAtLeast(0L)

            println("TreeGrowth: Target=${selectedMinutes}m ($targetDurationMs ms). Factor=$stretchFactor. Play=$playChunk, Pause=$pauseChunk")

            while (isActive && crntStatus == TimerStatus.Running) {
                statePlayer.play()
                delay(playChunk)

                if (isActive && crntStatus == TimerStatus.Running && pauseChunk > 0) {
                    statePlayer.pause()
                    delay(pauseChunk)
                }
            }
        } else {
            statePlayer.pause()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // We render this DIRECTLY. No If-statements, no Animations.
        // It sits in the background persistently.
        PlatformVideoPlayer(
            statePlayer,
            Modifier.fillMaxSize()
        )

        val overlayState = remember(crntStatus, selectedTree, selectedSeed) {
            OverlayState(crntStatus, selectedTree, selectedSeed)
        }

        AnimatedContent(
            targetState = overlayState,
            transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
            modifier = Modifier.fillMaxSize(),
            label = "OverlayTransition"
        ) { state ->
            when (state.status) {
                TimerStatus.Running -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        BreathingGlow()
                    }
                }

                TimerStatus.POST_QUIT, TimerStatus.HAS_QUIT -> {
                    // Dead Tree Image (Opaque)
                    AsyncImage(
                        model = "${Constants.cdn}/images/weathered_0_grid.png",
                        contentDescription = "Withered",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        filterQuality = FilterQuality.High
                    )
                }

                else -> {
                    Box(Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = "${Constants.cdn}/images/${state.tree?.id}_0_grid.png",
                            contentDescription = "Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            filterQuality = FilterQuality.High
                        )

                        if (!isVideoLoaded) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreathingGlow() {
    val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, // Very subtle
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Canvas(modifier = Modifier.size(350.dp)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFF9C4).copy(alpha = glowAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = (size.minDimension / 2) * glowScale
            )
        )
    }
}