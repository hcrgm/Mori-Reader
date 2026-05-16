package app.mori.reader.ui.pages.settings.dictionary

import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.settings.presentation.DictionaryManagementState
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.ui.theme.MoriTheme

@Composable
fun DictionarySettingsPage(
    settings: AppSettings,
    dictionaryState: DictionaryManagementState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixDictionarySettingsPage(
                settings = settings,
                dictionaryState = dictionaryState,
                onIntent = onIntent,
                onBack = onBack,
            )
        }

        UiThemeEngine.Material -> {
            MaterialDictionarySettingsPage(
                settings = settings,
                dictionaryState = dictionaryState,
                onIntent = onIntent,
                onBack = onBack,
            )
        }
    }
}
