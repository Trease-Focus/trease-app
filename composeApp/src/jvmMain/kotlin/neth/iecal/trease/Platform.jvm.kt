package neth.iecal.trease

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okio.FileSystem
import java.io.File


actual fun getPlatform(): Platform = Platform.DESKTOP

actual fun getCacheDir(): String {
    val osTempDir = System.getProperty("java.io.tmpdir")
    val cacheDir = File(osTempDir, "trease_cache")
    if (!cacheDir.exists()) {
        cacheDir.mkdirs()
    }
    return cacheDir.absolutePath
}

actual fun getPlatformHttpClient(): HttpClient {
    return HttpClient(OkHttp)
}
actual fun getFileSystem(): FileSystem {
    return FileSystem.SYSTEM
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