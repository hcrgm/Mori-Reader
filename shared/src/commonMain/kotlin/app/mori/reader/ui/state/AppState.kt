package app.mori.reader.ui

import androidx.compose.ui.graphics.vector.ImageVector
import app.mori.reader.data.settings.AppSettings
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Album
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Translate

enum class AppTab(
    val icon: ImageVector,
) {
    Home(
        icon = MiuixIcons.Album,
    ),
    Dictionary(
        icon = MiuixIcons.Translate,
    ),
    Settings(
        icon = MiuixIcons.Settings,
    ),
}

data class AppState(
    val settings: AppSettings = AppSettings(),
    val settingsLoaded: Boolean = false,
)
