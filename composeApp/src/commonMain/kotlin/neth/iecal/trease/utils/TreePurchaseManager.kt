package neth.iecal.trease.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.internal.decodeStringToJsonTree
import neth.iecal.trease.Constants
import neth.iecal.trease.getCacheManager

class TreePurchaseManager(){
    val cacheManager = getCacheManager()

    suspend fun addTree(treeId: String) {
        val trees = loadAllPurchasedTrees().toMutableList()
        trees.add(treeId)
        cacheManager.saveFile("purchased_trees", Json.encodeToString(trees))
    }

    suspend fun loadAllPurchasedTrees(): List<String> {
        val treesRaw = cacheManager.readFile("purchased_trees")
        return if(treesRaw == null) {
            Constants.preOwnedTrees
        }else{
            Json.decodeFromString<List<String>>(treesRaw)
        }
    }

}