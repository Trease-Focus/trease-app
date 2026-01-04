package neth.iecal.trease

import android.content.Context
import neth.iecal.trease.data.DeepFocus

const val INTENT_ACTION_START_DEEP_FOCUS = "launcher.launcher.start.deepfocus"
const val INTENT_ACTION_STOP_DEEP_FOCUS = "launcher.launcher.stop.deepfocus"

object AppBlockerServiceInfo{
    var appBlockerService: AppBlockerService? = null
    var isUsingAccessibilityService = false
    val deepFocus = DeepFocus()

    // Store the unlock time for each app that is temporarily unlocked
    var unlockedApps = mutableMapOf<String, Long>()

}

fun reloadServiceInfo(context: Context){
    val sp = context.getSharedPreferences("service_info", Context.MODE_PRIVATE)
    AppBlockerServiceInfo.isUsingAccessibilityService = sp.getBoolean("is_using_accessibility",false)
}
