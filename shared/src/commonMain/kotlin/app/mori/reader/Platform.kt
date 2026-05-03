package app.mori.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import app.mori.reader.data.settings.LanguageMode

expect fun platform(): String

@Composable
expect fun ApplyLanguageModeEffect(mode: LanguageMode)

@Composable
expect fun AppLocaleEnvironment(
    mode: LanguageMode,
    content: @Composable () -> Unit,
)
