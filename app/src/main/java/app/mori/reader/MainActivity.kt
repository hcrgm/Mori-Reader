package app.mori.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.createAndroidSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private var settingsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen().setKeepOnScreenCondition { !webViewReady || !settingsReady }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        loadSettingsThenShowContent()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun loadSettingsThenShowContent() {
        Thread {
            val initialSettings = readInitialSettings()
            runOnUiThread {
                settingsReady = true
                setContent {
                    App(initialSettings = initialSettings)
                }
            }
        }.start()
    }

    private fun readInitialSettings(): AppSettings =
        try {
            runBlocking {
                createAndroidSettingsRepository(applicationContext).settings.first()
            }
        } catch (_: Throwable) {
            AppSettings()
        }
}
