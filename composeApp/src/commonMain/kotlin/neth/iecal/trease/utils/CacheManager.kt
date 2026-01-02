package neth.iecal.trease.utils

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import neth.iecal.trease.getCacheDir
import neth.iecal.trease.getFileSystem
import neth.iecal.trease.getPlatformHttpClient
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

class CacheManager {

    private val fileSystem = getFileSystem()


    suspend fun saveFile(
        fileName: String,
        content: String
    ) {
        return withContext(Dispatchers.Main) {
            val cachePath = getCacheDir().toPath()

            if (!fileSystem.exists(cachePath)) {
                fileSystem.createDirectories(cachePath)
            }

            val filePath = cachePath.resolve(fileName)

            fileSystem.sink(filePath).buffer().use { sink ->
                sink.writeUtf8(content)
            }
        }
    }

    suspend fun readFile(fileName: String): String? {
        return withContext(Dispatchers.Main) {
            val filePath = getCacheDir()
                .toPath()
                .resolve(fileName)

            if (!fileSystem.exists(filePath)) {
                return@withContext null // Note: explicit return target
            }

            fileSystem.source(filePath).buffer().use { source ->
                source.readUtf8()
            }
        }
    }
}
