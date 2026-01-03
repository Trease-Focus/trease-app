package neth.iecal.trease.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import neth.iecal.trease.Constants
import neth.iecal.trease.viewmodels.HomeScreenViewModel
import org.jetbrains.compose.resources.painterResource
import trease.composeapp.generated.resources.Res
import trease.composeapp.generated.resources.coin
import trease.composeapp.generated.resources.grid
import trease.composeapp.generated.resources.rounded_clock_loader_10_24

@Composable
fun AppInfoDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) ,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),

            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Trease",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "A cross platform, open source and free focus tool.",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.size(8.dp))


                Text("I’m Nethical, the 17-year-old developer behind Digipaws, Questphone, and Trease.\n" +
                        "\n" +
                        "I was tired of seeing essential tools locked behind greedy paywalls, so I built these free, open-source alternatives to empower masses.\n" +
                        "\n" +
                        "Your support keeps me independent and ensures this software stays free forever.\n" +
                        "\n" +
                        "Support via: PayPal • Patreon • Crypto • Card (GitHub Sponsers) • UPI",
                    textAlign = TextAlign.Center,
                    )
                Button(
                    onClick = { uriHandler.openUri("https://digipaws.life/donate") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Donate",
                        modifier = Modifier.padding(vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,

                    )
                }

                Button(
                    onClick = { uriHandler.openUri("https://github.com/Trease-Focus/trease-app") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Star project on GitHub",
                        modifier = Modifier.padding(vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,

                    )
                }

                Spacer(Modifier.size(8.dp))

                Text(
                    text = "Trease is community-maintained, and we warmly welcome artists to contribute new tree designs!",
                    modifier = Modifier.padding(vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,

                )
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://github.com/Trease-Focus/trease-artwork") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Submit a new tree design",
                        modifier = Modifier.padding(vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,

                    )
                }
            }
        }
    }
}
