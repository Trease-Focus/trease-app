package neth.iecal.trease.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import neth.iecal.trease.ui.theme.ZenColors

@Composable
fun IsometricGrid() {
    // We use a Box to stack the Tree on top of the Grid
    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. The Isometric Block (Background)
        Canvas(modifier = Modifier.size(200.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val cy = h / 2

            val blockSize = 50f

            // Top Face (Grass)
            val topPath = Path().apply {
                moveTo(cx, cy - blockSize)
                lineTo(cx + blockSize * 1.7f, cy)
                lineTo(cx, cy + blockSize)
                lineTo(cx - blockSize * 1.7f, cy)
                close()
            }
            drawPath(topPath, color = ZenColors.GrassGreen)

            // Right Face (Dark Dirt)
            val rightPath = Path().apply {
                moveTo(cx + blockSize * 1.7f, cy)
                lineTo(cx + blockSize * 1.7f, cy + blockSize * 1.5f)
                lineTo(cx, cy + blockSize * 2.5f)
                lineTo(cx, cy + blockSize)
                close()
            }
            drawPath(rightPath, color = ZenColors.DarkDirtBrown)

            // Left Face (Lighter Dirt)
            val leftPath = Path().apply {
                moveTo(cx - blockSize * 1.7f, cy)
                lineTo(cx - blockSize * 1.7f, cy + blockSize * 1.5f)
                lineTo(cx, cy + blockSize * 2.5f)
                lineTo(cx, cy + blockSize)
                close()
            }
            drawPath(leftPath, color = ZenColors.DirtBrown)
        }

        // 2. The Tree (Foreground)
        AsyncImage(
            model = "https://raw.githubusercontent.com/Trease-Focus/trease-backend/refs/heads/main/samples/maple2.png",
            contentDescription = "Tree",
            modifier = Modifier
                .size(180.dp) // Adjust size to fit the block
                .offset(y = (-60).dp), // Move the tree UP so the trunk sits on the grass
            contentScale = ContentScale.Fit
        )
    }
}