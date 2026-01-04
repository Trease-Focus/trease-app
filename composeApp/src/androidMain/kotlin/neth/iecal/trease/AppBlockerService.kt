package neth.iecal.trease

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.text.get

class AppBlockerService : Service() {

    private val TAG = "AppBlockServiceFG"
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var handler: Handler
    private var timerRunnable: Runnable? = null
    private var lastForegroundPackage: String? = null
    private var isTimerRunning = false
    private var timerRunningForPackage = ""

    // Default locked apps - will be overridden by saved preferences
    private val lockedApps = mutableSetOf<String>()

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "AppBlockService"
        const val NOTIFICATION_ID = 123
        var isOverlayActive = false
        var currentLockedPackage: String? = null

        // Polling intervals
        private const val STANDARD_POLLING_INTERVAL_MS = 100L
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        Log.d(TAG, "AppBlockerService onCreate")
        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        handler = Handler(Looper.getMainLooper())
        createNotificationChannel()
        setupBroadcastListeners()

        // Resume state if service was killed
        if (AppBlockerServiceInfo.deepFocus.isRunning) {
            turnDeepFocus()
            // Resume the timer if we know the duration, otherwise it might just stay locked without a timer
            if (AppBlockerServiceInfo.deepFocus.duration > 0) {
                // Note: You might need logic here to calculate remaining time if you store start timestamp
                startCooldownTimer("deepfocus", AppBlockerServiceInfo.deepFocus.duration)
            }
        } else {
            loadLockedApps() // Load standard blocks if not in deep focus
        }

        startMonitoringApps()
        AppBlockerServiceInfo.appBlockerService = this
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun setupBroadcastListeners() {
        val filter = IntentFilter().apply {
            addAction(INTENT_ACTION_START_DEEP_FOCUS)
            addAction(INTENT_ACTION_STOP_DEEP_FOCUS)
        }

        Log.d(TAG, "registering receiver")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(refreshReceiver, filter, RECEIVER_EXPORTED)
            } else {
                registerReceiver(refreshReceiver, filter)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register receiver: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(appMonitorRunnable)
        AppBlockerServiceInfo.appBlockerService = null
        showHomwScreenOverlay()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID)

        try {
            unregisterReceiver(refreshReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Receiver not registered or already unregistered")
        }
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "App Block Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        serviceChannel.description = "Allows the appblocker to be run"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("AppBlocker Active")
            .setOngoing(true)
            .setContentText("Protecting your time")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)

        return builder.build()
    }

    private fun startMonitoringApps() {
        handler.post(appMonitorRunnable)
    }

    private val appMonitorRunnable = object : Runnable {
        override fun run() {
            detectAndHandleForegroundApp()
            handler.postDelayed(this, STANDARD_POLLING_INTERVAL_MS)
        }
    }

    private fun detectAndHandleForegroundApp() {
        val currentTime = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(currentTime - 2000, currentTime)
        val event = UsageEvents.Event()
        var detectedForegroundPackage: String?
        val recentLockedAppActivities = mutableSetOf<String>()

        var latestTimestamp: Long = 0
        var currentForegroundAppFromEvents: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                if (event.timeStamp > latestTimestamp) {
                    latestTimestamp = event.timeStamp
                    currentForegroundAppFromEvents = event.packageName
                }
                if (lockedApps.contains(event.packageName)) {
                    recentLockedAppActivities.add(event.packageName)
                }
            }
        }
        detectedForegroundPackage = currentForegroundAppFromEvents ?: getCurrentForegroundApp()

        if (isOverlayActive && detectedForegroundPackage != null && lockedApps.contains(detectedForegroundPackage)) {
            if (currentLockedPackage == detectedForegroundPackage) {
                handler.post { refreshHomeScreenOverlay() }
                return
            }
        }

        val isHomeScreen = isLauncherApp(detectedForegroundPackage)
        if (isHomeScreen) {
            handleHomeScreenDetected(detectedForegroundPackage)
            return
        }

        if (shouldShowLockScreen(recentLockedAppActivities, detectedForegroundPackage)) {
            return
        }

        detectedForegroundPackage?.let { foregroundPackage ->
            processForegroundApp(foregroundPackage)
        }
    }

    private fun shouldShowLockScreen(
        recentLockedAppActivities: Set<String>,
        detectedForegroundPackage: String?
    ): Boolean {
        if (detectedForegroundPackage == null) return false
        if (detectedForegroundPackage == packageName) return false

        val isAppCurrentlyLocked = lockedApps.contains(detectedForegroundPackage)
        val isTemporarilyUnlocked = AppBlockerServiceInfo.unlockedApps.containsKey(detectedForegroundPackage)

        // During Deep Focus, temporary unlocks might be restricted unless logic exists elsewhere.
        // Assuming current structure allows unlocks if they are in the unlockedApps map.

        if (isAppCurrentlyLocked && !isOverlayActive && !isTemporarilyUnlocked) {
            Log.d(TAG, "Lock condition met for: $detectedForegroundPackage")
            showLockScreenFor(detectedForegroundPackage)
            return true
        } else {
            // If unlocked, ensure we track timer if needed
            if(isTemporarilyUnlocked) {
                try {
                    val interval = AppBlockerServiceInfo.unlockedApps[detectedForegroundPackage]!! - System.currentTimeMillis()
                    // Only start cooldown for specific app if Deep Focus timer isn't the priority
                    // OR handle multiple timers. For now, we prioritize Deep Focus timer in UI.
                    if(!AppBlockerServiceInfo.deepFocus.isRunning) {
                        startCooldownTimer(detectedForegroundPackage, interval)
                    }
                } catch (_: Exception) { }
            }
        }
        return false
    }

    private fun showLockScreenFor(packageName: String) {
        currentLockedPackage = packageName
        isOverlayActive = true
        handler.post { showHomwScreenOverlay() }
    }

    private fun handleHomeScreenDetected(detectedForegroundPackage: String?) {
        if (currentLockedPackage != null) {
            currentLockedPackage = null
            isOverlayActive = false
        }
        lastForegroundPackage = detectedForegroundPackage
    }

    private fun processForegroundApp(foregroundPackage: String) {
        if (isLauncherApp(foregroundPackage)) {
            handleHomeScreenDetected(foregroundPackage)
            return
        }

        val isCurrentlyTemporarilyUnlocked = AppBlockerServiceInfo.unlockedApps.containsKey(foregroundPackage)

        if (isCurrentlyTemporarilyUnlocked) {
            if (currentLockedPackage == foregroundPackage) {
                currentLockedPackage = null
            }
            lastForegroundPackage = foregroundPackage
            return
        }

        if (lockedApps.contains(foregroundPackage) && !isOverlayActive) {
            showLockScreenFor(foregroundPackage)
        }

        lastForegroundPackage = foregroundPackage
    }

    private fun refreshHomeScreenOverlay() {
        if (isOverlayActive && currentLockedPackage != null) {
            val currentIntent = Intent(this, MainActivity::class.java)
            currentIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            currentIntent.putExtra("locked_package", currentLockedPackage)
            startActivity(currentIntent)
        }
    }

    private fun showHomwScreenOverlay() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        )
        intent.putExtra("locked_package", currentLockedPackage)
        startActivity(intent)
    }

    private fun isLauncherApp(packageName: String?): Boolean {
        if (packageName == null) return false
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    private fun getCurrentForegroundApp(): String? {
        var currentApp: String? = null
        val time = System.currentTimeMillis()
        val appList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 10,
            time
        )
        if (appList != null && appList.isNotEmpty()) {
            val sortedMap = sortedMapOf<Long, String>()
            for (usageStats in appList) {
                sortedMap[usageStats.lastTimeUsed] = usageStats.packageName
            }
            if (sortedMap.isNotEmpty()) {
                currentApp = sortedMap[sortedMap.lastKey()]
            }
        }
        return currentApp
    }

    // Placeholder to load standard Locked Apps when Deep Focus is NOT active
    private fun loadLockedApps() {
        // Implement your logic to load the standard blocklist from SharedPreferences or Room DB
        // For now, ensuring the list is clear or populated as per default needs
        lockedApps.clear()

        // Example: lockedApps.addAll(AppBlockerServiceInfo.savedLockedApps)
        // If you don't have a default list, this clears strict mode which is the intended "Stop" behavior

        Log.d(TAG, "Deep Focus stopped. Reverting to standard block list.")
    }

    private fun turnDeepFocus() {
        CoroutineScope(Dispatchers.IO).launch {
            val pm = applicationContext.packageManager
            val result = reloadApps(pm, applicationContext)

            if (result.isSuccess) {
                var allApps = result.getOrDefault(emptyList())
                val keyboardApps = getKeyboards(applicationContext)

                // Filter out exceptions, keyboard, and self
                allApps = allApps.filter {
                    !AppBlockerServiceInfo.deepFocus.exceptionApps.contains(it.packageName) &&
                            !keyboardApps.contains(it.packageName) &&
                            it.packageName != "neth.iecal.questphone"
                }

                withContext(Dispatchers.Main) {
                    lockedApps.clear()
                    lockedApps.addAll(allApps.map { it.packageName })
                    Log.d(TAG, "Deep Focus ON: Locked ${lockedApps.size} apps")
                    Toast.makeText(applicationContext, "Deep Focus Activated", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Helper to strictly stop Deep Focus
    private fun stopDeepFocus() {
        Log.d(TAG, "Stopping Deep Focus strictly.")
        AppBlockerServiceInfo.deepFocus.isRunning = false
        AppBlockerServiceInfo.deepFocus.exceptionApps = hashSetOf()
        AppBlockerServiceInfo.deepFocus.duration = 0

        // 1. Stop the visual timer
        stopCooldownTimer()

        // 2. Revert to standard locked apps
        loadLockedApps()

        // 3. Update Notification to default state
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        Toast.makeText(applicationContext, "Deep Focus Completed", Toast.LENGTH_SHORT).show()
    }

    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Broadcast received: ${intent?.action}")
            if (intent == null) return
            when (intent.action) {
                INTENT_ACTION_START_DEEP_FOCUS -> {
                    AppBlockerServiceInfo.deepFocus.exceptionApps =
                        intent.getStringArrayListExtra("exception")?.toHashSet() ?: hashSetOf()
                    AppBlockerServiceInfo.deepFocus.isRunning = true
                    val duration = intent.getLongExtra("duration", 0L)
                    AppBlockerServiceInfo.deepFocus.duration = duration

                    Log.d(TAG, "Starting deep focus for ${duration}ms")

                    // 1. Lock apps strictly
                    turnDeepFocus()

                    // 2. Start the lifecycle timer
                    startCooldownTimer("deepfocus", duration)
                    setReminderInMinutes(duration)
                }

                INTENT_ACTION_STOP_DEEP_FOCUS -> {
                    // Manual stop request
                    stopDeepFocus()
                }
            }
        }
    }

    private fun setReminderInMinutes(msFromNow: Long) {
        val triggerTimeMillis = System.currentTimeMillis() + msFromNow
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AppBlockerReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                } else {
                    Log.w(TAG, "Exact alarm permission not granted.")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}")
        }
    }

    private fun startCooldownTimer(packageName: String, duration: Long) {
        // Prevent overlapping timers for the same package
        if (timerRunningForPackage == packageName) return
        stopCooldownTimer() // Stop previous timer before starting new one

        val startTime = SystemClock.uptimeMillis()
        val endTime = startTime + duration

        updateTimerNotification(packageName, 0f, duration / 1000)

        isTimerRunning = true
        timerRunningForPackage = packageName

        timerRunnable = object : Runnable {
            override fun run() {
                val currentTime = SystemClock.uptimeMillis()
                val remainingMs = endTime - currentTime
                val remainingSeconds = remainingMs / 1000
                val progress = 1f - (remainingMs.toFloat() / duration.toFloat())

                if (remainingSeconds < 10 && remainingSeconds > 0) {
                    // Optional: Toast for final countdown
                }

                if (remainingSeconds > 0) {
                    updateTimerNotification(packageName, progress, remainingSeconds)
                    handler.postDelayed(this, 1000)
                } else {
                    Log.d(TAG, "Timer completed for $packageName")

                    // KEY CHANGE: Strict Check
                    if (packageName == "deepfocus") {
                        // If the Deep Focus timer ran out, we MUST end the session strictly
                        stopDeepFocus()
                    } else {
                        // Just a normal app unlock timer expiring
                        updateTimerNotification(packageName, 1f, 0)
                        handler.postDelayed({
                            stopCooldownTimer()
                        }, 2000)
                    }
                }
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun stopCooldownTimer() {
        timerRunnable?.let {
            handler.removeCallbacks(it)
        }
        isTimerRunning = false
        timerRunnable = null
        timerRunningForPackage = ""
        // Do not verify strict mode here, just clean up variables
        cancelTimerNotification()
    }

    @SuppressLint("DefaultLocale")
    private fun updateTimerNotification(
        packageName: String,
        progress: Float,
        remainingSeconds: Long
    ) {
        try {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val intent = if (packageName == "deepfocus") {
                Intent(this, MainActivity::class.java)
            } else {
                packageManager.getLaunchIntentForPackage(packageName) ?: Intent(this, MainActivity::class.java)
            }

            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val timeText = String.format("%d:%02d", minutes, seconds)

            val title = if (packageName == "deepfocus") "Focus Session Ongoing" else {
                val appName = try {
                    packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager).toString()
                } catch (e: Exception) { packageName }
                "Unlocked: $appName"
            }

            val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText("Time remaining: $timeText")
                .setProgress(100, (progress * 100).toInt(), false)
                .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority to avoid constant dinging
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true) // Prevent sound on every update

            notificationManager.notify(NOTIFICATION_ID, builder.build())

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification: ${e.message}")
        }
    }

    private fun cancelTimerNotification() {
        // Reset to default "Protecting your time" notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }
}

class AppBlockerReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // This receiver acts as a backup or a wake-up signal
        Log.d("AppBlockerReminder", "Alarm Fired - ensuring service is running")
        // Note: We do not strictly stop deep focus here; the service's internal runnable handles strict timing.
        // This ensures the service is alive to process the stop.
        val serviceIntent = Intent(context, AppBlockerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}