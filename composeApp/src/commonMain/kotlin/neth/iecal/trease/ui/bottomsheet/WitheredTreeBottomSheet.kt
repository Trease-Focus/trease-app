package neth.iecal.trease.ui.bottomsheet


import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import neth.iecal.trease.Constants
import neth.iecal.trease.models.FocusStats
import neth.iecal.trease.utils.TreeStatsLodger
import neth.iecal.trease.utils.getDate
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import trease.composeapp.generated.resources.Res
import trease.composeapp.generated.resources.baseline_info_outline_24

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun WitheredTreeSheet(
    onWitheredTreeSelected: (FocusStats) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var trees by remember { mutableStateOf<List<FocusStats>?>(null) }
    var selectedTree by remember { mutableStateOf<FocusStats?>(null) }
    
    LaunchedEffect(Unit) {
        val treeStatsLodger = TreeStatsLodger()
        trees = treeStatsLodger.getCache().filter { it.isFailed }
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
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
                WitheredTreeDetailView(
                    tree = tree,
                    onBack = { selectedTree = null },
                    onSelect = { 
                        onWitheredTreeSelected(selectedTree!!)
                        onDismissRequest()
                    }
                )
            } else {
                WitheredTreeGridView(
                    treeList = trees ?: emptyList(),
                    onTreeSelected = {
                        onWitheredTreeSelected(it)
                        onDismissRequest()
                    },
                    onTreeLongPress = { selectedTree = it }
                )
            }
        }
    }
}


@Composable
private fun WitheredTreeGridView(
    treeList: List<FocusStats>,
    onTreeSelected: (FocusStats) -> Unit,
    onTreeLongPress: (FocusStats) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select a Withered Tree",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tap to plant, hold to view details",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = treeList,
                key = { it.id }
            ) { tree ->
                WitheredTreeGridItem(tree, onTreeSelected, onTreeLongPress)
            }
            item {
                Icon(
                    painterResource(Res.drawable.baseline_info_outline_24), contentDescription = "Add new art",
                    modifier = Modifier.size(200.dp).combinedClickable(true, onClick =  {
                        uriHandler.openUri("https://github.com/Trease-Focus/trease-artwork")
                    }))
            }
        }
    }
}

@Composable
private fun WitheredTreeDetailView(
    tree: FocusStats,
    onBack: () -> Unit,
    onSelect: () -> Unit
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
                model = "${Constants.cdn}/images/${tree.treeId}_0_grid.png",
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(200.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,) {
                Text("${tree.duration / 60} mins", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Failed on: ${tree.completedOn.getDate()}",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
 
        Button(
            onClick = onSelect,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Grow This Tree")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WitheredTreeGridItem(
    tree: FocusStats,
    onClick: (FocusStats) -> Unit,
    onLongClick: (FocusStats) -> Unit
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
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = "${Constants.cdn}/images/${tree.treeId}_0.png",
                contentDescription = tree.treeId,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${tree.duration / 60} mins",
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

