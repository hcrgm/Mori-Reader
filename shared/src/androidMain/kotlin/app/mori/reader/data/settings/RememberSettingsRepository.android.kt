package app.mori.reader.data.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberSettingsRepository(): SettingsRepository {
    val applicationContext = LocalContext.current.applicationContext
    return remember(applicationContext) {
        createAndroidSettingsRepository(applicationContext)
    }
}
