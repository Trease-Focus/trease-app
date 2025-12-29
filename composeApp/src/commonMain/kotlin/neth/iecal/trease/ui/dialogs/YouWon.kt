package neth.iecal.trease.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import neth.iecal.trease.viewmodels.HomeScreenViewModel


@Composable
fun YouWon(viewModel: HomeScreenViewModel){
    val goalFocus by viewModel.selectedMinutes.collectAsState()

    Dialog(
        onDismissRequest = {
            viewModel.cleanTimerSession()
        }
    ){
        Surface(modifier = Modifier.wrapContentSize()) {
            Column(modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {

                Text("Wooww", style = MaterialTheme.typography.headlineLarge,textAlign = TextAlign.Center)

                AsyncImage(
                    model = "https://trease-focus.github.io/cache-trees/images/${viewModel.selectedTree.value}.png",
                    contentDescription = "Dead Tree",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(280.dp)
                )

                Text("You focused $goalFocus minutes to grow this tree",
                    textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    viewModel.cleanTimerSession()
                }){
                    Text("Close")
                }
            }

        }
    }
}