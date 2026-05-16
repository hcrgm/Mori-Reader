package app.mori.reader.ui.pages.settings.anki

import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.anki.presentation.AnkiIntent
import app.mori.reader.features.anki.presentation.AnkiState
import app.mori.reader.features.settings.presentation.DictionaryManagementState
import app.mori.reader.ui.theme.MoriTheme

@Composable
fun AnkiSettingsPage(
    settings: AppSettings,
    ankiState: AnkiState,
    dictionaryState: DictionaryManagementState,
    onIntent: (AnkiIntent) -> Unit,
    onBack: () -> Unit,
) {
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixAnkiSettingsPage(
                settings = settings,
                ankiState = ankiState,
                dictionaryState = dictionaryState,
                onIntent = onIntent,
                onBack = onBack,
            )
        }

        UiThemeEngine.Material -> {
            MaterialAnkiSettingsPage(
                settings = settings,
                ankiState = ankiState,
                dictionaryState = dictionaryState,
                onIntent = onIntent,
                onBack = onBack,
            )
        }
    }
}
