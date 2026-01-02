package neth.iecal.trease.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import neth.iecal.trease.models.FocusStats

class TreeStatsLodger {
    val cacheManager = CacheManager()

    suspend fun injectStats(focusStats: FocusStats) {
        val statsRaw = cacheManager.readFile("${getCurrentMonthYear()}.json")
        val stats = if(statsRaw != null) {
             Json.decodeFromString<MutableList<FocusStats>>(statsRaw)
        } else {
            mutableListOf()
        }
        val index = stats.indexOfFirst { it.id == focusStats.id }

        if (index != -1) {
            stats[index] = focusStats   // replace at same position
        } else {
            stats.add(focusStats)       // or add if not found
        }

        val encoded = Json.encodeToString(stats)
        cacheManager.saveFile("${getCurrentMonthYear()}.json", encoded)
    }




    suspend fun getCache(date:String = getCurrentMonthYear()):List<FocusStats> {

        val statsRaw = cacheManager.readFile("$date.json")
        val stats = if(statsRaw != null) {
            Json.decodeFromString<List<FocusStats>>(statsRaw)
        } else listOf()
        return stats
    }

    suspend fun getTodayStats():List<FocusStats> {
        val cache = getCache().filter {
            it.completedOn.getDate() == getCurrentDate()
        }
        return cache
    }

}