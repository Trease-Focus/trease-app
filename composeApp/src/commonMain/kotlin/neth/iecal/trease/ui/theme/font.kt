package neth.iecal.trease.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import trease.composeapp.generated.resources.Res
import trease.composeapp.generated.resources.inter_black
import trease.composeapp.generated.resources.inter_bold
import trease.composeapp.generated.resources.inter_extra_light
import trease.composeapp.generated.resources.inter_regular

@Composable
fun fonts(): FontFamily {
    val fontFamily = FontFamily(
        Font(Res.font.inter_regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.inter_bold, FontWeight.Bold, FontStyle.Normal),
        Font(Res.font.inter_black, FontWeight.Black, FontStyle.Normal),
        Font(Res.font.inter_extra_light, FontWeight.ExtraLight, FontStyle.Normal),
    )
    return fontFamily
}

@Composable
fun fontFamily() = Typography().run {

    val fontFamily = fonts()
    copy(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily =  fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily)
    )
}