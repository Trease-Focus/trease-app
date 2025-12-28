package neth.iecal.trease
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import neth.iecal.trease.ui.components.IsometricForest
import neth.iecal.trease.ui.screens.HomeScreen
import neth.iecal.trease.ui.theme.ZenTheme

@Serializable
data object Home

@Composable
fun App() {
    val navController = rememberNavController()
    ZenTheme() {
//        NavHost(navController = navController, startDestination = Home) {
//            composable<Home> {
//                HomeScreen()
//            }
//
//        }

        // Generate 500 IDs exactly like the user script
        val treeIds = remember { MutableList(21) { "tree" } }

        IsometricForest(treeIds = treeIds)
    }
}
