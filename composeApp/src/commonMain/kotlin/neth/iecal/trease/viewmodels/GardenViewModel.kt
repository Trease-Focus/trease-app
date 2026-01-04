package neth.iecal.trease.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import neth.iecal.trease.models.FocusStats
import neth.iecal.trease.utils.TreeStatsLodger
import neth.iecal.trease.utils.getCurrentMonthYear
import neth.iecal.trease.utils.toLocalDate
import kotlin.math.roundToInt

class GardenViewModel: ViewModel() {
    private val _pngTreeList = MutableStateFlow(listOf<String>())
    val pngTreeList: StateFlow<List<String>> = _pngTreeList.asStateFlow()

    private val _stats = MutableStateFlow(
        listOf<FocusStats>()
    )
    val stats: StateFlow<List<FocusStats>> = _stats.asStateFlow()
    val treeStatsLodger = TreeStatsLodger()
    val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    val _totalFocus = MutableStateFlow(0)
    val totalFocus: StateFlow<Int> = _totalFocus.asStateFlow()


    init {
        updateTreeStats(getCurrentMonthYear())
    }

    fun updateTreeStats(isoDate: String) {
        viewModelScope.launch{
            _stats.value = treeStatsLodger.getCache(isoDate)
            println("Loaded ${stats.value} tree stats")
            listAllPngGrids()

            _streak.value = calculateStreak(stats.value)
            _totalFocus.value = calculateTotalMinsFocused(stats.value)
        }
    }

    private fun listAllPngGrids() {
        val temp = mutableListOf<String>()
        stats.value.forEach {
            if (it.isFailed) {
                temp.add(it.failureTree+"_grid.png")
            } else {
                temp.add(it.treeId + "_${it.treeSeed}_grid.png")
            }
        }
        _pngTreeList.value = temp.toList()
    }


    private fun calculateStreak(stats: List<FocusStats>): Int {
        if (stats.isEmpty()) return 0

        val dates = stats
            .filter { !it.isFailed }
            .map { it.completedOn.toLocalDate() }
            .distinct()
            .sortedDescending()

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val yesterday = today.minus(DatePeriod(days = 1))

        val latestDate = dates.firstOrNull()

        if (latestDate != today && latestDate != yesterday) {
            return 0
        }

        var streak = 1
        for (i in 0 until dates.size - 1) {
            val current = dates[i]
            val next = dates[i + 1]

            // Check if the next date in the list is exactly one day before the current one
            if (current.minus(DatePeriod(days = 1)) == next ) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateTotalMinsFocused(statsLst: List<FocusStats>): Int {
        if(statsLst.isEmpty()) return 0
        var totalSeconds = 0f
        statsLst.filter { !it.isFailed }
            .forEach {
                totalSeconds+=it.duration
            }
        print("Total seconds $totalSeconds")
        val totalMinutes = (totalSeconds / 60)

        return totalMinutes.roundToInt()
    }
}
