package neth.iecal.trease.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import neth.iecal.trease.toComposeImageBitmap
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

// --- Configuration ---
const val SCALE = 4f
const val TILE_WIDTH = 100f * SCALE
const val TILE_HEIGHT = TILE_WIDTH / 2f
const val SOURCE_ANCHOR_Y_OFFSET = (150f * SCALE) + (TILE_WIDTH / 4f)
const val LIFT_HEIGHT = 80f // Height the tree rises
const val HOVER_SCALE = 1.05f // Slight zoom when lifted for natural feel

data class PlacedTree(
    val index: Int,
    val id: String,
    val image: ImageBitmap,
    val drawX: Float,
    val drawY: Float,
    val width: Int,
    val height: Int,
    // Pre-calculate center base for hit testing/shadows
    val centerX: Float = drawX + (width / 2f),
    val centerY: Float = drawY + SOURCE_ANCHOR_Y_OFFSET
)

data class ForestLayout(
    val trees: List<PlacedTree>,
    val totalWidth: Float,
    val totalHeight: Float
)

@Composable
fun IsometricForest(treeIds: List<String>) {
    val context = LocalPlatformContext.current
    val scope = rememberCoroutineScope()

    // --- State ---
    var forestLayout by remember { mutableStateOf<ForestLayout?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTreeIndex by remember { mutableStateOf<Int?>(null) }

    // Animation states mapped by unique tree index
    val treeAnimatables = remember { mutableStateMapOf<Int, Animatable<Float, AnimationVector1D>>() }

    fun getAnimatable(index: Int): Animatable<Float, AnimationVector1D> {
        return treeAnimatables.getOrPut(index) { Animatable(0f) }
    }

    // --- Logic Function: Lift Controller ---
    // Replaces the inline click listener logic
    fun toggleTreeLift(targetIndex: Int) {
        scope.launch {
            val prevIndex = selectedTreeIndex

            if (prevIndex == targetIndex) {
                // Case A: Clicking the already lifted tree -> Drop it
                selectedTreeIndex = null
                launch {
                    getAnimatable(targetIndex).animateTo(
                        targetValue = 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                }
            } else {
                // Case B: Clicking a new tree
                // 1. Drop the previous one (if any)
                if (prevIndex != null) {
                    launch {
                        getAnimatable(prevIndex).animateTo(
                            targetValue = 0f,
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                    }
                }

                // 2. Lift the new one
                selectedTreeIndex = targetIndex
                launch {
                    getAnimatable(targetIndex).animateTo(
                        targetValue = -LIFT_HEIGHT,
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                }
            }
        }
    }

    fun clearSelection() {
        val currentIndex = selectedTreeIndex ?: return
        selectedTreeIndex = null
        scope.launch {
            getAnimatable(currentIndex).animateTo(0f, spring(stiffness = Spring.StiffnessLow))
        }
    }

    // --- Load Logic ---
    LaunchedEffect(treeIds) {
        isLoading = true
        forestLayout = withContext(Dispatchers.Default) {
            if (treeIds.isEmpty()) return@withContext null

            val gridSize = ceil(sqrt(treeIds.size.toDouble())).toInt()
            val imageLoader = ImageLoader(context)

            val deferredLoad = treeIds.mapIndexed { index, id ->
                async {
                    val gridX = index % gridSize
                    val gridY = index / gridSize
                    val url = "https://trease-focus.github.io/cache-trees/images/${id}_grid.png"

                    try {
                        val request = ImageRequest.Builder(context).data(url).build()
                        val result = imageLoader.execute(request)
                        if (result is SuccessResult) {
                            val bitmap = result.image.toComposeImageBitmap()
                            Triple(index, Pair(gridX, gridY), bitmap)
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            val results = deferredLoad.awaitAll().filterNotNull()

            // Sort for Painter's Algorithm (Back to Front)
            val sortedResults = results.sortedWith(Comparator { a, b ->
                val (_, posA, _) = a
                val (_, posB, _) = b
                val depthA = posA.first + posA.second
                val depthB = posB.first + posB.second
                if (depthA != depthB) depthA - depthB else posA.first - posB.first
            })

            // Calculate Dimensions
            val isoWidth = (gridSize * 2) * (TILE_WIDTH / 2f)
            val isoHeight = gridSize * TILE_HEIGHT
            val totalWidth = isoWidth + (TILE_WIDTH * 2f)
            val totalHeight = isoHeight + (SOURCE_ANCHOR_Y_OFFSET * 2f)
            val originX = totalWidth / 2f
            val originY = 150f * SCALE

            val placements = sortedResults.map { (originalIndex, pos, bitmap) ->
                val (gridX, gridY) = pos
                val isoX = (gridX - gridY) * (TILE_WIDTH / 2f)
                val isoY = (gridX + gridY) * (TILE_WIDTH / 4f)

                val targetX = originX + isoX
                val targetY = originY + isoY + (TILE_WIDTH / 4f)
                val sourceAnchorX = bitmap.width / 2f
                val sourceAnchorY = SOURCE_ANCHOR_Y_OFFSET

                PlacedTree(
                    index = originalIndex,
                    id = treeIds[originalIndex],
                    image = bitmap,
                    drawX = targetX - sourceAnchorX,
                    drawY = targetY - sourceAnchorY,
                    width = bitmap.width,
                    height = bitmap.height
                )
            }
            ForestLayout(placements, totalWidth, totalHeight)
        }
        isLoading = false
    }

    // --- Rendering ---
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            forestLayout?.let { layout ->
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val screenWidth = constraints.maxWidth.toFloat()
                    val screenHeight = constraints.maxHeight.toFloat()

                    // Calculate Scale to fit forest in screen
                    val scaleX = screenWidth / layout.totalWidth
                    val scaleY = screenHeight / layout.totalHeight
                    val globalScale = min(scaleX, scaleY)

                    val usedWidth = layout.totalWidth * globalScale
                    val usedHeight = layout.totalHeight * globalScale
                    val translateX = (screenWidth - usedWidth) / 2f
                    val translateY = (screenHeight - usedHeight) / 2f

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            // CRITICAL: Keys added to ensure touch rects update on resize
                            .pointerInput(layout, globalScale, translateX, translateY) {
                                detectTapGestures { tapOffset ->
                                    // 1. Transform Touch to Virtual Space
                                    val virtualX = (tapOffset.x - translateX) / globalScale
                                    val virtualY = (tapOffset.y - translateY) / globalScale

                                    // 2. Hit Test (Reverse order to hit top-most rendered tree first)
                                    val clickedTree = layout.trees.asReversed().firstOrNull { tree ->
                                        val offset = treeAnimatables[tree.index]?.value ?: 0f
                                        val left = tree.drawX
                                        val top = tree.drawY + offset
                                        val right = left + tree.width
                                        val bottom = top + tree.height

                                        // Accurate Hit Box (could use pixel check here for precision)
                                        virtualX in left..right && virtualY in top..bottom
                                    }

                                    // 3. Delegate to Lift Function
                                    if (clickedTree != null) {
                                        toggleTreeLift(clickedTree.index)
                                    } else {
                                        clearSelection()
                                    }
                                }
                            }
                    ) {
                        // All drawing must happen inside this transform to stay aligned
                        withTransform({
                            translate(left = translateX, top = translateY)
                            scale(globalScale, globalScale, pivot = Offset.Zero)
                        }) {

                            // A. Draw Unselected Trees
                            layout.trees.forEach { tree ->
                                if (tree.index != selectedTreeIndex) {
                                    val offset = treeAnimatables[tree.index]?.value ?: 0f
                                    drawImage(
                                        image = tree.image,
                                        topLeft = Offset(tree.drawX, tree.drawY + offset)
                                    )
                                }
                            }

                            // B. Draw Selected Tree (Last = On Top)
                            selectedTreeIndex?.let { selectedIndex ->
                                val tree = layout.trees.find { it.index == selectedIndex }
                                if (tree != null) {
                                    val offset = treeAnimatables[tree.index]?.value ?: 0f

                                    // Draw Shadow to anchor the tree visually to the ground
                                    // This fixes the "positions don't seem accurate" feeling
                                    drawOval(
                                        color = Color.Black.copy(alpha = 0.2f),
                                        topLeft = Offset(tree.centerX - 40f, tree.centerY - 15f),
                                        size = Size(80f, 30f)
                                    )

                                    // Draw the Lifted Tree
                                    // Optional: Slight scale up to simulate "closer to camera"
                                    val liftScale = if (offset < -1f) HOVER_SCALE else 1f
                                    val widthChange = (tree.width * liftScale - tree.width) / 2f
                                    val heightChange = (tree.height * liftScale - tree.height)

                                    drawImage(
                                        image = tree.image,
                                        topLeft = Offset(
                                            tree.drawX - widthChange,
                                            tree.drawY + offset - heightChange
                                        ),
                                        // If you want actual scaling, use dstSize, or another withTransform
                                        // But keeping it simple is often smoother:
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}