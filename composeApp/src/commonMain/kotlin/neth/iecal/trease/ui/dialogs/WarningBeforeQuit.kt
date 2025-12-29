package neth.iecal.trease.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import neth.iecal.trease.models.TimerStatus
import neth.iecal.trease.viewmodels.HomeScreenViewModel


@Composable
fun WarningBeforeQuit(viewModel: HomeScreenViewModel,onDismissed:()->Unit,onQuitConfirmed: () -> Unit) {
    val timeRemaining by viewModel.remainingSeconds.collectAsState()
    val goalFocus by viewModel.selectedMinutes.collectAsState()

    var typeText by remember { mutableStateOf("") }
    var quitterText by remember { mutableStateOf("I am nub") }
    Dialog(
        onDismissRequest = {
            onDismissed()
        }
    ){
        Surface(modifier = Modifier.wrapContentSize().verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {

                Text("The tree will wither..", style = MaterialTheme.typography.headlineLarge,textAlign = TextAlign.Center)

                AsyncImage(
                    model = "https://trease-focus.github.io/cache-trees/images/${viewModel.selectedTree.value}_grid.png",
                    contentDescription = "Tree",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(280.dp)
                )

                Text("You've already focused ${viewModel.formatTime((goalFocus*60L) - (timeRemaining))} minutes." +
                        "Its only a short way until $goalFocus minutes..."+
                    "Please type \"$quitterText\" below to unlock the quit button",
                    textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = typeText,
                    onValueChange = {typeText=it}
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),){
                    OutlinedButton(onClick = {
                        onQuitConfirmed()
                        onDismissed()
                    },
                        enabled = typeText==quitterText ){
                        Text("QUIT")
                    }
                    Button(onClick = {
                        onDismissed()
                    }){
                        Text("Close")
                    }
                }

            }

        }
    }
}