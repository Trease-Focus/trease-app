package neth.iecal.trease

// jsMain/kotlin/YourPackage/Platform.js.kt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
// Ensure you have appropriate imports for your VideoPlayerState
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import neth.iecal.trease.interfaces.CacheManager
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import okio.FileSystem
import okio.fakefilesystem.FakeFileSystem

actual fun getPlatform(): Platform = Platform.WEB

actual fun getCacheDir(): String {
    // Browsers don't have traditional file paths.
    // Since we are using a virtual file system (see below),
    // we return a virtual root path.
    return "/trease_cache"
}

actual fun getPlatformHttpClient(): HttpClient {
    // OkHttp is JVM-only. The Js engine uses the browser's fetch API.
    return HttpClient(Js)
}

actual fun getFileSystem(): FileSystem {
    return FakeFileSystem()
}

@Composable
actual fun PlatformVideoPlayer(
    state: VideoPlayerState,
    modifier: Modifier
) {
    VideoPlayerSurface(
        state ,
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

@Composable
actual fun FocusStarterDialog(
    viewModel: HomeScreenViewModel,
    onConfirm: () -> Unit,
    onDismissed: () -> Unit
) {
    LaunchedEffect(Unit){
        onConfirm()
    }
}

actual fun onForceStopFocus() {
}

actual fun getCacheManager(): CacheManager {
    val cacheManager = WebCacheManager()
    return cacheManager
}