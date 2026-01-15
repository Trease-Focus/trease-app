package neth.iecal.trease.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import neth.iecal.trease.models.AppInfo
import neth.iecal.trease.utils.CacheManager
import neth.iecal.trease.utils.LinuxAppBlocker
import neth.iecal.trease.viewmodels.HomeScreenViewModel

@Composable
fun LinuxAppSelectionDialog(
    viewModel: HomeScreenViewModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelection by remember { mutableStateOf(emptySet<String>()) }
    var installedApps by remember { mutableStateOf(listOf<AppInfo>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

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

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            installedApps = LinuxAppBlocker.getInstance().getInstalledApps()
            isLoading = false

            val cacheManager = CacheManager()
            val rawSelection = cacheManager.readFile("selected-app-selection.txt")
            currentSelection = if(rawSelection==null)emptySet() else Json.decodeFromString<Set<String>>(rawSelection)

            installedApps = installedApps.sortedByDescending { currentSelection.contains (it.packageName ) }

        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Apps to Block",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...") },
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

                Text(
                    text = "Note that all the selected apps will be immediately killed. Please save all unsaved files before starting the app blocker.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
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
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(16.dp))
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

                        if (filteredApps.isEmpty() && !isLoading) {
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
                    if (currentSelection.isNotEmpty()) {
                        coroutineScope.launch {
                            val blocker = LinuxAppBlocker.getInstance()
                            val durationMillis = viewModel.remainingSeconds.value * 1_000L
                            blocker.startBlocking(currentSelection, durationMillis)

                            val cacheManager = neth.iecal.trease.interfaces.CacheManager()
                            cacheManager.saveFile("selected-app-selection.txt",Json.encodeToString(currentSelection))
                        }
                        onDismiss()
                        onConfirm()
                    }
                },
                enabled = currentSelection.isNotEmpty() && !isLoading
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
