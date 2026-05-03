package app.mori.reader.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.mori.reader.data.settings.ThemeMode
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun AppTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val controller = remember(themeMode) {
        ThemeController(
            colorSchemeMode = themeMode.toColorSchemeMode(),
        )
    }

    MiuixTheme(
        controller = controller,
        smoothRounding = true,
        content = content,
    )
}

private fun ThemeMode.toColorSchemeMode(): ColorSchemeMode =
    when (this) {
        ThemeMode.System -> ColorSchemeMode.System
        ThemeMode.Light -> ColorSchemeMode.Light
        ThemeMode.Dark -> ColorSchemeMode.Dark
    }
