package app.mori.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplayTransitionEffects
import app.mori.reader.app.navigation.AppNavigationState
import app.mori.reader.features.audiobook.presentation.AudiobookIntent
import app.mori.reader.features.audiobook.presentation.AudiobookUiState
import app.mori.reader.features.bookshelf.presentation.BookshelfIntent
import app.mori.reader.features.bookshelf.presentation.BookshelfState
import app.mori.reader.features.dictionary.presentation.DictionaryIntent
import app.mori.reader.features.dictionary.presentation.DictionaryState
import app.mori.reader.features.reader.presentation.ReaderViewModel
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.features.settings.presentation.SettingsUiState
import app.mori.reader.ui.AppEffect
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
import app.mori.reader.ui.navigation.AppRoute
import app.mori.reader.ui.navigation.MainTabsContent
import app.mori.reader.ui.pages.reader.ReaderPage
import app.mori.reader.ui.pages.settings.AppearanceSettingsPage
import app.mori.reader.ui.pages.settings.AudioSettingsPage
import app.mori.reader.ui.pages.settings.DictionarySettingsPage
import app.mori.reader.ui.text.resolveString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppContent(
    state: AppState,
    bookshelfState: BookshelfState,
    dictionaryState: DictionaryState,
    audiobookUiState: AudiobookUiState,
    settingsUi: SettingsUiState,
    effects: Flow<AppEffect>,
    onIntent: (AppIntent) -> Unit,
    onBookshelfIntent: (BookshelfIntent) -> Unit,
    onDictionaryIntent: (DictionaryIntent) -> Unit,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onAudiobookIntent: (AudiobookIntent) -> Unit,
) {
    val showToast = rememberSystemToast()
    val navigationState = remember { AppNavigationState() }

    LaunchedEffect(effects, showToast) {
        effects.collectLatest { effect ->
            when (effect) {
                is AppEffect.ShowMessage -> showToast(effect.message.resolveString())
                is AppEffect.OpenReader -> navigationState.openReader(effect.bookId)
            }
        }
    }

    val rootTransitionEffects =
        remember {
            NavDisplayTransitionEffects(
                enableCornerClip = true,
                dimAmount = 0.5f,
                blockInputDuringTransition = true,
                popDirectionFollowsSwipeEdge = false,
            )
        }

    val rootEntries =
        rememberDecoratedNavEntries(
            backStack = navigationState.rootBackStack,
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider =
                entryProvider<NavKey> {
                    entry<AppRoute.Main> {
                        MainTabsContent(
                            home = bookshelfState,
                            dictionary = dictionaryState,
                            settings = state.settings,
                            audiobook = audiobookUiState,
                            navigationState = navigationState,
                            onIntent = onIntent,
                            onBookshelfIntent = onBookshelfIntent,
                            onDictionaryIntent = onDictionaryIntent,
                            onSettingsIntent = onSettingsIntent,
                            onAudiobookIntent = onAudiobookIntent,
                            onOpenAppearanceSettings = { navigationState.pushRoot(AppRoute.AppearanceSettings) },
                            onOpenDictionarySettings = { navigationState.pushRoot(AppRoute.DictionarySettings) },
                            onOpenAudioSettings = { navigationState.pushRoot(AppRoute.AudioSettings) },
                        )
                    }
                    entry<AppRoute.Reader> { route ->
                        val readerViewModel =
                            koinViewModel<ReaderViewModel>(
                                key = "reader-${route.bookId}",
                                parameters = { parametersOf(route.bookId) },
                            )
                        val readerState by readerViewModel.state.collectAsStateWithLifecycle()
                        ReaderPage(
                            reader = readerState,
                            settings = state.settings,
                            bookId = route.bookId,
                            onReaderIntent = readerViewModel::onIntent,
                            onSettingsIntent = onSettingsIntent,
                            onBack = navigationState::popRoot,
                        )
                    }
                    entry<AppRoute.DictionarySettings> {
                        DictionarySettingsPage(
                            settings = state.settings,
                            dictionaryState = settingsUi.dictionaryManagement,
                            onIntent = onSettingsIntent,
                            onBack = navigationState::popRoot,
                        )
                    }
                    entry<AppRoute.AppearanceSettings> {
                        AppearanceSettingsPage(
                            settings = state.settings,
                            onSettingsIntent = onSettingsIntent,
                            onBack = navigationState::popRoot,
                        )
                    }
                    entry<AppRoute.AudioSettings> {
                        AudioSettingsPage(
                            settings = state.settings,
                            settingsUi = settingsUi,
                            onIntent = onSettingsIntent,
                            onBack = navigationState::popRoot,
                        )
                    }
                },
        )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MiuixTheme.colorScheme.surface,
    ) {
        NavDisplay(
            entries = rootEntries,
            modifier = Modifier.fillMaxSize(),
            onBack = navigationState::popRoot,
            transitionEffects = rootTransitionEffects,
        )
    }
}
