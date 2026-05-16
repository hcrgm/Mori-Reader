package app.mori.reader

import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.LanguageMode

expect fun platform(): String

data class MoriAppInfo(
    val appName: String,
    val versionName: String,
    val versionCode: Long,
)

@Composable
expect fun rememberMoriAppInfo(): MoriAppInfo

@Composable
expect fun ApplyLanguageModeEffect(mode: LanguageMode)

@Composable
expect fun AppLocaleEnvironment(
    mode: LanguageMode,
    content: @Composable () -> Unit,
)
