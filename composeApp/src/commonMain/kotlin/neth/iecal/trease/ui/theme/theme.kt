package neth.iecal.trease.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZenLightColorScheme = lightColorScheme(
    // The Sky - Pure or off-white for maximum breathability
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1A1A), // Deep charcoal for soft contrast

    // The Floating Island - Minimalist accent (e.g., a subtle light grey)
    primary = Color(0xFF1A1A1A),
    onPrimary = Color(0xFFF2F2F2),

    // The Floating Island (Dirt layer) - Swapped for a soft neutral
    secondary = Color(0xFFEBEBEB),
    onSecondary = Color(0xFF4A4A4A),

    // The Clouds - Pure white for elevation
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),

    // The Progress Bar & Secondary Text
    surfaceVariant = Color(0xFFF5F5F5), // Very light track
    onSurfaceVariant = Color(0xFF8E8E8E), // Muted grey for metadata
)
@Composable
fun ZenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = ZenLightColorScheme

    MaterialTheme(
        typography = fontFamily(),
        colorScheme = colorScheme,
        content = content
    )
}