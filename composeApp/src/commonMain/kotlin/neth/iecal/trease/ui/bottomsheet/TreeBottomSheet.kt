package neth.iecal.trease.ui.bottomsheet
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import neth.iecal.trease.Constants
import neth.iecal.trease.api.TreeRepository
import neth.iecal.trease.models.TreeData
import neth.iecal.trease.models.TreeUiState
import neth.iecal.trease.utils.CacheManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeBottomSheet(onDismiss: () -> Unit,onSelected: (TreeData) -> Unit) {
    var uiState by remember { mutableStateOf<TreeUiState>(TreeUiState.Loading) }

    LaunchedEffect(Unit) {
        val cacheManager = CacheManager()
        val cache = cacheManager.readFile("tree.json")
        if(cache!=null) {
            uiState = TreeUiState.Success(
                Json.decodeFromString(cache)
            )
        }
         try {
            val trees = TreeRepository.fetchTrees()
            uiState = TreeUiState.Success(trees)
            cacheManager.saveFile("tree.json",Json.encodeToString(trees))
        } catch (e: Exception) {
            uiState = TreeUiState.Error("Failed to load trees $e")
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp)
        ) {
            if (uiState is TreeUiState.Success) {
                items((uiState as TreeUiState.Success).trees) { treeId ->
                    TreeItem(treeId,onSelected)
                }
            }
        }
    }
}

@Composable
fun TreeItem(treeId: TreeData, onSelected: (TreeData) -> Unit) {
    Column(
        modifier = Modifier.padding(8.dp).combinedClickable(true,onClick = { onSelected(treeId) }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val treeUrl =  "${Constants.cdn}/images/${treeId.id}_${treeId.variants-1}.png"
        print("Displaying tree $treeUrl")
        AsyncImage(
            model =treeUrl,
            contentDescription = treeId.name,
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
        Text(treeId.name, style = MaterialTheme.typography.bodySmall)
    }
}