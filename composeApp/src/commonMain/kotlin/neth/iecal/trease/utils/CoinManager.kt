package neth.iecal.trease.utils

class CoinManager(){
    val cacheManager = CacheManager()


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