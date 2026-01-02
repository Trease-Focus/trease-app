package neth.iecal.trease.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import neth.iecal.trease.Constants
import neth.iecal.trease.toComposeImageBitmap
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt


const val SCALE = 4f
const val TILE_WIDTH = 100f * SCALE
const val TILE_HEIGHT = TILE_WIDTH / 2f
const val SOURCE_ANCHOR_Y_OFFSET = (150f * SCALE) + (TILE_WIDTH / 4f)

private data class PlacedTree(
    val id: String,
    val image: ImageBitmap,
    val drawX: Float,
    val drawY: Float
)

private data class ForestLayout(
    val trees: List<PlacedTree>,
    val totalWidth: Float,
    val totalHeight: Float
)

@Composable
fun IsometricForest(pngList: List<String>) {
    val context = LocalPlatformContext.current

    var forestLayout by remember { mutableStateOf<ForestLayout?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(pngList) {
        isLoading = true

        forestLayout = withContext(Dispatchers.Default) {
            if (pngList.isEmpty()) return@withContext null

            val gridSize = ceil(sqrt(pngList.size.toDouble())).toInt()

            val imageLoader = ImageLoader(context)

            val deferredLoad = pngList.mapIndexed { index, id ->
                async {
                    val gridX = index % gridSize
                    val gridY = index / gridSize
                    val url = "${Constants.cdn}/images/${id}"

                    try {
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .build()

                        val result = imageLoader.execute(request)
                        if (result is SuccessResult) {
                            // Using the extension function requested
                            val bitmap = result.image.toComposeImageBitmap()
                            return@async Triple(Pair(gridX, gridY), bitmap, id)
                        }
                    } catch (e: Exception) {
                        println("⚠️ Failed to load: $url")
                    }
                    return@async null
                }
            }

            val results = deferredLoad.awaitAll().filterNotNull()

            // Sort by Depth (Painter's Algorithm)
            val sortedResults = results.sortedWith(Comparator { a, b ->
                val (posA, _) = a
                val (posB, _) = b
                val depthA = posA.first + posA.second
                val depthB = posB.first + posB.second

                if (depthA != depthB) depthA - depthB else posA.first - posB.first
            })

            val isoWidth = (gridSize * 2) * (TILE_WIDTH / 2f)
            val isoHeight = gridSize * TILE_HEIGHT

            // Total virtual dimensions of the forest
            val totalWidth = isoWidth + (TILE_WIDTH * 2f)
            val totalHeight = isoHeight + (SOURCE_ANCHOR_Y_OFFSET * 2f)

            // Center point of the virtual canvas
            val originX = totalWidth / 2f
            val originY = 150f * SCALE

            // Create PlacedTree objects with exact coordinates
            val placements = sortedResults.map { (pos, bitmap, id) ->
                val (gridX, gridY) = pos

                // Isometric Projection
                val isoX = (gridX - gridY) * (TILE_WIDTH / 2f)
                val isoY = (gridX + gridY) * (TILE_WIDTH / 4f)

                val targetX = originX + isoX
                val targetY = originY + isoY + (TILE_WIDTH / 4f)

                // Adjust for image anchor
                val sourceAnchorX = bitmap.width / 2f
                val sourceAnchorY = SOURCE_ANCHOR_Y_OFFSET

                PlacedTree(
                    id = id,
                    image = bitmap,
                    drawX = targetX - sourceAnchorX,
                    drawY = targetY - sourceAnchorY
                )
            }

            ForestLayout(placements, totalWidth, totalHeight)
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            forestLayout?.let { layout ->
                // BoxWithConstraints gives us the actual screen size available
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val screenWidth = constraints.maxWidth.toFloat()
                    val screenHeight = constraints.maxHeight.toFloat()

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Calculate Scale to Fit
                        val scaleX = screenWidth / layout.totalWidth
                        val scaleY = screenHeight / layout.totalHeight

                        // Use the smaller scale to ensure it fits both dimensions (Fit Center)
                        val scale = min(scaleX, scaleY)

                        // Calculate Centering Offsets
                        val usedWidth = layout.totalWidth * scale
                        val usedHeight = layout.totalHeight * scale

                        val translateX = (screenWidth - usedWidth) / 2f
                        val translateY = (screenHeight - usedHeight) / 2f

                        withTransform({
                            translate(left = translateX, top = translateY)
                            scale(
                                scaleX = scale,
                                scaleY = scale,
                                pivot = Offset.Zero
                            )
                        }) {
                            layout.trees.forEach { tree ->
                                drawImage(
                                    image = tree.image,
                                    topLeft = Offset(tree.drawX, tree.drawY)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
