package neth.iecal.trease

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.github.kdroidfilter.composemediaplayer.linux.GStreamerInit

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Trease",
        state = WindowState(size = DpSize(450.dp, 900.dp))
    ) {
        System.setProperty("skiko.renderApi", "OPENGL")
        App()
    }
}