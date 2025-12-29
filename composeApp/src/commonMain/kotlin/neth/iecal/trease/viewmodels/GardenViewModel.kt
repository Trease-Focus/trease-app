package neth.iecal.trease.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import neth.iecal.trease.models.FocusStats
import neth.iecal.trease.utils.CacheManager
import neth.iecal.trease.utils.TreeStatsLodger

class GardenViewModel(): ViewModel() {
    private val _treeList = MutableStateFlow(listOf<String>())
    val treeList: StateFlow<List<String>> = _treeList.asStateFlow()

    var stats = listOf<FocusStats>()
    val treeStatsLodger = TreeStatsLodger()
    val viewModelScope: CoroutineScope = MainScope()
    init {
        updateTreeStats()
    }

    fun updateTreeStats(){
        viewModelScope.launch(Dispatchers.Default) {
            stats = treeStatsLodger.getCache()
            println("Loaded ${stats} tree stats")
            addTreeFromStats()
        }
    }
    private fun addTreeFromStats(){
        val temp = mutableListOf<String>()
        stats.forEach {
            if(it.isFailed) {
                temp.add(it.failureTree)
            }else{
                temp.add(it.treeId)
            }
        }
        _treeList.value = temp.toList()
    }


}