package app.mori.reader.ui.theme

import app.mori.reader.data.settings.AppearanceSettings
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.data.settings.UiThemeEngine

data class MoriThemeState(
    val themeMode: ThemeMode,
    val uiThemeEngine: UiThemeEngine,
    val uiScalePercent: Int,
    val monetEnabled: Boolean,
    val monetKeyColor: Long,
    val blurEnabled: Boolean,
)

fun AppearanceSettings.toMoriThemeState(): MoriThemeState =
    MoriThemeState(
        themeMode = themeMode,
        uiThemeEngine = uiThemeEngine,
        uiScalePercent = uiScalePercent,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
        blurEnabled = blurEnabled,
    )
