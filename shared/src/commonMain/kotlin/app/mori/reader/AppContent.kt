package app.mori.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.NavDisplayTransitionEffects
import app.mori.reader.ui.AppEffect
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.navigation.MoriNavigationBar
import app.mori.reader.ui.components.navigation.MoriNavigationRail
import app.mori.reader.ui.layout.shouldShowWideLayout
import app.mori.reader.ui.navigation.AppRoute
import app.mori.reader.ui.navigation.toRoute
import app.mori.reader.ui.pages.dictionary.DictionaryPage
import app.mori.reader.ui.pages.home.HomePage
import app.mori.reader.ui.pages.reader.ReaderPage
import app.mori.reader.ui.pages.settings.AnkiSettingsPage
import app.mori.reader.ui.pages.settings.AppearanceSettingsPage
import app.mori.reader.ui.pages.settings.AudioSettingsPage
import app.mori.reader.ui.pages.settings.DictionarySettingsPage
import app.mori.reader.ui.pages.settings.SettingsPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AppContent(
    state: AppState,
    effects: Flow<AppEffect>,
    onIntent: (AppIntent) -> Unit,
) {
    val showToast = rememberSystemToast()
    var rootBackStack by remember { mutableStateOf(listOf<NavKey>(AppRoute.Main)) }

    LaunchedEffect(effects, showToast) {
        effects.collectLatest { effect ->
            when (effect) {
                is AppEffect.ShowMessage -> showToast(effect.message)
                is AppEffect.OpenReader -> {
                    rootBackStack = rootBackStack + AppRoute.Reader(effect.bookId)
                }
            }
        }
    }

    val rootTransitionEffects = remember {
        NavDisplayTransitionEffects(
            enableCornerClip = true,
            dimAmount = 0.5f,
            blockInputDuringTransition = true,
            popDirectionFollowsSwipeEdge = false,
        )
    }

    val rootEntries = rememberDecoratedNavEntries(
        backStack = rootBackStack,
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = entryProvider<NavKey> {
            entry<AppRoute.Main> {
                MainTabsContent(
                    state = state,
                    message = null,
                    onIntent = onIntent,
                    onOpenAppearanceSettings = {
                        rootBackStack = rootBackStack + AppRoute.AppearanceSettings
                    },
                    onOpenDictionarySettings = {
                        rootBackStack = rootBackStack + AppRoute.DictionarySettings
                    },
                    onOpenAudioSettings = {
                        rootBackStack = rootBackStack + AppRoute.AudioSettings
                    },
                    onOpenAnkiSettings = { rootBackStack = rootBackStack + AppRoute.AnkiSettings },
                )
            }
            entry<AppRoute.Reader> { route ->
                ReaderPage(
                    state = state,
                    bookId = route.bookId,
                    onIntent = onIntent,
                    onBack = {
                        if (rootBackStack.size > 1) {
                            rootBackStack = rootBackStack.dropLast(1)
                        }
                    },
                )
            }
            entry<AppRoute.DictionarySettings> {
                DictionarySettingsPage(
                    state = state,
                    message = null,
                    onIntent = onIntent,
                    onBack = {
                        if (rootBackStack.size > 1) {
                            rootBackStack = rootBackStack.dropLast(1)
                        }
                    },
                )
            }
            entry<AppRoute.AppearanceSettings> {
                AppearanceSettingsPage(
                    state = state,
                    message = null,
                    onIntent = onIntent,
                    onBack = {
                        if (rootBackStack.size > 1) {
                            rootBackStack = rootBackStack.dropLast(1)
                        }
                    },
                )
            }
            entry<AppRoute.AudioSettings> {
                AudioSettingsPage(
                    state = state,
                    message = null,
                    onIntent = onIntent,
                    onBack = {
                        if (rootBackStack.size > 1) {
                            rootBackStack = rootBackStack.dropLast(1)
                        }
                    },
                )
            }
            entry<AppRoute.AnkiSettings> {
                AnkiSettingsPage(
                    state = state,
                    message = null,
                    onIntent = onIntent,
                    onBack = {
                        if (rootBackStack.size > 1) {
                            rootBackStack = rootBackStack.dropLast(1)
                        }
                    },
                )
            }
        },
    )

    NavDisplay(
        entries = rootEntries,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            if (rootBackStack.size > 1) {
                rootBackStack = rootBackStack.dropLast(1)
            }
        },
        transitionEffects = rootTransitionEffects,
    )
}

@Composable
private fun MainTabsContent(
    state: AppState,
    message: String?,
    onIntent: (AppIntent) -> Unit,
    onOpenAppearanceSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onOpenAnkiSettings: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentTab.ordinal,
        pageCount = { AppTab.entries.size },
    )
    val isWideScreen = shouldShowWideLayout()
    val coroutineScope = rememberCoroutineScope()
    val routes = remember { AppTab.entries.map { it.toRoute() } }
    var selectedPage by remember { mutableIntStateOf(pagerState.currentPage) }
    var dictionaryVerticalScrollActive by remember { mutableStateOf(false) }
    val surfaceColor = MiuixTheme.colorScheme.surface
    val navigationBackdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedPage = pagerState.currentPage
        val currentTab = AppTab.entries[pagerState.currentPage]
        if (state.currentTab != currentTab) {
            onIntent(AppIntent.SelectTab(currentTab))
        }
    }

    LaunchedEffect(selectedPage) {
        if (AppTab.entries[selectedPage] != AppTab.Dictionary) {
            dictionaryVerticalScrollActive = false
        }
    }

    val onTabSelected: (AppTab) -> Unit = { tab ->
        selectedPage = tab.ordinal
        if (state.currentTab != tab) {
            onIntent(AppIntent.SelectTab(tab))
        }
        coroutineScope.animateToTab(pagerState, tab)
    }

    if (isWideScreen) {
        Row(modifier = Modifier.fillMaxSize()) {
            MoriNavigationRail(
                selectedTab = AppTab.entries[selectedPage],
                backdrop = navigationBackdrop,
                blurEnabled = state.settings.blurEnabled,
                onTabSelected = onTabSelected,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(navigationBackdrop),
            ) {
                MainTabsPager(
                    state = state,
                    message = message,
                    fixedPadding = PaddingValues(),
                    pagerState = pagerState,
                    routes = routes,
                    dictionaryVerticalScrollActive = dictionaryVerticalScrollActive,
                    onIntent = onIntent,
                    onOpenAppearanceSettings = onOpenAppearanceSettings,
                    onOpenDictionarySettings = onOpenDictionarySettings,
                    onOpenAudioSettings = onOpenAudioSettings,
                    onOpenAnkiSettings = onOpenAnkiSettings,
                    onWebViewVerticalScrollActiveChange = { dictionaryVerticalScrollActive = it },
                )
            }
        }
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                MoriNavigationBar(
                    selectedTab = AppTab.entries[selectedPage],
                    backdrop = navigationBackdrop,
                    blurEnabled = state.settings.blurEnabled,
                    onTabSelected = onTabSelected,
                )
            },
        ) { fixedPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(navigationBackdrop),
            ) {
                MainTabsPager(
                    state = state,
                    message = message,
                    fixedPadding = fixedPadding,
                    pagerState = pagerState,
                    routes = routes,
                    dictionaryVerticalScrollActive = dictionaryVerticalScrollActive,
                    onIntent = onIntent,
                    onOpenAppearanceSettings = onOpenAppearanceSettings,
                    onOpenDictionarySettings = onOpenDictionarySettings,
                    onOpenAudioSettings = onOpenAudioSettings,
                    onOpenAnkiSettings = onOpenAnkiSettings,
                    onWebViewVerticalScrollActiveChange = { dictionaryVerticalScrollActive = it },
                )
            }
        }
    }
}

@Composable
private fun MainTabsPager(
    state: AppState,
    message: String?,
    fixedPadding: PaddingValues,
    pagerState: PagerState,
    routes: List<AppRoute>,
    dictionaryVerticalScrollActive: Boolean,
    onIntent: (AppIntent) -> Unit,
    onOpenAppearanceSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onOpenAnkiSettings: () -> Unit,
    onWebViewVerticalScrollActiveChange: (Boolean) -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 0,
        userScrollEnabled = !dictionaryVerticalScrollActive && state.dictionary.popupStack.isEmpty(),
        verticalAlignment = androidx.compose.ui.Alignment.Top,
    ) { page ->
        NavDisplay(
            backStack = listOf(routes[page]),
            modifier = Modifier.fillMaxSize(),
            onBack = {},
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider = entryProvider<NavKey> {
                entry<AppRoute.Home> {
                    HomePage(
                        state = state,
                        message = message,
                        fixedPadding = fixedPadding,
                        onIntent = onIntent,
                    )
                }
                entry<AppRoute.Dictionary> {
                    DictionaryPage(
                        state = state,
                        message = message,
                        fixedPadding = fixedPadding,
                        onIntent = onIntent,
                        onWebViewVerticalScrollActiveChange = onWebViewVerticalScrollActiveChange,
                    )
                }
                entry<AppRoute.Settings> {
                    SettingsPage(
                        state = state,
                        message = message,
                        fixedPadding = fixedPadding,
                        onIntent = onIntent,
                        onOpenAppearanceSettings = onOpenAppearanceSettings,
                        onOpenDictionarySettings = onOpenDictionarySettings,
                        onOpenAudioSettings = onOpenAudioSettings,
                        onOpenAnkiSettings = onOpenAnkiSettings,
                    )
                }
            },
        )
    }
}

private fun CoroutineScope.animateToTab(
    pagerState: PagerState,
    tab: AppTab,
) {
    if (pagerState.currentPage == tab.ordinal) return

    launch {
        pagerState.animateScrollToPage(tab.ordinal)
    }
}
