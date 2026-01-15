package neth.iecal.trease

import io.ktor.utils.io.core.toByteArray
import neth.iecal.trease.interfaces.CacheManager

class WebCacheManager: CacheManager {
    override suspend fun saveFile(fileName: String, content: String) {
        WebStorage.saveFile(fileName,content.toByteArray())
    }

    override suspend fun readFile(fileName: String): String? {
       return  WebStorage.loadFileUrl(fileName)
    }
}