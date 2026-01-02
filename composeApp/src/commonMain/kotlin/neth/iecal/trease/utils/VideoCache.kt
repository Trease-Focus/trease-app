package neth.iecal.trease.utils

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import neth.iecal.trease.getCacheDir
import neth.iecal.trease.getFileSystem
import neth.iecal.trease.getPlatformHttpClient
import okio.Path.Companion.toPath
import okio.buffer


suspend fun getCachedVideoPath(
    url: String,
    videoId: String
): String {

    val client = getPlatformHttpClient()
    val fileSystem = getFileSystem()

    val cachePath = getCacheDir().toPath()

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

        val sink = fileSystem.sink(filePath).buffer()

        try {
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(8_192)
                while (!packet.isEmpty) {
                    sink.write(packet.readBytes())
                }
            }
        } finally {
            sink.flush()
            sink.close()
        }

    } catch (e: Exception) {
        if (fileSystem.exists(filePath)) {
            fileSystem.delete(filePath)
        }
        throw e
    }

    return filePath.toString()
}
