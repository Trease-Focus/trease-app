package neth.iecal.trease.utils

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import neth.iecal.trease.getCacheDir
import neth.iecal.trease.getFileSystem
import neth.iecal.trease.getPlatformHttpClient
// KEY IMPORTS FOR OKIO
import okio.Path.Companion.toPath
import okio.buffer

class VideoCacheManager {
    private val client = getPlatformHttpClient()
    // FIX: Use the expect/actual function
    private val fileSystem = getFileSystem()

    suspend fun getCachedVideoPath(url: String, videoId: String): String {
        val cacheDir = getCacheDir()
        val cachePath = cacheDir.toPath()

        if (!fileSystem.exists(cachePath)) {
            fileSystem.createDirectories(cachePath)
        }

        val filePath = cachePath.resolve("$videoId.webm")

        if (fileSystem.exists(filePath)) {
            return filePath.toString()
        }

        try {
            val response = client.get(url)
            val channel = response.bodyAsChannel()

            // 1. Create Sink
            // 2. Buffer it (turns Sink into BufferedSink)
            val bufferedSink = fileSystem.sink(filePath).buffer()

            try {
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(8192)
                    while (!packet.isEmpty) {
                        // 3. write now works because bufferedSink is valid
                        bufferedSink.write(packet.readBytes())
                    }
                }
            } finally {
                // 4. close/flush now work
                bufferedSink.flush()
                bufferedSink.close()
            }

        } catch (e: Exception) {
            if (fileSystem.exists(filePath)) {
                fileSystem.delete(filePath)
            }
            throw e
        }

        return filePath.toString()
    }
}