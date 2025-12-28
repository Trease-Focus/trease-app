package neth.iecal.trease

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.BitmapImage
import coil3.Image

actual fun Image.toComposeImageBitmap(): ImageBitmap {
    if (this is BitmapImage) {
        return this.bitmap.asImageBitmap()
    } else {
        throw IllegalArgumentException("Image is not a BitmapImage. It might be a Vector or Drawable.")
    }
}