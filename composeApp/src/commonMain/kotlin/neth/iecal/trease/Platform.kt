package neth.iecal.trease

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import neth.iecal.trease.interfaces.CacheManager
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import okio.FileSystem

enum class Platform {
    ANDROID,
    DESKTOP,
    IOS,
    WEB,
    UNKNOWN
}


expect fun getPlatform(): Platform

@Composable
expect fun PlatformVideoPlayer(state: VideoPlayerState, modifier: Modifier)
expect fun getCacheDir(): String
expect fun getPlatformHttpClient(): io.ktor.client.HttpClient
expect fun getFileSystem(): FileSystem

@Composable
expect fun FocusStarterDialog(viewModel: HomeScreenViewModel, onConfirm: () -> Unit = {}, onDismissed: () -> Unit = {})

expect fun onForceStopFocus()

expect fun getCacheManager(): CacheManager

expect fun isBlockerRunning():Boolean