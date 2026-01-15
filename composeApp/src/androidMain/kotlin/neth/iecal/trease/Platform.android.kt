package neth.iecal.trease

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.github.kdroidfilter.composemediaplayer.SurfaceType
import io.github.kdroidfilter.composemediaplayer.VideoPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import neth.iecal.trease.interfaces.CacheManager
import neth.iecal.trease.ui.dialogs.AppSelectionDialog
import neth.iecal.trease.utils.AppBlockerManager
import neth.iecal.trease.utils.DefaultCacheManager
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import okio.FileSystem
import java.io.File

@SuppressLint("StaticFieldLeak")
object TreaseContext {
    private var _context: Context? = null
    val context: Context
        get() = _context ?: throw IllegalStateException(
            "Android context not initialized. Call TreaseContext.init(context) in your Application class."
        )

    fun init(context: Context) {
        _context = context.applicationContext
    }
}


actual fun getPlatform(): Platform = Platform.ANDROID

actual fun getCacheDir(): String {
    val cacheDir = File(TreaseContext.context.cacheDir, "trease_cache")
    if (!cacheDir.exists()) {
        cacheDir.mkdirs()
    }
    return cacheDir.absolutePath
}

actual fun getPlatformHttpClient(): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
            }
        }
    }
}

actual fun getFileSystem(): FileSystem {
    return FileSystem.SYSTEM
}

@Composable
actual fun PlatformVideoPlayer(state: VideoPlayerState, modifier: Modifier) {
    VideoPlayerSurface(
        state ,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        surfaceType = SurfaceType.TextureView
    )
}

@Composable
actual fun FocusStarterDialog(
    viewModel: HomeScreenViewModel,
    onConfirm: () -> Unit,
    onDismissed: () -> Unit
) {
    AppSelectionDialog(viewModel, onConfirm = onConfirm,onDismissed)
}

actual fun onForceStopFocus() {
    val appBlockerManager = AppBlockerManager(TreaseContext.context.applicationContext)
    appBlockerManager.stopService()
}

actual fun getCacheManager(): CacheManager {
    return DefaultCacheManager()
}