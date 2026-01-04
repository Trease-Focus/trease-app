package neth.iecal.trease.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import neth.iecal.trease.data.AllowedAppsRepositoryAndroid
import neth.iecal.trease.viewmodels.HomeScreenViewModel

@Composable
actual fun initializeRepository(viewModel: HomeScreenViewModel) {
    val context = LocalContext.current
    viewModel.repository = AllowedAppsRepositoryAndroid(context)
}
