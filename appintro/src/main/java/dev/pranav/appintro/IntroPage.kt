package dev.pranav.appintro

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a single page in the app intro
 *
 * @param title The title of the intro page
 * @param description The description text for the page
 * @param icon Optional icon to display (can be null)
 * @param backgroundColor Optional background color for the page (defaults to primary container color)
 * @param contentColor Optional color for text content (defaults to on primary container color)
 * @param illustration Optional composable for a custom illustration
 * @param customContent Optional composable for completely custom page content, replaces default layout when provided
 * @param onNext Optional callback when user moves to the next page from this page (returns true to proceed, false to prevent navigation)
 */
data class IntroPage(
    val title: String,
    val description: String,
    val icon: ImageVector? = null,
    val backgroundColor: Color? = null,
    val contentColor: Color? = null,
    val illustration: (@Composable () -> Unit)? = null,
    val customContent: (@Composable () -> Unit)? = null,
    val onNext: (() -> Boolean)? = null,
    val painter: Painter? = null
)
