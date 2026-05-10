package app.mori.reader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import app.mori.reader.data.settings.ThemeMode
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun AppTheme(
    themeMode: ThemeMode,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    content: @Composable () -> Unit,
) {
    val darkTheme = themeMode.isDarkTheme(isSystemInDarkTheme())
    val controller =
        remember(themeMode, monetEnabled, monetKeyColor) {
            ThemeController(
                colorSchemeMode = themeMode.toColorSchemeMode(monetEnabled),
                keyColor = monetKeyColor.takeIf { it != 0L }?.let(::Color),
            )
        }

    MiuixTheme(
        controller = controller,
        smoothRounding = true,
        content = {
            ApplySystemBarsThemeEffect(darkTheme = darkTheme)
            content()
        },
    )
}

@Composable
expect fun ApplySystemBarsThemeEffect(darkTheme: Boolean)

private fun ThemeMode.isDarkTheme(systemDarkTheme: Boolean): Boolean =
    when (this) {
        ThemeMode.System -> systemDarkTheme
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

private fun ThemeMode.toColorSchemeMode(monetEnabled: Boolean): ColorSchemeMode =
    when (this) {
        ThemeMode.System -> if (monetEnabled) ColorSchemeMode.MonetSystem else ColorSchemeMode.System
        ThemeMode.Light -> if (monetEnabled) ColorSchemeMode.MonetLight else ColorSchemeMode.Light
        ThemeMode.Dark -> if (monetEnabled) ColorSchemeMode.MonetDark else ColorSchemeMode.Dark
    }
