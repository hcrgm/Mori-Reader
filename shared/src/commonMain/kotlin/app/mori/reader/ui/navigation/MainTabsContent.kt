package app.mori.reader.ui.navigation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import app.mori.reader.app.navigation.AppNavigationState
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.anki.presentation.AnkiIntent
import app.mori.reader.features.anki.presentation.AnkiState
import app.mori.reader.features.audiobook.presentation.AudiobookIntent
import app.mori.reader.features.audiobook.presentation.AudiobookUiState
import app.mori.reader.features.bookshelf.presentation.BookshelfIntent
import app.mori.reader.features.bookshelf.presentation.BookshelfState
import app.mori.reader.features.dictionary.presentation.DictionaryIntent
import app.mori.reader.features.dictionary.presentation.DictionaryState
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.navigation.eInkPagerSwipeModifier
import app.mori.reader.ui.components.scaffold.MoriMainTabsScaffold
import app.mori.reader.ui.layout.shouldShowWideLayout
import app.mori.reader.ui.pages.dictionary.DictionaryPage
import app.mori.reader.ui.pages.home.HomePage
import app.mori.reader.ui.pages.settings.SettingsPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
internal fun MainTabsContent(
    home: BookshelfState,
    dictionary: DictionaryState,
    settings: AppSettings,
    audiobook: AudiobookUiState,
    ankiState: AnkiState,
    navigationState: AppNavigationState,
    onIntent: (AppIntent) -> Unit,
    onBookshelfIntent: (BookshelfIntent) -> Unit,
    onDictionaryIntent: (DictionaryIntent) -> Unit,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onAudiobookIntent: (AudiobookIntent) -> Unit,
    onAnkiIntent: (AnkiIntent) -> Unit,
    onOpenAppearanceSettings: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onOpenAnkiSettings: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val pagerState =
        rememberPagerState(
            initialPage = AppTab.Home.ordinal,
            pageCount = { AppTab.entries.size },
        )
    val coroutineScope = rememberCoroutineScope()
    val reduceMotion =
        settings.appearance.uiThemeEngine == UiThemeEngine.Material &&
            settings.appearance.materialEInkMode
    val mainPagerState = rememberMoriMainPagerState(pagerState, coroutineScope)
    val isWideScreen = shouldShowWideLayout()
    val selectedTab = AppTab.entries[mainPagerState.selectedPage]

    LaunchedEffect(selectedTab, navigationState) {
        navigationState.onCurrentTabChanged(selectedTab)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            mainPagerState.syncPage()
        }
    }

    val onTabSelected: (AppTab) -> Unit = { tab ->
        mainPagerState.animateToPage(tab.ordinal, animate = !reduceMotion)
    }

    MoriMainTabsScaffold(
        isWideScreen = isWideScreen,
        blurEnabled = settings.appearance.blurEnabled,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
    ) { fixedPadding ->
        MainTabsPager(
            home = home,
            dictionary = dictionary,
            settings = settings,
            audiobook = audiobook,
            ankiState = ankiState,
            fixedPadding = fixedPadding,
            pagerState = pagerState,
            reduceMotion = reduceMotion,
            navigationState = navigationState,
            onIntent = onIntent,
            onBookshelfIntent = onBookshelfIntent,
            onDictionaryIntent = onDictionaryIntent,
            onSettingsIntent = onSettingsIntent,
            onAudiobookIntent = onAudiobookIntent,
            onAnkiIntent = onAnkiIntent,
            onOpenAppearanceSettings = onOpenAppearanceSettings,
            onOpenReaderSettings = onOpenReaderSettings,
            onOpenDictionarySettings = onOpenDictionarySettings,
            onOpenAudioSettings = onOpenAudioSettings,
            onOpenAnkiSettings = onOpenAnkiSettings,
            onOpenAbout = onOpenAbout,
            onPageSwipeChange = { page -> mainPagerState.animateToPage(page, animate = false) },
            onWebViewVerticalScrollActiveChange = navigationState::onDictionaryScrollActiveChange,
        )
    }
}

private class MoriMainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(
        targetIndex: Int,
        animate: Boolean,
    ) {
        if (targetIndex == selectedPage) return

        navJob?.cancel()

        selectedPage = targetIndex
        isNavigating = true

        if (!animate) {
            navJob =
                coroutineScope.launch {
                    val myJob = coroutineContext.job
                    try {
                        pagerState.scrollToPage(targetIndex)
                    } finally {
                        if (navJob == myJob) {
                            isNavigating = false
                            if (pagerState.currentPage != targetIndex) {
                                selectedPage = pagerState.currentPage
                            }
                        }
                    }
                }
            return
        }

        val distance = abs(targetIndex - pagerState.currentPage).coerceAtLeast(2)
        val duration = 100 * distance + 100
        val layoutInfo = pagerState.layoutInfo
        val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
        val currentDistanceInPages = targetIndex - pagerState.currentPage - pagerState.currentPageOffsetFraction
        val scrollPixels = currentDistanceInPages * pageSize

        navJob =
            coroutineScope.launch {
                val myJob = coroutineContext.job
                try {
                    pagerState.animateScrollBy(
                        value = scrollPixels,
                        animationSpec = tween(easing = EaseInOut, durationMillis = duration),
                    )
                } finally {
                    if (navJob == myJob) {
                        isNavigating = false
                        if (pagerState.currentPage != targetIndex) {
                            selectedPage = pagerState.currentPage
                        }
                    }
                }
            }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

@Composable
private fun rememberMoriMainPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
): MoriMainPagerState =
    remember(pagerState, coroutineScope) {
        MoriMainPagerState(pagerState, coroutineScope)
    }

@Composable
private fun MainTabsPager(
    home: BookshelfState,
    dictionary: DictionaryState,
    settings: AppSettings,
    audiobook: AudiobookUiState,
    ankiState: AnkiState,
    fixedPadding: PaddingValues,
    pagerState: PagerState,
    reduceMotion: Boolean,
    navigationState: AppNavigationState,
    onIntent: (AppIntent) -> Unit,
    onBookshelfIntent: (BookshelfIntent) -> Unit,
    onDictionaryIntent: (DictionaryIntent) -> Unit,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onAudiobookIntent: (AudiobookIntent) -> Unit,
    onAnkiIntent: (AnkiIntent) -> Unit,
    onOpenAppearanceSettings: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onOpenAnkiSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onPageSwipeChange: (Int) -> Unit,
    onWebViewVerticalScrollActiveChange: (Boolean) -> Unit,
) {
    val defaultOverscrollEffect = rememberOverscrollEffect()
    val defaultFlingBehavior = PagerDefaults.flingBehavior(state = pagerState)
    val instantFlingBehavior =
        PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = tween(durationMillis = 0),
        )
    val flingBehavior = if (reduceMotion) instantFlingBehavior else defaultFlingBehavior
    HorizontalPager(
        state = pagerState,
        modifier =
            Modifier
                .fillMaxSize()
                .eInkPagerSwipeModifier(
                    enabled = false,
                    currentPage = pagerState.currentPage,
                    pageCount = AppTab.entries.size,
                    onPageChange = onPageSwipeChange,
                ),
        beyondViewportPageCount = AppTab.entries.lastIndex,
        flingBehavior = flingBehavior,
        userScrollEnabled =
            !reduceMotion &&
                navigationState.canSwipeTabs(
                    hasDictionaryPopup = dictionary.popupStack.isNotEmpty(),
                ),
        overscrollEffect = if (reduceMotion) null else defaultOverscrollEffect,
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
                    ankiState = ankiState,
                    fixedPadding = fixedPadding,
                    onDictionaryIntent = onDictionaryIntent,
                    onAnkiIntent = onAnkiIntent,
                    onWebViewVerticalScrollActiveChange = onWebViewVerticalScrollActiveChange,
                )
            }

            AppTab.Settings -> {
                SettingsPage(
                    settings = settings,
                    fixedPadding = fixedPadding,
                    onOpenAppearanceSettings = onOpenAppearanceSettings,
                    onOpenReaderSettings = onOpenReaderSettings,
                    onOpenDictionarySettings = onOpenDictionarySettings,
                    onOpenAudioSettings = onOpenAudioSettings,
                    onOpenAnkiSettings = onOpenAnkiSettings,
                    onOpenAbout = onOpenAbout,
                )
            }
        }
    }
}
