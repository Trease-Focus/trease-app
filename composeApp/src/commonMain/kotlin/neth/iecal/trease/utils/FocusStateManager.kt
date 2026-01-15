package neth.iecal.trease.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import neth.iecal.trease.getCacheManager
import neth.iecal.trease.models.FocusStats

class FocusStateManager(){
    val cacheManager = getCacheManager()

    suspend fun setRunning(oversAt:Long) {
        cacheManager.saveFile("running_over_at",oversAt.toString())
    }
    suspend fun isRunning():Long? {
        return cacheManager.readFile("running_over_at")?.toLong()
    }

    suspend fun setRunningTree(tree: FocusStats) {
        cacheManager.saveFile("running_for_tree",Json.encodeToString(tree))
    }
    suspend fun getRunningTree():FocusStats? {
        val rawTree = cacheManager.readFile("running_for_tree") ?: return null
        return Json.decodeFromString<FocusStats>(rawTree)
    }


}