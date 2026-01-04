package neth.iecal.trease.data


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import neth.iecal.trease.AppBlockerService
import neth.iecal.trease.AppBlockerServiceInfo
import neth.iecal.trease.INTENT_ACTION_START_DEEP_FOCUS
import nethical.questphone.data.AppInfo

class AllowedAppsRepositoryAndroid(private val context: Context) : AllowedAppsRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("allowed_apps", Context.MODE_PRIVATE)

    override suspend fun saveAllowedPackages(packages: Set<String>) = withContext(Dispatchers.IO) {
        prefs.edit().putStringSet("packages", packages).apply()
        startAppBlockerService(packages)
    }

    override suspend fun getAllowedPackages(): Set<String> {
        val packages = prefs.getStringSet("packages", emptySet()) ?: emptySet()
        return packages
    }

    override fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        return pm.getInstalledApplications(PackageManager.GET_META_DATA).map {
            AppInfo(
                name = pm.getApplicationLabel(it).toString(),
                packageName = it.packageName
            )
        }.sortedBy { it.name }
    }

    private fun startAppBlockerService(allowedApps: Set<String>) {
        AppBlockerServiceInfo.deepFocus.exceptionApps = allowedApps.toHashSet()
        AppBlockerServiceInfo.deepFocus.isRunning = true

        val broadcastIntent = Intent(INTENT_ACTION_START_DEEP_FOCUS)
        broadcastIntent.putStringArrayListExtra("exception", ArrayList(allowedApps))
        context.sendBroadcast(broadcastIntent)

        Thread.sleep(100)

        val intent = Intent(context, AppBlockerService::class.java)
        context.startForegroundService(intent)
    }

    fun stopService() {
        AppBlockerServiceInfo.deepFocus.isRunning = false
        AppBlockerServiceInfo.deepFocus.exceptionApps.clear()

        val intent = Intent(context, AppBlockerService::class.java)
        context.stopService(intent)

        val broadcastIntent = Intent("neth.iecal.trease.ACTION_STOP_DEEP_FOCUS")
        context.sendBroadcast(broadcastIntent)
    }

    override fun stopBlockerService() {
        stopService()
    }
}
