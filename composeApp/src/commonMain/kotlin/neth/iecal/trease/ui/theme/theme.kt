package neth.iecal.trease.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZenLightColorScheme = lightColorScheme(
    // The Sky
    background = ZenColors.SkyBlue,
    onBackground = ZenColors.TextBlack,

    // The Floating Island (Top layer)
    primary = ZenColors.GrassGreen,
    onPrimary = ZenColors.TextBlack,

    // The Floating Island (Dirt layer)
    secondary = ZenColors.DirtBrown,
    onSecondary = ZenColors.CloudWhite,

    // The Clouds
    surface = ZenColors.CloudWhite,
    onSurface = ZenColors.TextBlack,

    // The Progress Bar & Secondary Text
    surfaceVariant = ZenColors.CloudWhite.copy(alpha = 0.5f), // Transparent track
    onSurfaceVariant = ZenColors.TextGrey, // Quote text color
)
@Composable
fun ZenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = ZenLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}