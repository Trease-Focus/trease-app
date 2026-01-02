package neth.iecal.trease.utils

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.delay
import neth.iecal.trease.getCacheDir
import neth.iecal.trease.getFileSystem
import neth.iecal.trease.getPlatformHttpClient
import okio.Path.Companion.toPath
import okio.buffer

suspend fun getCachedVideoPath(
    url: String,
    videoId: String,
    selectedSeed: Int
): String {

    val client = getPlatformHttpClient()
    val fileSystem = getFileSystem()
    val cachePath = getCacheDir().toPath()

    if (!fileSystem.exists(cachePath)) {
        fileSystem.createDirectories(cachePath)
    }

    val baseName = "${videoId}_${selectedSeed}"
    val finalPath = cachePath.resolve("$baseName.webm")
    val tempPath = cachePath.resolve("$baseName.webm.part")
    val okPath = cachePath.resolve("$baseName.ok")

    if (fileSystem.exists(finalPath) && fileSystem.exists(okPath)) {
        return finalPath.toString()
    }

    // Cleanup any previous failed attempt
    if (fileSystem.exists(tempPath)) {
        fileSystem.delete(tempPath)
    }
    if (fileSystem.exists(okPath)) {
        fileSystem.delete(okPath)
    }

    var sink: okio.BufferedSink? = null

    try {
        val response = client.get(url)
        val channel = response.bodyAsChannel()

        sink = fileSystem.sink(tempPath).buffer()

        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(8_192)
            while (!packet.isEmpty) {
                sink.write(packet.readBytes())
            }
        }

        sink.flush()
        sink.close()
        sink = null

        fileSystem.atomicMove(tempPath, finalPath)

        // delay for filesystem / mmap visibility (Linux quirk)
        delay(50)

        fileSystem.write(okPath) {
            writeUtf8("ok")
        }

    } catch (e: Exception) {
        sink?.close()

        if (fileSystem.exists(tempPath)) {
            fileSystem.delete(tempPath)
        }
        if (fileSystem.exists(okPath)) {
            fileSystem.delete(okPath)
        }

        throw e
    }

    return finalPath.toString()
}
