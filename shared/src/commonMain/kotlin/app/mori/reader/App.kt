package app.mori.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.anki.presentation.AnkiViewModel
import app.mori.reader.features.audiobook.presentation.AudiobookViewModel
import app.mori.reader.features.bookshelf.presentation.BookshelfViewModel
import app.mori.reader.features.dictionary.presentation.DictionaryViewModel
import app.mori.reader.features.settings.presentation.SettingsViewModel
import app.mori.reader.ui.AppEffect
import app.mori.reader.ui.RootViewModel
import app.mori.reader.ui.theme.AppTheme
import app.mori.reader.ui.theme.toMoriThemeState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun App(initialSettings: AppSettings? = null) {
    val viewModel = koinViewModel<RootViewModel>(parameters = { parametersOf(initialSettings) })
    val dictionaryViewModel = koinViewModel<DictionaryViewModel>()
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val ankiViewModel = koinViewModel<AnkiViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val dictionaryState by dictionaryViewModel.state.collectAsStateWithLifecycle()
    val settingsUiState by settingsViewModel.state.collectAsStateWithLifecycle()
    val ankiState by ankiViewModel.state.collectAsStateWithLifecycle()

    if (!state.settingsLoaded) {
        return
    }

    val bookshelfViewModel = koinViewModel<BookshelfViewModel>()
    val audiobookViewModel =
        koinViewModel<AudiobookViewModel>(
            parameters = { parametersOf(state.settings.sasayaki.preferredStorageMode) },
        )
    val homeState by bookshelfViewModel.state.collectAsStateWithLifecycle()
    val audiobookState by audiobookViewModel.state.collectAsStateWithLifecycle()

    AppLocaleEnvironment(mode = state.settings.appearance.languageMode) {
        ApplyLanguageModeEffect(state.settings.appearance.languageMode)
        AppTheme(
            themeState = state.settings.appearance.toMoriThemeState(),
        ) {
            AppContent(
                state = state,
                bookshelfState = homeState,
                dictionaryState = dictionaryState,
                audiobookUiState = audiobookState,
                settingsUiState = settingsUiState,
                ankiState = ankiState,
                effects =
                    merge(
                        viewModel.effects,
                        settingsViewModel.effects,
                        audiobookViewModel.effects.map { AppEffect.ShowMessage(it) },
                        ankiViewModel.effects,
                    ),
                onIntent = viewModel::onIntent,
                onBookshelfIntent = bookshelfViewModel::onIntent,
                onDictionaryIntent = dictionaryViewModel::onIntent,
                onSettingsIntent = settingsViewModel::onIntent,
                onAudiobookIntent = audiobookViewModel::onIntent,
                onAnkiIntent = ankiViewModel::onIntent,
            )
        }
    }
}
