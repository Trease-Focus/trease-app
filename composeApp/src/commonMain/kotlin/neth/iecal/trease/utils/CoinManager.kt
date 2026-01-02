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

class CoinManager(): ViewModel(){
    val _coins = MutableStateFlow(0)
    val coins : StateFlow<Int> = _coins.asStateFlow()
    val cacheManager = CacheManager()
    val vmScope = CoroutineScope(Dispatchers.Default)

    init {
        reloadCoins()
    }

    fun addCoins(value: Int) {
        _coins.value = value
        vmScope.launch {
            cacheManager.saveFile("coins",coins.value.toString())
        }
    }

    fun reloadCoins(){
        vmScope.launch {
            _coins.value = cacheManager.readFile("coins")?.toInt() ?: 0
        }
    }

}