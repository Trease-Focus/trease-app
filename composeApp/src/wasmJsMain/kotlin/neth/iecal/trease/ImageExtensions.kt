package neth.iecal.trease

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import coil3.BitmapImage
import coil3.Image

actual fun Image.toComposeImageBitmap(): ImageBitmap {
    if (this is BitmapImage) {
        // Coil 3 on non-Android uses org.jetbrains.skia.Bitmap
        return this.bitmap.asComposeImageBitmap()
    } else {
        throw IllegalArgumentException("Image is not a BitmapImage.")
    }
}