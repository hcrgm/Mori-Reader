package app.mori.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        loadSettingsThenShowContent()
    }

    private fun loadSettingsThenShowContent() {
        Thread {
            val initialSettings = readInitialSettings()
            runOnUiThread {
                setContent {
                    App(initialSettings = initialSettings)
                }
            }
        }.start()
    }

    private fun readInitialSettings(): AppSettings =
        try {
            val settingsRepository = GlobalContext.get().get<SettingsRepository>()
            runBlocking {
                settingsRepository.settings.first()
            }
        } catch (_: Throwable) {
            AppSettings()
        }
}
