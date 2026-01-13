package neth.iecal.trease.utils

import kotlinx.coroutines.*
import neth.iecal.trease.models.AppInfo
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class LinuxAppBlocker {
    private var blockerJob: Job? = null
    private val isRunning = AtomicBoolean(false)
    private var blockedApps = setOf<String>()

    companion object {
        private var instance: LinuxAppBlocker? = null

        fun getInstance(): LinuxAppBlocker {
            if (instance == null) {
                instance = LinuxAppBlocker()
            }
            return instance!!
        }
    }

    fun startBlocking(appExecutables: Set<String>, durationMillis: Long) {
        if (isRunning.get()) {
            stopBlocking()
        }

        blockedApps = appExecutables
        isRunning.set(true)

        blockerJob = CoroutineScope(Dispatchers.IO).launch {
            val endTime = System.currentTimeMillis() + durationMillis

            while (isActive && isRunning.get() && System.currentTimeMillis() < endTime) {
                try {
                    blockApps()
                    delay(2000) // Check every 2 seconds
                } catch (e: Exception) {
                    println("Error blocking apps: ${e.message}")
                }
            }

            isRunning.set(false)
        }
    }

    fun stopBlocking() {
        isRunning.set(false)
        blockerJob?.cancel()
        blockerJob = null
    }

    private fun blockApps() {
        blockedApps.forEach { appName ->
            try {
                killProcessByName(appName)
            } catch (e: Exception) {
                println("Failed to block $appName: ${e.message}")
            }
        }
    }

    private fun killProcessByName(processName: String) {
        try {
            // Use pkill to kill processes by name
            val process = ProcessBuilder("pkill", "-9", processName)
                .redirectErrorStream(true)
                .start()

            process.waitFor()
        } catch (e: Exception) {
            // Silent fail - process might not be running
        }
    }

    fun getInstalledApps(): List<AppInfo> {
        val appsMap = mutableMapOf<String, String>() // packageName -> name

        // Scan for .desktop files (more user-friendly)
        File("/usr/share/applications").listFiles()?.forEach { file ->
            if (file.extension == "desktop") {
                try {
                    val content = file.readText()
                    val nameMatch = Regex("Name=(.+)").find(content)
                    val execMatch = Regex("Exec=(.+)").find(content)

                    if (nameMatch != null && execMatch != null) {
                        val name = nameMatch.groupValues[1].trim()
                        val exec = execMatch.groupValues[1]
                            .split(" ")[0]
                            .substringAfterLast("/")

                        if (!exec.startsWith("%") && exec.isNotBlank()) {
                            // Only add if not already present (first wins)
                            if (!appsMap.containsKey(exec)) {
                                appsMap[exec] = name
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Skip problematic files
                }
            }
        }

        // Add common executables from /usr/bin if not already included
        File("/usr/bin").listFiles()?.forEach { file ->
            if (file.isFile && file.canExecute() && !file.name.startsWith(".")) {
                val execName = file.name
                if (!appsMap.containsKey(execName) && isCommonApp(execName)) {
                    appsMap[execName] = execName
                }
            }
        }

        return appsMap.map { (packageName, name) ->
            AppInfo(name, packageName)
        }.sortedBy { it.name.lowercase() }
    }

    private fun isCommonApp(name: String): Boolean {
        val commonApps = listOf(
            "firefox", "chrome", "chromium", "brave", "opera",
            "slack", "discord", "telegram", "signal",
            "spotify", "vlc", "mpv",
            "steam", "lutris",
            "code", "sublime", "atom", "intellij",
            "gimp", "inkscape", "blender"
        )
        return commonApps.any { name.contains(it, ignoreCase = true) }
    }

    fun isBlocking(): Boolean = isRunning.get()
}
