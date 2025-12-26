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
import neth.iecal.trease.api.TreeRepository
import neth.iecal.trease.models.TreeUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeBottomSheet(onDismiss: () -> Unit,onSelected: (String) -> Unit) {
    var uiState by remember { mutableStateOf<TreeUiState>(TreeUiState.Loading) }

    LaunchedEffect(Unit) {
        uiState = try {
            TreeUiState.Success(TreeRepository.fetchTrees())
        } catch (e: Exception) {
            TreeUiState.Error("Failed to load trees")
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
fun TreeItem(treeId: String,onSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(8.dp).combinedClickable(true,onClick = { onSelected(treeId) }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = "https://trease-focus.github.io/cache-trees/images/$treeId.png",
            contentDescription = treeId,
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
        Text(treeId, style = MaterialTheme.typography.bodySmall)
    }
}