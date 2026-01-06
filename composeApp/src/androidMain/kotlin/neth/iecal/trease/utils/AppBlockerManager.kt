package neth.iecal.trease.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import neth.iecal.trease.services.ACTION_START_DEEP_FOCUS
import neth.iecal.trease.services.ACTION_STOP_DEEP_FOCUS
import neth.iecal.trease.services.AppBlockerService
import neth.iecal.trease.data.AppInfo
import androidx.core.content.edit
import neth.iecal.trease.data.DeepFocus
import neth.iecal.trease.services.ServiceState

class AppBlockerManager(private val context: Context)  {
    private val prefs: SharedPreferences = context.getSharedPreferences("allowed_apps", Context.MODE_PRIVATE)

     suspend fun saveAllowedPackages(packages: Set<String>) = withContext(Dispatchers.IO) {
         prefs.edit { putStringSet("packages", packages) }
     }

     suspend fun getAllowedPackagesCache(): Set<String> {
        val packages = prefs.getStringSet("packages", emptySet()) ?: emptySet()
        return packages
    }

    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA).map {
            AppInfo(
                name = pm.getApplicationLabel(it).toString(),
                packageName = it.packageName,
            )
        }.sortedBy { it.name }
    }
    fun startAppBlockerService(allowedApps: Set<String>, duration: Long) {
        val serviceIntent = Intent(context, AppBlockerService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

        // in case the service was freshly started
        ServiceState.isRunning = true
        ServiceState.duration = duration
        ServiceState.lockedPackages = allowedApps.toMutableSet()

        val broadcastIntent = Intent(ACTION_START_DEEP_FOCUS).apply {
            // incase it was already running and deepfocus needs to be started
            putStringArrayListExtra("exception", ArrayList(allowedApps))
            putExtra("duration", duration)
            setPackage(context.packageName)
        }
        context.sendBroadcast(broadcastIntent)
    }

    fun stopService() {
        val broadcastIntent = Intent(ACTION_STOP_DEEP_FOCUS).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(broadcastIntent)

        val serviceIntent = Intent(context, AppBlockerService::class.java)
        context.stopService(serviceIntent)
    }

}