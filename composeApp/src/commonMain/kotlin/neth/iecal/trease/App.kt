package neth.iecal.trease
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import neth.iecal.trease.ui.screens.HomeScreen
import neth.iecal.trease.ui.theme.ZenTheme

@Serializable
data object Home

@Composable
fun App() {
    val navController = rememberNavController()
    ZenTheme() {
        NavHost(navController = navController, startDestination = Home) {
            composable<Home> {
                HomeScreen()
            }

        }
    }
}
