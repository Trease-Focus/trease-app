package neth.iecal.trease.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.compose.ImagePainter
import neth.iecal.trease.Constants
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import org.jetbrains.compose.resources.painterResource
import trease.composeapp.generated.resources.Res
import trease.composeapp.generated.resources.coin
import trease.composeapp.generated.resources.grid
import trease.composeapp.generated.resources.outline_arrow_forward_ios_24
import trease.composeapp.generated.resources.rounded_clock_loader_10_24

@Composable
fun YouWon(viewModel: HomeScreenViewModel) {
    val goalFocus by viewModel.selectedMinutes.collectAsState()
    val isRare = viewModel.calculateRarity()
    val coins = viewModel.calculateRewardedCoin()
    val tree = viewModel.selectedTree.collectAsState().value
    val variant = viewModel.currentTreeSeedVariant.value

    Dialog(onDismissRequest = { viewModel.cleanTimerSession() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) ,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = tree.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "#$variant",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                Box(
                    modifier = Modifier
                        .size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "${Constants.cdn}/images/${tree.id}_${variant}_grid.png",
                        contentDescription = "Grown Tree",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.padding(12.dp).fillMaxSize()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = "${goalFocus}m",
                        label = "Focus",
                        painter = painterResource(Res.drawable.rounded_clock_loader_10_24)
                    )

                    // Only show Rarity if it is actually Rare, otherwise show something else or center the others
                    if (isRare) {
                        StatItem(
                            value = "Rare",
                            label = "Quality",
                            tint = MaterialTheme.colorScheme.primary,
                            painter = painterResource(Res.drawable.grid)

                        )
                    }

                    StatItem(
                        value = "+$coins",
                        label = "Earned",
                        tint = MaterialTheme.colorScheme.tertiary,
                        painter = painterResource(Res.drawable.coin)

                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Button
                Button(
                    onClick = { viewModel.cleanTimerSession() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Collect",
                        modifier = Modifier.padding(vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    painter: Painter
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painter,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}