package app.mori.reader.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import app.mori.reader.app.navigation.AppNavigationState
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.audiobook.presentation.AudiobookIntent
import app.mori.reader.features.audiobook.presentation.AudiobookUiState
import app.mori.reader.features.bookshelf.presentation.BookshelfIntent
import app.mori.reader.features.bookshelf.presentation.BookshelfState
import app.mori.reader.features.dictionary.presentation.DictionaryIntent
import app.mori.reader.features.dictionary.presentation.DictionaryState
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.navigation.MoriNavigationBar
import app.mori.reader.ui.components.navigation.MoriNavigationRail
import app.mori.reader.ui.layout.shouldShowWideLayout
import app.mori.reader.ui.pages.dictionary.DictionaryPage
import app.mori.reader.ui.pages.home.HomePage
import app.mori.reader.ui.pages.settings.SettingsPage
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MainTabsContent(
    home: BookshelfState,
    dictionary: DictionaryState,
    settings: AppSettings,
    audiobook: AudiobookUiState,
    navigationState: AppNavigationState,
    onIntent: (AppIntent) -> Unit,
    onBookshelfIntent: (BookshelfIntent) -> Unit,
    onDictionaryIntent: (DictionaryIntent) -> Unit,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onAudiobookIntent: (AudiobookIntent) -> Unit,
    onOpenAppearanceSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
) {
    val pagerState =
        rememberPagerState(
            initialPage = AppTab.Home.ordinal,
            pageCount = { AppTab.entries.size },
        )
    val coroutineScope = rememberCoroutineScope()
    val isWideScreen = shouldShowWideLayout()
    val density = LocalDensity.current
    val surfaceColor = MiuixTheme.colorScheme.surface
    val navigationBackdrop =
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    val selectedTab = AppTab.entries[pagerState.currentPage]

    LaunchedEffect(selectedTab, navigationState) {
        navigationState.onCurrentTabChanged(selectedTab)
    }

    val onTabSelected: (AppTab) -> Unit = { tab ->
        coroutineScope.launch {
            pagerState.animateScrollToPage(tab.ordinal)
        }
    }

    if (isWideScreen) {
        var navigationRailWidthPx by remember { mutableIntStateOf(0) }
        val navigationRailPadding =
            PaddingValues(
                start = with(density) { navigationRailWidthPx.toDp() },
            )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(surfaceColor),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .layerBackdrop(navigationBackdrop),
            ) {
                MainTabsPager(
                    home = home,
                    dictionary = dictionary,
                    settings = settings,
                    audiobook = audiobook,
                    fixedPadding = navigationRailPadding,
                    pagerState = pagerState,
                    navigationState = navigationState,
                    onIntent = onIntent,
                    onBookshelfIntent = onBookshelfIntent,
                    onDictionaryIntent = onDictionaryIntent,
                    onSettingsIntent = onSettingsIntent,
                    onAudiobookIntent = onAudiobookIntent,
                    onOpenAppearanceSettings = onOpenAppearanceSettings,
                    onOpenDictionarySettings = onOpenDictionarySettings,
                    onOpenAudioSettings = onOpenAudioSettings,
                    onWebViewVerticalScrollActiveChange = navigationState::onDictionaryScrollActiveChange,
                )
            }
            MoriNavigationRail(
                selectedTab = selectedTab,
                backdrop = navigationBackdrop,
                blurEnabled = settings.appearance.blurEnabled,
                onTabSelected = onTabSelected,
                modifier = Modifier.onSizeChanged { navigationRailWidthPx = it.width },
            )
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                MoriNavigationBar(
                    selectedTab = selectedTab,
                    backdrop = navigationBackdrop,
                    blurEnabled = settings.appearance.blurEnabled,
                    onTabSelected = onTabSelected,
                )
            },
        ) { fixedPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .layerBackdrop(navigationBackdrop),
            ) {
                MainTabsPager(
                    home = home,
                    dictionary = dictionary,
                    settings = settings,
                    audiobook = audiobook,
                    fixedPadding = fixedPadding,
                    pagerState = pagerState,
                    navigationState = navigationState,
                    onIntent = onIntent,
                    onBookshelfIntent = onBookshelfIntent,
                    onDictionaryIntent = onDictionaryIntent,
                    onSettingsIntent = onSettingsIntent,
                    onAudiobookIntent = onAudiobookIntent,
                    onOpenAppearanceSettings = onOpenAppearanceSettings,
                    onOpenDictionarySettings = onOpenDictionarySettings,
                    onOpenAudioSettings = onOpenAudioSettings,
                    onWebViewVerticalScrollActiveChange = navigationState::onDictionaryScrollActiveChange,
                )
            }
        }
    }
}

@Composable
private fun MainTabsPager(
    home: BookshelfState,
    dictionary: DictionaryState,
    settings: AppSettings,
    audiobook: AudiobookUiState,
    fixedPadding: PaddingValues,
    pagerState: PagerState,
    navigationState: AppNavigationState,
    onIntent: (AppIntent) -> Unit,
    onBookshelfIntent: (BookshelfIntent) -> Unit,
    onDictionaryIntent: (DictionaryIntent) -> Unit,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onAudiobookIntent: (AudiobookIntent) -> Unit,
    onOpenAppearanceSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onWebViewVerticalScrollActiveChange: (Boolean) -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 0,
        userScrollEnabled =
            navigationState.canSwipeTabs(
                hasDictionaryPopup = dictionary.popupStack.isNotEmpty(),
            ),
        verticalAlignment = androidx.compose.ui.Alignment.Top,
    ) { page ->
        when (AppTab.entries[page]) {
            AppTab.Home -> {
                HomePage(
                    home = home,
                    settings = settings,
                    audiobook = audiobook,
                    fixedPadding = fixedPadding,
                    onBookshelfIntent = onBookshelfIntent,
                    onAudiobookIntent = onAudiobookIntent,
                    onOpenBook = { onIntent(AppIntent.OpenBook(it)) },
                )
            }

            AppTab.Dictionary -> {
                DictionaryPage(
                    dictionaryState = dictionary,
                    settings = settings,
                    fixedPadding = fixedPadding,
                    onDictionaryIntent = onDictionaryIntent,
                    onWebViewVerticalScrollActiveChange = onWebViewVerticalScrollActiveChange,
                )
            }

            AppTab.Settings -> {
                SettingsPage(
                    settings = settings,
                    fixedPadding = fixedPadding,
                    onOpenAppearanceSettings = onOpenAppearanceSettings,
                    onOpenDictionarySettings = onOpenDictionarySettings,
                    onOpenAudioSettings = onOpenAudioSettings,
                )
            }
        }
    }
}
