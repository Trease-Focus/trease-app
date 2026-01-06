package neth.iecal.trease

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatOverline
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission
import androidx.core.content.edit
import dev.pranav.appintro.AppIntro
import dev.pranav.appintro.IntroPage
import neth.iecal.trease.ui.theme.MainTheme


class AppIntroActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainTheme {
                AppIntroScreen()
            }
        }
    }
}

@SuppressLint("BatteryLife")
@Composable
fun AppIntroScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val prefs = context.getSharedPreferences("trease_prefs", Context.MODE_PRIVATE)

    val onFinishCallback = {
        prefs.edit { putBoolean("first_run", false) }

        activity!!.finish()
    }

    val pages = listOf(
        IntroPage(
            title = "Trease",
            description = "Trease helps you focus",
            painter = painterResource(R.drawable.grid),
            backgroundColor = Color(0xFF24623C),
            contentColor = Color.White,
            onNext = { true }
        ),

        IntroPage(
            title = "Usage Statistics",
            description = "Allow Trease to access your app usage data to monitor and limit your app usage effectively.",
            icon = Icons.Rounded.QueryStats,
            backgroundColor = Color(0xFFC2A72C),
            contentColor = Color.White,
            onNext = {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
                val hasPerm = if (mode == AppOpsManager.MODE_DEFAULT) {
                    checkCallingOrSelfPermission(context, Manifest.permission.PACKAGE_USAGE_STATS) == PermissionChecker.PERMISSION_GRANTED
                } else {
                    mode == AppOpsManager.MODE_ALLOWED
                }

                if (!hasPerm) {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                    false
                } else true
            }
        ),

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            IntroPage(
                title = "Notifications Permission",
                description = "Allow Trease to send you reminders and alerts to help you stay focused.",
                icon = Icons.Default.NotificationsNone,
                backgroundColor = Color(0xFFFFD740),
                contentColor = Color.Black,
                onNext = {
                    if (checkCallingOrSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PermissionChecker.PERMISSION_GRANTED
                    ) {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                        false
                    } else { true}
                }
            )
        } else null,


        IntroPage(
            title = "Overlays Permission",
            description = "Allow Trease to draw over other apps to effectively block distracting applications when needed.",
            icon = Icons.Default.FormatOverline,
            backgroundColor = Color(0xFFFF9A63),
            contentColor = Color.White,
            onNext = {
                if (!Settings.canDrawOverlays(context)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    context.startActivity(intent)
                    false
                } else true
            }
        )
    ).filterNotNull()

    AppIntro(
        pages = pages,
        onFinish = onFinishCallback,
        onSkip = { activity?.finishAffinity() },
        showSkipButton = false,
        useAnimatedPager = true,
        nextButtonText = "Next",
        finishButtonText = "Get Started"
    )
}
