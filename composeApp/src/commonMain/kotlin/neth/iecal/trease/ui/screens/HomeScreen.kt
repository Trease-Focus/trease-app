package neth.iecal.trease.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import neth.iecal.trease.Home
import neth.iecal.trease.ui.bottomsheet.TreeBottomSheet
import neth.iecal.trease.ui.components.IsometricGrid
import neth.iecal.trease.ui.components.ProgressBar
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import neth.iecal.trease.viewmodels.TimerStatus

@Composable
fun HomeScreen() {
    val timerViewModel = viewModel { HomeScreenViewModel() }
    val remainingSeconds by timerViewModel.remainingSeconds.collectAsStateWithLifecycle()
    val progress by timerViewModel.progress.collectAsStateWithLifecycle()
    val status by timerViewModel.timerStatus.collectAsStateWithLifecycle()

    val isTreeSelectionVisible by timerViewModel.isTreeSelectionVisible.collectAsStateWithLifecycle()
    val selectedTreeId by timerViewModel.selectedTree.collectAsStateWithLifecycle()

    Scaffold(Modifier.fillMaxSize()) { paddingValues ->
        if(isTreeSelectionVisible) {
            TreeBottomSheet(onDismiss = {
                timerViewModel.toggleIsTreeSelectionVisible()
            }, onSelected = {
                timerViewModel.selectedTree.value = it
                timerViewModel.toggleIsTreeSelectionVisible()
            })
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer Display
            Text(
                text = timerViewModel.formatTime(remainingSeconds),
                fontSize = 80.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-2).sp
            )

            // The Isometric Tree
            Box(modifier = Modifier.size(300.dp).combinedClickable(true, onClick ={
                timerViewModel.toggleIsTreeSelectionVisible()
            }), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = "https://trease-focus.github.io/cache-trees/images/${selectedTreeId}_grid.png",
                    contentDescription = selectedTreeId,
                    contentScale = ContentScale.Crop,
                    clipToBounds = true,
                    modifier = Modifier.fillMaxSize()

                )
            }

            ProgressBar(progress = progress)

            Spacer(modifier = Modifier.height(32.dp))

            // Setting/Control UI
            if (status == TimerStatus.Idle) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = { timerViewModel.adjustTime(-5) }) { Text("-5m") }

                    Button(
                        onClick = { timerViewModel.toggleTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("START FOCUS", color = Color.White)
                    }

                    Button(onClick = { timerViewModel.adjustTime(5) }) { Text("+5m") }
                }
            } else {
                Button(
                    onClick = { timerViewModel.toggleTimer() },
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("GIVE UP")
                }
            }

            Text(
                text = "He who conquers himself is\nthe mightiest warrior",
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 40.dp),
                color = Color.Gray
            )
        }
    }
}