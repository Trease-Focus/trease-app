package neth.iecal.trease.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val BlackAndWhiteScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = PureWhite,
    primaryContainer = PureWhite,
    onPrimaryContainer = PureBlack,

    secondary = PureBlack,
    onSecondary = PureWhite,
    secondaryContainer = LightGray,
    onSecondaryContainer = PureBlack,

    tertiary = PureBlack,
    onTertiary = PureWhite,
    tertiaryContainer = LightGray,
    onTertiaryContainer = PureBlack,

    background = PureWhite,
    onBackground = PureBlack,
    surface = PureWhite,
    onSurface = PureBlack,

    surfaceVariant = LightGray,
    onSurfaceVariant = PureBlack,

    outline = PureBlack,
    outlineVariant = MediumGray,

    error = Color(0xFFB00020),
    onError = PureWhite,
    errorContainer = LightGray,
    onErrorContainer = PureBlack
)
@Composable
fun MainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = BlackAndWhiteScheme

    MaterialTheme(
        typography = fontFamily(),
        colorScheme = colorScheme,
        content = content
    )
}