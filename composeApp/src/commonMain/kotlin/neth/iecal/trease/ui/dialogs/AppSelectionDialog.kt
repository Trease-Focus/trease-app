package neth.iecal.trease.ui.dialogs

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import nethical.questphone.data.AppInfo


@Composable
fun AppSelectionDialog(
    installedApps: List<AppInfo>,
    selectedApps: Set<String>,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
    onSelectionChange: (Set<String>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Allowed Apps") },
        text = {
            LazyColumn {
                items(installedApps) { app ->
                    androidx.compose.foundation.layout.Row {
                        Checkbox(
                            checked = selectedApps.contains(app.packageName),
                            onCheckedChange = { checked ->
                                val newSelected = if (checked) {
                                    selectedApps + app.packageName
                                } else {
                                    selectedApps - app.packageName
                                }
                                onSelectionChange(newSelected)
                            }
                        )
                        Text(app.name)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedApps) }) {
                Text("Start")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
