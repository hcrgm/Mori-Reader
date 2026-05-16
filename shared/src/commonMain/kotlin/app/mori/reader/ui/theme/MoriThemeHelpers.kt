package app.mori.reader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.ThemeMode

@Composable
fun isMoriDarkTheme(): Boolean = MoriTheme.themeMode.resolveDarkTheme(isSystemInDarkTheme())

private fun ThemeMode.resolveDarkTheme(systemDarkTheme: Boolean): Boolean =
    when (this) {
        ThemeMode.System -> systemDarkTheme
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
