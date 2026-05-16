package app.mori.reader.ui

import app.mori.reader.data.settings.AppSettings

enum class AppTab {
    Home,
    Dictionary,
    Settings,
}

data class AppState(
    val settings: AppSettings = AppSettings(),
    val settingsLoaded: Boolean = false,
)
