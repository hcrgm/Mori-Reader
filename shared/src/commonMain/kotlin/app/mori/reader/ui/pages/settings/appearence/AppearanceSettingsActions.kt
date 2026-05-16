package app.mori.reader.ui.pages.settings.appearence

import app.mori.reader.data.settings.LanguageMode
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.data.settings.UiThemeEngine

internal data class AppearanceSettingsActions(
    val onThemeModeSelected: (ThemeMode) -> Unit,
    val onUiThemeEngineSelected: (UiThemeEngine) -> Unit,
    val onLanguageModeSelected: (LanguageMode) -> Unit,
    val onMonetEnabledChanged: (Boolean) -> Unit,
    val onMonetKeyColorSelected: (Long) -> Unit,
    val onUiScalePercentChanged: (Int) -> Unit,
    val onBlurEnabledChanged: (Boolean) -> Unit,
)
