package neth.iecal.trease.services

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import neth.iecal.trease.MainActivity
import neth.iecal.trease.R
import neth.iecal.trease.data.DeepFocus
import neth.iecal.trease.utils.getKeyboards
import neth.iecal.trease.utils.reloadApps
import java.util.TreeMap

const val ACTION_START_DEEP_FOCUS = "trease.start.deepfocus"
const val ACTION_STOP_DEEP_FOCUS = "trease.stop.deepfocus"
const val NOTIF_CHANNEL_ID = "AppBlockerService"
const val NOTIF_ID = 123
const val POLLING_INTERVAL = 150L

object ServiceState {
    var isRunning = false
    var currentLockedPackage: String? = null
    var lockedPackages = mutableSetOf<String>()
    var duration = 0L //in ms
}

class AppBlockerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var usageStatsManager: UsageStatsManager
    private var timerJob: Job? = null


    override fun onCreate() {
        super.onCreate()
        ServiceState.isRunning = true
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        createNotificationChannel()
        startForegroundServiceCompat()
        registerBroadcastReceiver()

        if(ServiceState.isRunning) {
            startDeepFocus(ServiceState.duration, ServiceState.lockedPackages.toList())
        }
        // Start the main blocking loop
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        ServiceState.isRunning = false
        ServiceState.currentLockedPackage = null
        serviceScope.cancel() // Kills all monitoring and timers
        try { unregisterReceiver(controlReceiver) } catch (_: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? = null


    private fun startMonitoring() = serviceScope.launch {
        while (isActive) {
            val foregroundApp = getForegroundApp()

            if (foregroundApp != null && foregroundApp != packageName) {
                handleAppCheck(foregroundApp)
            }

            delay(POLLING_INTERVAL)
        }
    }

    private fun handleAppCheck(packageName: String) {
        //  Check if it's the launcher (Home screen)
        if (isLauncherApp(packageName)) {
            ServiceState.currentLockedPackage = null
            return
        }

        // Check if locked
        if (ServiceState.lockedPackages.contains(packageName)) {
            // Avoid spamming the overlay if we are already handling this package
            if (ServiceState.currentLockedPackage != packageName) {
                blockApp(packageName)
            }
        } else {
            // App is allowed
            ServiceState.currentLockedPackage = null
        }
    }

    private fun blockApp(packageName: String) {
        ServiceState.currentLockedPackage = packageName

        // Bring our activity to front
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("locked_package", packageName)
        }
        startActivity(intent)
    }

    private fun getForegroundApp(): String? {
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, time - 10000, time
        )

        if (stats.isNullOrEmpty()) return null

        val sortedStats = TreeMap<Long, String>()
        for (usage in stats) {
            sortedStats[usage.lastTimeUsed] = usage.packageName
        }

        return if (sortedStats.isNotEmpty()) sortedStats.lastEntry()?.value else null
    }

    private fun isLauncherApp(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName == packageName
    }


    private fun startDeepFocus(durationMs: Long, exceptions: List<String>) {
        serviceScope.launch {
            updateLockedAppsList(exceptions)

            ServiceState.isRunning = true
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Focus Started", Toast.LENGTH_SHORT).show()
            }

            startTimer(durationMs)
        }
    }

    private suspend fun updateLockedAppsList(exceptions: List<String>) {
        val result = reloadApps(packageManager, applicationContext)
        val allApps = result.getOrDefault(emptyList())
        val keyboards = getKeyboards(applicationContext)

        val toLock = allApps.filter { app ->
            val isException = exceptions.contains(app.packageName)
            val isKeyboard = keyboards.contains(app.packageName)
            val isSelf = app.packageName == packageName
            !isException && !isKeyboard && !isSelf
        }.map { it.packageName }

        ServiceState.lockedPackages.clear()
        ServiceState.lockedPackages.addAll(toLock)
    }

    private fun stopDeepFocus() {
        ServiceState.lockedPackages.clear()
        ServiceState.isRunning = false

        Toast.makeText(this, "Focus Session Complete", Toast.LENGTH_LONG).show()

        // Kill service completely
        stopSelf()
    }

    private fun startTimer(durationMs: Long) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            val endTime = System.currentTimeMillis() + durationMs

            while (isActive) {
                val remaining = endTime - System.currentTimeMillis()

                if (remaining <= 0) {
                    withContext(Dispatchers.Main) { stopDeepFocus() }
                    break
                }

                updateNotification(remaining)
                delay(1000) // Update notification every second
            }
        }
    }

    private val controlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_START_DEEP_FOCUS -> {
                    val exceptions = intent.getStringArrayListExtra("exception") ?: arrayListOf()
                    val duration = intent.getLongExtra("duration", 0L)
                    startDeepFocus(duration, exceptions)
                }
                ACTION_STOP_DEEP_FOCUS -> stopDeepFocus()
            }
        }
    }

    private fun registerBroadcastReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_START_DEEP_FOCUS)
            addAction(ACTION_STOP_DEEP_FOCUS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(controlReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(controlReceiver, filter)
        }
    }

    private fun updateNotification(remainingMs: Long) {
        val seconds = (remainingMs / 1000) % 60
        val minutes = (remainingMs / (1000 * 60))
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val notification = getNotificationBuilder()
            .setContentText("Focus Time Remaining: $timeText")
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID, notification)
    }

    private fun startForegroundServiceCompat() {
        val notification = getNotificationBuilder().build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("Trease Deep Focus")
            .setContentText("Initializing...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIF_CHANNEL_ID,
            "Deep Focus Active",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows active focus timer"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}