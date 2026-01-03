package neth.iecal.trease.utils

import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CoinManager(){
    val cacheManager = CacheManager()


    suspend fun addCoins(value: Int) {
        val coins = reloadCoins()
        cacheManager.saveFile("coins",(coins+value).toString())
    }

    suspend fun reloadCoins(): Int{
        return cacheManager.readFile("coins")?.toInt() ?: 0

    }

}