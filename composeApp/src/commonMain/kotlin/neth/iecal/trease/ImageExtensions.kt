package neth.iecal.trease

import androidx.compose.ui.graphics.ImageBitmap
import coil3.Image

/**
 * Converts a Coil 3 platform-agnostic Image into a Compose ImageBitmap.
 * Throws IllegalArgumentException if the image is not a BitmapImage (e.g. SVGs/Vectors).
 */
expect fun Image.toComposeImageBitmap(): ImageBitmap