package neth.iecal.trease.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import neth.iecal.trease.Platform
import neth.iecal.trease.getPlatform
import neth.iecal.trease.ui.bottomsheet.TreeBottomSheet
import neth.iecal.trease.ui.components.TreeGrowthPlayer
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import neth.iecal.trease.viewmodels.TimerStatus

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    val timerViewModel = viewModel { HomeScreenViewModel() }
    val remainingSeconds by timerViewModel.remainingSeconds.collectAsStateWithLifecycle()
    val progress by timerViewModel.progress.collectAsStateWithLifecycle()
    val status by timerViewModel.timerStatus.collectAsStateWithLifecycle()
    val isTreeSelectionVisible by timerViewModel.isTreeSelectionVisible.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        if (isTreeSelectionVisible) {
            TreeBottomSheet(
                onDismiss = { timerViewModel.toggleIsTreeSelectionVisible() },
                onSelected = {
                    timerViewModel.selectedTree.value = it
                    timerViewModel.toggleIsTreeSelectionVisible()
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = timerViewModel.formatTime(remainingSeconds),
                fontSize = 100.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(100.dp))

            Box(
                modifier = Modifier
                    .size(280.dp)
                    .combinedClickable(
                        enabled = true,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(false,),
                        onClick = { timerViewModel.toggleIsTreeSelectionVisible() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                TreeGrowthPlayer(timerViewModel)
            }

            Spacer(modifier = Modifier.height(100.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .width(280.dp)
                    .height(2.dp),
                trackColor = MaterialTheme.colorScheme.onPrimary,
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "He who conquers himself is\nthe mightiest warrior",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (status == TimerStatus.Idle) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp,Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    OutlinedButton(onClick = { timerViewModel.adjustTime(-5) }) {
                        Text("−5", fontSize = 18.sp)
                    }

                    Button(
                        onClick = { timerViewModel.toggleTimer() },
                    ) {
                        Text("Start")
                    }

                    OutlinedButton(onClick = { timerViewModel.adjustTime(5) }) {
                        Text("+5", fontSize = 18.sp)
                    }
                }
            } else {
                TextButton(
                    onClick = { timerViewModel.toggleTimer() }
                ) {
                    Text("Give up", color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}