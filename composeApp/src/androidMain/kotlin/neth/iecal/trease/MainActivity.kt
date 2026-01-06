package neth.iecal.trease

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        TreaseContext.init(this)

        val prefs = getSharedPreferences("trease_prefs", Context.MODE_PRIVATE)

        if (prefs.getBoolean("first_run", true)) {
            startActivity(Intent(this, AppIntroActivity::class.java))
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
