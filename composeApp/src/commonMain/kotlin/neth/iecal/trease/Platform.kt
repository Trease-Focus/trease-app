package neth.iecal.trease

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
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