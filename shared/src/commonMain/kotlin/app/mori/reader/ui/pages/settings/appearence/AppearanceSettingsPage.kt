package app.mori.reader.ui.pages.settings.appearence

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.ui.theme.MoriTheme

@Composable
fun AppearanceSettingsPage(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    val actions =
        remember(onSettingsIntent) {
            AppearanceSettingsActions(
                onThemeModeSelected = { onSettingsIntent(SettingsIntent.SetThemeMode(it)) },
                onUiThemeEngineSelected = { onSettingsIntent(SettingsIntent.SetUiThemeEngine(it)) },
                onLanguageModeSelected = { onSettingsIntent(SettingsIntent.SetLanguageMode(it)) },
                onMonetEnabledChanged = { onSettingsIntent(SettingsIntent.SetMonetEnabled(it)) },
                onMonetKeyColorSelected = { onSettingsIntent(SettingsIntent.SetMonetKeyColor(it)) },
                onUiScalePercentChanged = { onSettingsIntent(SettingsIntent.SetUiScalePercent(it)) },
                onBlurEnabledChanged = { onSettingsIntent(SettingsIntent.SetBlurEnabled(it)) },
            )
        }

    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixAppearanceSettingsPage(
                settings = settings,
                actions = actions,
                onBack = onBack,
            )
        }

        UiThemeEngine.Material -> {
            MaterialAppearanceSettingsPage(
                settings = settings,
                actions = actions,
                onBack = onBack,
            )
        }
    }
}
