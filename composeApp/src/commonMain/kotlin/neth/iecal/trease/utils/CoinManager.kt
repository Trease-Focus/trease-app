package neth.iecal.trease.utils

import neth.iecal.trease.getCacheManager

class CoinManager(){
    val cacheManager = getCacheManager()


    suspend fun addCoins(value: Int) {
        val coins = reloadCoins()
        cacheManager.saveFile("coins",(coins+value).toString())
    }
    suspend fun removeCoins(value: Int) {
        val coins = reloadCoins()
        cacheManager.saveFile("coins",(coins-value).toString())
    }

    suspend fun reloadCoins(): Int{
        return cacheManager.readFile("coins")?.toInt() ?: 40

    }

}