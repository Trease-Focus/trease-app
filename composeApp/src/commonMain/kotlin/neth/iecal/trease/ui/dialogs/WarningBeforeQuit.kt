package neth.iecal.trease.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import org.jetbrains.compose.resources.painterResource
import trease.composeapp.generated.resources.Res
import trease.composeapp.generated.resources.outline_arrow_forward_ios_24
import trease.composeapp.generated.resources.rounded_clock_loader_10_24

@Composable
fun WarningBeforeQuit(
    viewModel: HomeScreenViewModel,
    onDismissed: () -> Unit,
    onQuitConfirmed: () -> Unit
) {
    val timeRemaining by viewModel.remainingSeconds.collectAsState()
    val goalFocus by viewModel.selectedMinutes.collectAsState()

    // Logic to calculate elapsed time
    val elapsedSeconds = (goalFocus * 60L) - timeRemaining
    val elapsedFormatted = viewModel.formatTime(elapsedSeconds)

    // Validation State
    var typeText by remember { mutableStateOf("") }
    val quitterText by remember { mutableStateOf("I am nub") }
    val isMatch = typeText == quitterText

    Dialog(onDismissRequest = { onDismissed() }) {
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
                    text = "The tree will wither...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WarningStatItem(
                        value = elapsedFormatted,
                        label = "Focused",
                        painterResource(Res.drawable.rounded_clock_loader_10_24)
                    )
                    WarningStatItem(
                        value = "${goalFocus}m",
                        label = "Goal",
                        painterResource(Res.drawable.rounded_clock_loader_10_24)
                    )
                }

                Text(
                    text = buildAnnotatedString {
                        append("To confirm giving up, type \"")
                        withStyle(style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )) {
                            append(quitterText)
                        }
                        append("\" below.")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = typeText,
                    onValueChange = { typeText = it },
                    placeholder = { Text(quitterText, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onDismissed() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Resume")
                    }

                    TextButton(
                        onClick = {
                            onQuitConfirmed()
                            onDismissed()
                        },
                        enabled = isMatch,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                            disabledContentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        )
                    ) {
                        Text("Give Up")
                    }
                }
            }
        }
    }
}

@Composable
private fun WarningStatItem(
    value: String,
    label: String,
    painter: Painter
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}