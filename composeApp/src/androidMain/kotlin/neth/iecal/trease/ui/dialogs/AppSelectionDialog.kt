package neth.iecal.trease.ui.dialogs

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import neth.iecal.trease.utils.AppBlockerManager
import neth.iecal.trease.utils.reloadApps
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import neth.iecal.trease.data.AppInfo

@Composable
fun AppSelectionDialog(
    viewModel: HomeScreenViewModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelection by remember { mutableStateOf(emptySet<String>()) }

    var installedApps by remember { mutableStateOf(listOf<AppInfo>()) }
    var searchQuery by remember { mutableStateOf("") }

    var isLoading by remember {mutableStateOf(true)}
    val filteredApps by remember(searchQuery, installedApps) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                installedApps
            } else {
                installedApps.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.packageName.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var icons by remember { mutableStateOf(mapOf<String, Drawable>()) }
    LaunchedEffect(Unit) {
        val appBlockerManager  = AppBlockerManager(context)
        currentSelection = appBlockerManager.getAllowedPackagesCache()

        val appResult = reloadApps(context.packageManager,context)
        if(appResult.isSuccess) {
            installedApps = appResult.getOrDefault(emptyList())
        }
        installedApps = installedApps.sortedByDescending { currentSelection.contains (it.packageName ) }

        val pm = context.packageManager

        val tempMap = mutableMapOf<String, Drawable>()
        for (info in installedApps) {
            try {
                val icon = try {
                    pm.getApplicationIcon(info.packageName)
                } catch (e: Exception) {
                    pm.defaultActivityIcon
                }
                tempMap[info.packageName] = icon
            }catch (e:Exception){

            }
        }
        icons = tempMap
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Allowed Apps",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search packages...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isSelected = currentSelection.contains(app.packageName)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable {
                                        currentSelection = if (isSelected) {
                                            currentSelection - app.packageName
                                        } else {
                                            currentSelection + app.packageName
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null // Handled by Row clickable for better UX
                                )
                                Spacer(modifier = Modifier.padding(8.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(icons[app.packageName]),
                                    contentDescription = "App Icon",
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.padding(8.dp))
                                Column {

                                    Text(
                                        text = app.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (filteredApps.isEmpty()) {
                            item {
                                Text(
                                    text = "No apps found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val appBlockManager = AppBlockerManager(context)
                        appBlockManager.saveAllowedPackages(currentSelection)
                        appBlockManager.startAppBlockerService(currentSelection,viewModel.remainingSeconds.value * 1_000)
                    }
                    onDismiss()
                    onConfirm()
                }
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}