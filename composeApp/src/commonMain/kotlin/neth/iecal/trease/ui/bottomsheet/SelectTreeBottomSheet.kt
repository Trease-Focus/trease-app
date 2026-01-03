package neth.iecal.trease.ui.bottomsheet

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import neth.iecal.trease.Constants
import neth.iecal.trease.api.TreeRepository
import neth.iecal.trease.models.TreeData
import neth.iecal.trease.models.TreeUiState
import neth.iecal.trease.utils.CacheManager
import neth.iecal.trease.utils.TreePurchaseManager
import neth.iecal.trease.viewmodels.HomeScreenViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun GrowTreeBottomSheet(
    onDismiss: () -> Unit,
    onAddToCanvas: (TreeData) -> Unit,
    onShowWitheredTrees:()->Unit,
    viewModel: HomeScreenViewModel,
) {
    var uiState by remember { mutableStateOf<TreeUiState>(TreeUiState.Loading) }
    var selectedTree by remember { mutableStateOf<TreeData?>(null) }

    var purchasedTrees by remember { mutableStateOf(emptyList<String>()) }
    val coroutine = rememberCoroutineScope()
    var showBrokeDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val treePurchaseManager = TreePurchaseManager()
        purchasedTrees = treePurchaseManager.loadAllPurchasedTrees()

        val cacheManager = CacheManager()
        cacheManager.readFile("tree.json")?.let {
            uiState = TreeUiState.Success(Json.decodeFromString(it))
        }

        try {
            val trees = TreeRepository.fetchTrees()
            uiState = TreeUiState.Success(trees)
            cacheManager.saveFile("tree.json", Json.encodeToString(trees))
        } catch (e: Exception) {
            if (uiState !is TreeUiState.Success) {
                uiState = TreeUiState.Error("Unable to load forest: ${e.message}")
            }
        }
    }

    if(showBrokeDialog) {
        Dialog(onDismissRequest = { showBrokeDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Too broke to buy this tree!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = { showBrokeDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Close",
                            modifier = Modifier.padding(vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        AnimatedContent(
            targetState = selectedTree,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it / 2 } + fadeOut()
                } else {
                    slideInHorizontally { -it / 2 } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                }.using(SizeTransform(clip = false))
            },
            label = "TreeSheetContent"
        ) { tree ->
            if (tree != null) {
                TreeDetailView(
                    tree = tree,
                    onBack = { selectedTree = null },
                    onSelect = { onAddToCanvas(tree) },
                    isPurchased = purchasedTrees.contains(tree.id),
                    onPurchased = {
                        if(viewModel.coins.value >= tree.basePrice){
                            val tempTree = purchasedTrees.toMutableList()
                            tempTree.add(tree.id)
                            purchasedTrees = tempTree.toList()
                            coroutine.launch {
                                val purchaseManager = TreePurchaseManager()
                                purchaseManager.addTree(tree.id)
                                viewModel.useCoins(tree.basePrice)
                                onAddToCanvas(tree)
                            }
                        }else{
                            showBrokeDialog = true
                        }
                    }
                )
            } else {
                TreeGridView(
                    uiState = uiState,
                    onTreeSelected = {
                        if(purchasedTrees.contains(it.id)) onAddToCanvas(it) else  selectedTree = it
                                     },
                    onTreeLongPress = { selectedTree = it },
                    onShowWitheredTrees = onShowWitheredTrees,
                    purchasedTrees = purchasedTrees
                )
            }
        }
    }
}


@Composable
private fun TreeGridView(
    uiState: TreeUiState,
    onTreeSelected: (TreeData) -> Unit,
    onTreeLongPress: (TreeData) -> Unit,
    onShowWitheredTrees: () -> Unit,
    purchasedTrees: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select a Tree",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tap to plant, hold to view details",
            style = MaterialTheme.typography.bodySmall,
        )


        Spacer(modifier = Modifier.height(8.dp))

        SuggestionChip(
            onClick = {onShowWitheredTrees() },
            label = { Text("Plant a Withered Tree") },
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is TreeUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            }
            is TreeUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
            }
            is TreeUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.trees.filter { it.isGrowable },
                        key = { it.id }
                    ) { tree ->
                        TreeGridItem(tree, onTreeSelected, onTreeLongPress,purchasedTrees.contains(tree.id))
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeDetailView(
    tree: TreeData,
    onBack: () -> Unit,
    onSelect: () -> Unit,
    isPurchased: Boolean,
    onPurchased: ()->Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        SuggestionChip(
            onClick = {onBack() },
            label = { Text("Go Back") },
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp),) {
            AsyncImage(
                model = "${Constants.cdn}/images/${tree.id}_${tree.variants - 1}_grid.png",
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(200.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,) {
                Text(tree.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "By ${tree.creator}",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = tree.description,
            style = MaterialTheme.typography.bodyMedium,
        )
        if(!isPurchased) {
            Text(
                text = "Price: ${tree.basePrice}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Tree Variants", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tree.variants) { index ->
                VariantItem(tree.id, index, isRare = index == tree.variants-1)
            }
        }

        Spacer(Modifier.height(32.dp))

        if(isPurchased) {
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Select This Tree")
            }
        }else{
            Button(
                onClick = onPurchased,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Buy Tree")
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TreeGridItem(
    tree: TreeData,
    onClick: (TreeData) -> Unit,
    onLongClick: (TreeData) -> Unit,
    isPurchased: Boolean
) {
    val haptic = LocalHapticFeedback.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { onClick(tree) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick(tree)
                }
            )
            .padding(8.dp)
    ) {
        // Image Container
        Box(
            modifier = Modifier
                .size(90.dp)
                .padding(8.dp)
                .alpha(if (isPurchased) 1f else 0.3f),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "${Constants.cdn}/images/${tree.id}_${tree.variants - 1}.png",
                contentDescription = tree.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = tree.name,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VariantItem(treeId: String, variantIndex: Int,isRare: Boolean ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "${Constants.cdn}/images/${treeId}_${variantIndex}.png",
                contentDescription = "Stage $variantIndex",
                modifier = Modifier.padding(4.dp).fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "#${variantIndex + 1}",
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = if (isRare) "Rare" else " ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}