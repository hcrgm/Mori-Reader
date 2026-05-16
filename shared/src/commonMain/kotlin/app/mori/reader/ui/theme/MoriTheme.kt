package app.mori.reader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.data.settings.UiThemeEngine
import top.yukonga.miuix.kmp.theme.MiuixTheme

object MoriTheme {
    val uiThemeEngine: UiThemeEngine
        @Composable
        @ReadOnlyComposable
        get() = LocalMoriUiThemeEngine.current

    val themeMode: ThemeMode
        @Composable
        @ReadOnlyComposable
        get() = LocalMoriThemeMode.current
}

@Composable
internal fun ProvideMoriTheme(
    themeMode: ThemeMode,
    uiThemeEngine: UiThemeEngine,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMoriThemeMode provides themeMode,
        LocalMoriUiThemeEngine provides uiThemeEngine,
        content = content,
    )
}

@Composable
fun moriSurfaceColor() =
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> MiuixTheme.colorScheme.surface
        UiThemeEngine.Material -> MaterialTheme.colorScheme.surface
    }

private val LocalMoriUiThemeEngine =
    staticCompositionLocalOf {
        UiThemeEngine.Miuix
    }

private val LocalMoriThemeMode =
    staticCompositionLocalOf {
        ThemeMode.System
    }
