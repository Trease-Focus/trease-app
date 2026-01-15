package neth.iecal.trease.interfaces

interface CacheManager {
    suspend fun saveFile(
        fileName: String,
        content: String
    )
    suspend fun readFile(fileName: String): String?
}