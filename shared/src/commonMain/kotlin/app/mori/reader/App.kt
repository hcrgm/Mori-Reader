package app.mori.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.mori.reader.data.anki.rememberAnkiRepository
import app.mori.reader.data.audio.rememberAudioRepository
import app.mori.reader.data.book.rememberBookRepository
import app.mori.reader.data.dictionary.rememberDictionaryRepository
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.rememberSettingsRepository
import app.mori.reader.ui.AppViewModel
import app.mori.reader.ui.theme.AppTheme

@Composable
fun App(initialSettings: AppSettings? = null) {
    val settingsRepository = rememberSettingsRepository()
    val dictionaryRepository = rememberDictionaryRepository()
    val audioRepository = rememberAudioRepository()
    val ankiRepository = rememberAnkiRepository()
    val bookRepository = rememberBookRepository()
    val viewModel = viewModel<AppViewModel> {
        AppViewModel(
            settingsRepository = settingsRepository,
            dictionaryRepository = dictionaryRepository,
            audioRepository = audioRepository,
            ankiRepository = ankiRepository,
            bookRepository = bookRepository,
            initialSettings = initialSettings,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (!state.settingsLoaded) {
        return
    }

    AppLocaleEnvironment(mode = state.settings.languageMode) {
        ApplyLanguageModeEffect(state.settings.languageMode)
        AppTheme(themeMode = state.settings.themeMode) {
            AppContent(
                state = state,
                effects = viewModel.effects,
                onIntent = viewModel::onIntent,
            )
        }
    }
}
