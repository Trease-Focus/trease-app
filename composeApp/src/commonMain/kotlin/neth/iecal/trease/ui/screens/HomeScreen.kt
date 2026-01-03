package neth.iecal.trease.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import neth.iecal.trease.Garden
import neth.iecal.trease.GardenFullScreen
import neth.iecal.trease.models.TimerStatus
import neth.iecal.trease.ui.bottomsheet.GrowTreeBottomSheet
import neth.iecal.trease.ui.bottomsheet.WitheredTreeSheet
import neth.iecal.trease.ui.components.TreeGrowthPlayer
import neth.iecal.trease.ui.dialogs.AppInfoDialog
import neth.iecal.trease.ui.dialogs.WarningBeforeQuit
import neth.iecal.trease.ui.dialogs.YouLost
import neth.iecal.trease.ui.dialogs.YouWon
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import org.jetbrains.compose.resources.painterResource
import trease.composeapp.generated.resources.Res
import trease.composeapp.generated.resources.baseline_info_outline_24
import trease.composeapp.generated.resources.coin
import trease.composeapp.generated.resources.grid
import trease.composeapp.generated.resources.stats

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val viewModel = viewModel { HomeScreenViewModel() }
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val status by viewModel.timerStatus.collectAsStateWithLifecycle()
    val isTreeSelectionVisible by viewModel.isTreeSelectionVisible.collectAsStateWithLifecycle()
    val isWitheredTreeSelectionVisible by viewModel.isWitheredTreeSelectionVisible.collectAsStateWithLifecycle()



    val coins by viewModel.coins.collectAsStateWithLifecycle()
    var isShowQuitWarningDialog by remember { mutableStateOf(false) }
    var showAppInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.displayCutout
    ) { paddingValues ->

        if (isTreeSelectionVisible) {
            GrowTreeBottomSheet(
                onDismiss = { viewModel.toggleIsTreeSelectionVisible() },
                onAddToCanvas = {
                    viewModel.selectTree(it)
                    viewModel.toggleIsTreeSelectionVisible()
                },
                onShowWitheredTrees = {
                    viewModel.toggleIsTreeSelectionVisible()
                    viewModel.toggleIsWitheredTreeSelectionVisible()
                },
                viewModel
            )
        }

        if(isWitheredTreeSelectionVisible) {
            WitheredTreeSheet(
                onWitheredTreeSelected = {
                    viewModel.selectWitheredTree(it)
                },
                onDismissRequest = {
                    viewModel.toggleIsWitheredTreeSelectionVisible()
                }
            )
        }

        if(showAppInfoDialog) {
            AppInfoDialog{
                showAppInfoDialog = false
            }
        }
        Box(Modifier.fillMaxSize()) {
            if(status != TimerStatus.Running) {
                TopAppBar(
                    modifier = Modifier.align(Alignment.TopCenter).zIndex(999f).padding(end = 16.dp),
                    title = {},
                    actions = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showAppInfoDialog = true }, Modifier.size(40.dp)) {
                                Image(
                                    painter = painterResource(Res.drawable.baseline_info_outline_24),
                                    contentDescription = "Info",
                                )
                            }
                            IconButton(onClick = { navController.navigate(Garden) }, Modifier.size(40.dp)) {
                                Image(
                                    painter = painterResource(Res.drawable.stats),
                                    contentDescription = "Garden",
                                )
                            }
                            IconButton(onClick = { navController.navigate(GardenFullScreen) }, Modifier.size(25.dp)) {
                                Image(
                                    painter = painterResource(Res.drawable.grid),
                                    contentDescription = "Garden",
                                    Modifier.size(40.dp)
                                )
                            }
                            Row {
                                Image(
                                    painter = painterResource(Res.drawable.coin),
                                    contentDescription = "Garden",
                                )
                                Text(" $coins")
                            }

                        }

                    }
                )
            }
            Column(
                Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        text = viewModel.formatTime(remainingSeconds),
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .combinedClickable(
                                enabled = true,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(false,),
                                onClick = { viewModel.toggleIsTreeSelectionVisible() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        TreeGrowthPlayer(viewModel, 1.2f)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .width(280.dp)
                            .height(2.dp)
                            .padding(top = 8.dp),
                        trackColor = MaterialTheme.colorScheme.onPrimary,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "He who conquers himself is\nthe mightiest warrior",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )


                    if (isShowQuitWarningDialog) {
                        WarningBeforeQuit(
                            viewModel, onDismissed = {
                                isShowQuitWarningDialog = false
                            },
                            onQuitConfirmed = {
                                viewModel.toggleTimer() // quits the timer and sets status to has_quit
                            })
                    }

                    if (status == TimerStatus.HAS_QUIT) {
                        YouLost(viewModel)
                    }
                    if (status == TimerStatus.HAS_WON) {
                        YouWon(viewModel)
                    }

                    when (status) {
                        TimerStatus.Running -> {
                            TextButton(
                                onClick = {
                                    isShowQuitWarningDialog = true
                                }
                            ) {
                                Text("Give up", color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                            }
                        }

                        else -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                modifier = Modifier.fillMaxWidth(0.7f)
                            ) {
                                OutlinedButton(onClick = {
                                    viewModel.adjustTime(-5) }, enabled = viewModel.selectedWitheredTree.value == null) {
                                    Text("−5", fontSize = 18.sp)
                                }

                                Button(
                                    onClick = { viewModel.toggleTimer() },
                                ) {
                                    Text("Start")
                                }

                                OutlinedButton(onClick = {

                                    viewModel.adjustTime(if(viewModel.selectedMinutes.value==1L) 4 else 5)
                                },
                                    enabled = viewModel.selectedWitheredTree.value == null) {
                                    Text("+5", fontSize = 18.sp)
                                }
                            }
                        }


                    }

                }
            }
        }
    }
}