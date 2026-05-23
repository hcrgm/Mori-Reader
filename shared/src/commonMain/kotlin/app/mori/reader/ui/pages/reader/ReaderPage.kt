package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.ReaderThemeMode
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.data.settings.effectiveReaderSettings
import app.mori.reader.features.anki.presentation.AnkiIntent
import app.mori.reader.features.anki.presentation.AnkiState
import app.mori.reader.features.reader.presentation.ReaderIntent
import app.mori.reader.features.reader.presentation.ReaderState
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.reader_loading_epub
import app.mori.reader.shared.generated.resources.reader_no_chapter
import app.mori.reader.ui.layout.shouldShowWideLayout
import app.mori.reader.ui.text.asString
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.blur.layerBackdrop

@Composable
fun ReaderPage(
    reader: ReaderState,
    settings: AppSettings,
    ankiState: AnkiState,
    bookId: String,
    onReaderIntent: (ReaderIntent) -> Unit,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onAnkiIntent: (AnkiIntent) -> Unit,
    onBack: () -> Unit,
) {
    val book = reader.book
    val chapter = reader.currentChapter
    var sasayakiOpen by rememberSaveable(bookId) { mutableStateOf(false) }
    var readingSchemePanelVisible by rememberSaveable(bookId) { mutableStateOf(false) }
    var openReadingSchemeAdvanced by rememberSaveable(bookId) { mutableStateOf(false) }
    var bookmarkPanelVisible by rememberSaveable(bookId) { mutableStateOf(false) }
    var bookmarkTextCaptureRequestKey by rememberSaveable(bookId) { mutableStateOf(0) }
    var transientReaderChromeVisible by rememberSaveable(bookId) { mutableStateOf(false) }
    var chromeInteractionVersion by rememberSaveable(bookId) { mutableStateOf(0) }
    var exitingReader by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val chaptersOpen = drawerState.currentValue == DrawerValue.Open || drawerState.targetValue == DrawerValue.Open
    val wideLayout = shouldShowWideLayout()
    val hideTransientReaderChrome = {
        transientReaderChromeVisible = false
    }
    val dismissExpandedPanels = {
        readingSchemePanelVisible = false
        openReadingSchemeAdvanced = false
        bookmarkPanelVisible = false
    }
    val showTransientReaderChrome = {
        transientReaderChromeVisible = true
        chromeInteractionVersion += 1
    }
    val refreshTransientReaderChromeTimeout = {
        if (transientReaderChromeVisible) {
            chromeInteractionVersion += 1
        }
    }
    val handleBack: () -> Unit = {
        when {
            chaptersOpen -> {
                coroutineScope.launch {
                    drawerState.close()
                }
            }

            sasayakiOpen -> {
                sasayakiOpen = false
            }

            readingSchemePanelVisible -> {
                readingSchemePanelVisible = false
                openReadingSchemeAdvanced = false
            }

            bookmarkPanelVisible -> {
                bookmarkPanelVisible = false
            }

            reader.lookupStack.any { it.visible } -> {
                onReaderIntent(ReaderIntent.DismissLookup())
            }

            wideLayout && !transientReaderChromeVisible -> {
                showTransientReaderChrome()
            }

            !wideLayout && !transientReaderChromeVisible -> {
                showTransientReaderChrome()
            }

            else -> {
                exitingReader = true
                onBack()
            }
        }
    }
    ReaderBackHandler(
        enabled = !exitingReader,
        onBack = handleBack,
    )
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isDark =
        when (settings.appearance.readerThemeMode) {
            ReaderThemeMode.FollowApp -> {
                when (settings.appearance.themeMode) {
                    ThemeMode.System -> isSystemInDarkTheme()
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                }
            }

            ReaderThemeMode.Light -> {
                false
            }

            ReaderThemeMode.Dark -> {
                true
            }
        }
    val materialEInkMode = materialReaderEInkMode(settings)
    val effectiveBlurEnabled = settings.appearance.blurEnabled && !materialEInkMode
    val readerBackground = readerBackgroundColor(isDark = isDark, materialEInkMode = materialEInkMode)
    val effectiveReaderSettings = settings.effectiveReaderSettings(book?.info?.readerSchemeId)
    val actionBarPinned = effectiveReaderSettings.actionBarPinned
    val chromeVisible = actionBarPinned || transientReaderChromeVisible
    val sideChromeVisible = chromeVisible && !chaptersOpen
    val readerBottomAestheticPadding = 8.dp
    val visibleNavigationBarPadding =
        if (effectiveReaderSettings.fullscreen) {
            0.dp
        } else {
            navigationBarPadding
        }
    val compactReaderTopPadding = if (effectiveReaderSettings.fullscreen) 0.dp else statusBarPadding
    val readerContentTopPadding = compactReaderTopPadding + 50.dp
    val readerContentBottomPadding =
        if (actionBarPinned) {
            48.dp + visibleNavigationBarPadding + readerBottomAestheticPadding
        } else {
            visibleNavigationBarPadding + readerBottomAestheticPadding
        }
    val readerPopupBottomPadding = visibleNavigationBarPadding + 80.dp
    ReaderFullscreenEffect(
        enabled = effectiveReaderSettings.fullscreen && !exitingReader,
    )
    val popupBackdrop =
        rememberReaderPopupBackdrop(
            blurEnabled = effectiveBlurEnabled,
            readerBackground = readerBackground,
        )
    val popupBlurActive = popupBackdrop != null && reader.lookupStack.any { it.visible }
    val bookmarkRootChapterTitleFor: (Int) -> String =
        remember(book) {
            val chaptersByIndex = book?.chapters.orEmpty().associateBy { it.index }
            val rootTitles = mutableMapOf<Int, String>()
            var currentRootTitle: String? = null
            book?.tableOfContents.orEmpty().forEach { row ->
                if (row.indentLevel == 0) {
                    currentRootTitle = row.label
                }
                currentRootTitle
                    ?.takeIf { it.isNotBlank() }
                    ?.let { rootTitles.putIfAbsent(row.chapterIndex, it) }
            }
            return@remember { chapterIndex: Int ->
                rootTitles[chapterIndex]
                    ?: chaptersByIndex[chapterIndex]?.title
                    ?: ""
            }
        }
    val readerChromePinned =
        chaptersOpen ||
            sasayakiOpen ||
            readingSchemePanelVisible ||
            bookmarkPanelVisible ||
            actionBarPinned ||
            reader.lookupStack.any { it.visible }
    val accessoryPanelVisible = readingSchemePanelVisible || bookmarkPanelVisible
    val readerContentStartPadding =
        if (wideLayout) {
            ReaderSideRailWidth
        } else {
            0.dp
        }

    LaunchedEffect(bookId) {
        onReaderIntent(ReaderIntent.LoadBook(bookId))
    }

    LaunchedEffect(actionBarPinned, wideLayout) {
        if (actionBarPinned) {
            showTransientReaderChrome()
        }
    }

    LaunchedEffect(transientReaderChromeVisible, chromeInteractionVersion, readerChromePinned) {
        if (!transientReaderChromeVisible || readerChromePinned) return@LaunchedEffect
        kotlinx.coroutines.delay(5_000)
        if (!readerChromePinned) {
            transientReaderChromeVisible = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = chaptersOpen,
        drawerContent = {
            ReaderChapterSheet(
                isOpen = chaptersOpen,
                isDark = isDark,
                materialEInkMode = materialEInkMode,
                monetEnabled = settings.appearance.monetEnabled,
                monetKeyColor = settings.appearance.monetKeyColor,
                bookTitle = book?.info?.title.orEmpty(),
                bookAuthor = book?.info?.author,
                currentCharacter = reader.currentCharacter,
                totalCharacters = book?.totalCharacterCount ?: 0,
                rows =
                    remember(book) {
                        val chapterStarts = book?.chapters.orEmpty().associate { it.index to it.characterStart }
                        book?.tableOfContents.orEmpty().map { row ->
                            if (row.indentLevel == 0) {
                                row.copy(characterCount = chapterStarts[row.chapterIndex])
                            } else {
                                row
                            }
                        }
                    },
                bookmarks = reader.savedBookmarks,
                currentChapterIndex = reader.chapterIndex,
                chapterTitleForBookmark = bookmarkRootChapterTitleFor,
                onDismiss = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                onSelect = { row ->
                    onReaderIntent(ReaderIntent.OpenChapter(row.chapterIndex, row.fragment))
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                onSelectBookmark = { bookmark ->
                    onReaderIntent(ReaderIntent.JumpToCharacter(bookmark.characterCount))
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                onDeleteBookmark = { bookmarkId ->
                    onReaderIntent(ReaderIntent.DeleteBookmark(bookmarkId))
                },
                onJumpToCharacter = { characterCount ->
                    onReaderIntent(ReaderIntent.JumpToCharacter(characterCount))
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
            )
        },
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(readerBackground),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .then(if (popupBackdrop != null) Modifier.layerBackdrop(popupBackdrop) else Modifier),
            ) {
                when {
                    reader.isLoading -> {
                        ReaderStatus(
                            text = stringResource(Res.string.reader_loading_epub),
                            isDark = isDark,
                            materialEInkMode = materialEInkMode,
                            monetEnabled = settings.appearance.monetEnabled,
                            monetKeyColor = settings.appearance.monetKeyColor,
                        )
                    }

                    reader.errorMessage != null -> {
                        ReaderStatus(
                            text = reader.errorMessage.asString(),
                            isDark = isDark,
                            materialEInkMode = materialEInkMode,
                            monetEnabled = settings.appearance.monetEnabled,
                            monetKeyColor = settings.appearance.monetKeyColor,
                        )
                    }

                    chapter == null -> {
                        ReaderStatus(
                            text = stringResource(Res.string.reader_no_chapter),
                            isDark = isDark,
                            materialEInkMode = materialEInkMode,
                            monetEnabled = settings.appearance.monetEnabled,
                            monetKeyColor = settings.appearance.monetKeyColor,
                        )
                    }

                    else -> {
                        val currentChapterSasayakiCues =
                            remember(reader.sasayakiMatches, reader.chapterIndex) {
                                reader.currentChapterSasayakiCues
                            }
                        ReaderWebView(
                            state =
                                ReaderWebViewState(
                                    chapter = chapter,
                                    progress = reader.chapterProgress,
                                    navigationVersion = reader.navigationVersion,
                                    fragment = reader.fragment,
                                    capturePageTextRequestKey = bookmarkTextCaptureRequestKey,
                                    selectionHighlightLength = reader.lookupStack.firstOrNull()?.highlightLength,
                                    sasayakiCues = currentChapterSasayakiCues,
                                    highlightedSasayakiCueId = reader.sasayakiPlayer.currentCueId,
                                ),
                            config =
                                ReaderWebViewSettings(
                                    verticalWriting = reader.verticalWriting,
                                    isDark = isDark,
                                    eInkMode = materialEInkMode,
                                    scanLength = settings.dictionary.scanLength,
                                    fontFamily = effectiveReaderSettings.fontFamily,
                                    fontSize = effectiveReaderSettings.fontSize,
                                    lineHeight = effectiveReaderSettings.lineHeight,
                                    horizontalPadding = effectiveReaderSettings.horizontalPadding,
                                    verticalPadding = effectiveReaderSettings.verticalPadding,
                                    avoidPageBreak = effectiveReaderSettings.avoidPageBreak,
                                    justifyText = effectiveReaderSettings.justifyText,
                                    characterSpacing = effectiveReaderSettings.characterSpacing,
                                    continuousMode = effectiveReaderSettings.continuousMode,
                                    hideFurigana = effectiveReaderSettings.hideFurigana,
                                    viewportLayoutKey =
                                        listOf(
                                            effectiveReaderSettings.fullscreen,
                                            actionBarPinned,
                                            visibleNavigationBarPadding,
                                            readerContentTopPadding,
                                            readerContentBottomPadding,
                                            readerContentStartPadding,
                                        ).hashCode(),
                                    sasayakiAutoScroll = settings.sasayaki.autoScroll,
                                    sasayakiHighlightEnabled = settings.sasayaki.highlightEnabled,
                                    sasayakiHighlightColor = settings.sasayaki.highlightColor,
                                    stabilizeForBackdrop = popupBlurActive,
                                ),
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = readerContentStartPadding,
                                        top = readerContentTopPadding,
                                        bottom = readerContentBottomPadding,
                                ),
                            callbacks =
                                ReaderWebViewCallbacks(
                                    onUserInteraction = {
                                        if (transientReaderChromeVisible && !readerChromePinned) {
                                            hideTransientReaderChrome()
                                        }
                                    },
                                    onProgressChanged = {
                                        onReaderIntent(ReaderIntent.UpdateProgress(it))
                                    },
                                    onProgressSaved = { onReaderIntent(ReaderIntent.SaveProgress(it)) },
                                    onPageTextCaptured = {
                                        onReaderIntent(ReaderIntent.ToggleCurrentBookmark(it))
                                    },
                                    onTextSelected = { text, sentence, rect ->
                                        if (!readerChromePinned) {
                                            hideTransientReaderChrome()
                                        }
                                        onReaderIntent(ReaderIntent.TextSelected(text, sentence, rect))
                                    },
                                    onLinkActivated = {
                                        if (!readerChromePinned) {
                                            hideTransientReaderChrome()
                                        }
                                        onReaderIntent(ReaderIntent.JumpToLink(it))
                                    },
                                    onTapOutside = {
                                        if (reader.lookupStack.any { it.visible }) {
                                            onReaderIntent(ReaderIntent.DismissLookup())
                                        } else if (accessoryPanelVisible) {
                                            dismissExpandedPanels()
                                            if (!actionBarPinned) {
                                                hideTransientReaderChrome()
                                            }
                                        } else if (transientReaderChromeVisible && !actionBarPinned) {
                                            hideTransientReaderChrome()
                                        } else {
                                            showTransientReaderChrome()
                                        }
                                    },
                                    onNextChapter = {
                                        if (!readerChromePinned) {
                                            hideTransientReaderChrome()
                                        }
                                        onReaderIntent(ReaderIntent.OpenNextChapter)
                                    },
                                    onPreviousChapter = {
                                        if (!readerChromePinned) {
                                            hideTransientReaderChrome()
                                        }
                                        onReaderIntent(ReaderIntent.OpenPreviousChapter)
                                    },
                                ),
                        )
                    }
                }
                if (accessoryPanelVisible) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(
                                    top = readerContentTopPadding,
                                    bottom = readerContentBottomPadding,
                                ).clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    dismissExpandedPanels()
                                    if (!actionBarPinned) {
                                        hideTransientReaderChrome()
                                    }
                                },
                    )
                }

                if (effectiveReaderSettings.showReadingInfo) {
                    ReaderTopChrome(
                        title = book?.info?.title.orEmpty(),
                        chapter = chapter?.title.orEmpty(),
                        progress =
                            book?.let {
                                "${reader.currentCharacter} / ${it.totalCharacterCount} ${reader.progressPercent.formatPercent()}%"
                            },
                        isDark = isDark,
                        materialEInkMode = materialEInkMode,
                        monetEnabled = settings.appearance.monetEnabled,
                        monetKeyColor = settings.appearance.monetKeyColor,
                        compactTopPadding = compactReaderTopPadding,
                        compactStartPadding = if (wideLayout) readerContentStartPadding + 16.dp else 28.dp,
                        compactEndPadding = if (wideLayout) 88.dp else 28.dp,
                        onClick = if (!wideLayout && !actionBarPinned) showTransientReaderChrome else null,
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter),
                    )
                }

                if (wideLayout) {
                    ReaderSideChrome(
                        visible = sideChromeVisible,
                        settings = settings,
                        bookId = bookId,
                        bookSchemeId = book?.info?.readerSchemeId,
                        bookLastSchemeId = book?.info?.lastReaderSchemeId,
                        isDark = isDark,
                        materialEInkMode = materialEInkMode,
                        monetEnabled = settings.appearance.monetEnabled,
                        monetKeyColor = settings.appearance.monetKeyColor,
                        topPadding = statusBarPadding,
                        bottomPadding = visibleNavigationBarPadding,
                        onShow = showTransientReaderChrome,
                        onHide = {
                            if (actionBarPinned) return@ReaderSideChrome
                            transientReaderChromeVisible = false
                            readingSchemePanelVisible = false
                            bookmarkPanelVisible = false
                            openReadingSchemeAdvanced = false
                        },
                        onMenu = {
                            bookmarkPanelVisible = false
                            sasayakiOpen = false
                            readingSchemePanelVisible = false
                            openReadingSchemeAdvanced = false
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                        onSasayaki = {
                            bookmarkPanelVisible = false
                            readingSchemePanelVisible = false
                            openReadingSchemeAdvanced = false
                            sasayakiOpen = true
                        },
                        readingSchemePanelVisible = readingSchemePanelVisible,
                        onReadingScheme = {
                            sasayakiOpen = false
                            bookmarkPanelVisible = false
                            openReadingSchemeAdvanced = false
                            readingSchemePanelVisible = !readingSchemePanelVisible
                        },
                        bookmarkPanelVisible = bookmarkPanelVisible,
                        bookmarks = reader.savedBookmarks,
                        currentPositionBookmarked = reader.isCurrentPositionBookmarked,
                        chapterTitleForBookmark = bookmarkRootChapterTitleFor,
                        onBookmark = {
                            sasayakiOpen = false
                            readingSchemePanelVisible = false
                            openReadingSchemeAdvanced = false
                            bookmarkPanelVisible = !bookmarkPanelVisible
                        },
                        onToggleCurrentBookmark = {
                            if (reader.isCurrentPositionBookmarked) {
                                onReaderIntent(ReaderIntent.ToggleCurrentBookmark())
                            } else {
                                bookmarkTextCaptureRequestKey += 1
                            }
                        },
                        onSelectBookmark = { bookmark ->
                            bookmarkPanelVisible = false
                            onReaderIntent(ReaderIntent.JumpToCharacter(bookmark.characterCount))
                        },
                        onDeleteBookmark = { bookmarkId ->
                            onReaderIntent(ReaderIntent.DeleteBookmark(bookmarkId))
                        },
                        onSwitchToGlobalScheme = {
                            onSettingsIntent(SettingsIntent.SetBookReaderScheme(bookId, null))
                            onReaderIntent(ReaderIntent.SetBookReaderScheme(null))
                        },
                        onSwitchToReaderScheme = { schemeId ->
                            onSettingsIntent(SettingsIntent.SetBookReaderScheme(bookId, schemeId))
                            onReaderIntent(ReaderIntent.SetBookReaderScheme(schemeId))
                        },
                        onCreateReaderScheme = { scheme ->
                            onSettingsIntent(SettingsIntent.CreateReaderPersonalizedScheme(scheme))
                        },
                        onRenameReaderScheme = { schemeId, name ->
                            onSettingsIntent(SettingsIntent.RenameReaderPersonalizedScheme(schemeId, name))
                        },
                        onDeleteReaderScheme = { schemeId ->
                            onSettingsIntent(SettingsIntent.DeleteReaderPersonalizedScheme(schemeId))
                            if (book?.info?.readerSchemeId == schemeId) {
                                onReaderIntent(ReaderIntent.SetBookReaderScheme(null))
                            }
                        },
                        onUpdateGlobalReaderSettings = {
                            onSettingsIntent(SettingsIntent.UpdateGlobalReaderSettings(it))
                        },
                        onUpdateReaderSchemeSettings = { schemeId, schemeSettings ->
                            onSettingsIntent(SettingsIntent.UpdateReaderPersonalizedSchemeSettings(schemeId, schemeSettings))
                        },
                        openAdvanced = openReadingSchemeAdvanced,
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                } else {
                    ReaderBottomChrome(
                        visible = chromeVisible,
                        settings = settings,
                        bookId = bookId,
                        bookSchemeId = book?.info?.readerSchemeId,
                        bookLastSchemeId = book?.info?.lastReaderSchemeId,
                        isDark = isDark,
                        materialEInkMode = materialEInkMode,
                        monetEnabled = settings.appearance.monetEnabled,
                        monetKeyColor = settings.appearance.monetKeyColor,
                        bottomPadding = visibleNavigationBarPadding,
                        onMenu = {
                            refreshTransientReaderChromeTimeout()
                            bookmarkPanelVisible = false
                            sasayakiOpen = false
                            readingSchemePanelVisible = false
                            openReadingSchemeAdvanced = false
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                        onSasayaki = {
                            refreshTransientReaderChromeTimeout()
                            bookmarkPanelVisible = false
                            readingSchemePanelVisible = false
                            openReadingSchemeAdvanced = false
                            sasayakiOpen = true
                        },
                        readingSchemePanelVisible = readingSchemePanelVisible,
                        onReadingScheme = {
                            refreshTransientReaderChromeTimeout()
                            sasayakiOpen = false
                            bookmarkPanelVisible = false
                            openReadingSchemeAdvanced = false
                            readingSchemePanelVisible = !readingSchemePanelVisible
                        },
                        bookmarkPanelVisible = bookmarkPanelVisible,
                        bookmarks = reader.savedBookmarks,
                        currentPositionBookmarked = reader.isCurrentPositionBookmarked,
                        chapterTitleForBookmark = bookmarkRootChapterTitleFor,
                        onBookmark = {
                            refreshTransientReaderChromeTimeout()
                            sasayakiOpen = false
                            readingSchemePanelVisible = false
                            openReadingSchemeAdvanced = false
                            bookmarkPanelVisible = !bookmarkPanelVisible
                        },
                        onToggleCurrentBookmark = {
                            refreshTransientReaderChromeTimeout()
                            if (reader.isCurrentPositionBookmarked) {
                                onReaderIntent(ReaderIntent.ToggleCurrentBookmark())
                            } else {
                                bookmarkTextCaptureRequestKey += 1
                            }
                        },
                        onSelectBookmark = { bookmark ->
                            bookmarkPanelVisible = false
                            onReaderIntent(ReaderIntent.JumpToCharacter(bookmark.characterCount))
                        },
                        onDeleteBookmark = { bookmarkId ->
                            refreshTransientReaderChromeTimeout()
                            onReaderIntent(ReaderIntent.DeleteBookmark(bookmarkId))
                        },
                        onSwitchToGlobalScheme = {
                            onSettingsIntent(SettingsIntent.SetBookReaderScheme(bookId, null))
                            onReaderIntent(ReaderIntent.SetBookReaderScheme(null))
                        },
                        onSwitchToReaderScheme = { schemeId ->
                            onSettingsIntent(SettingsIntent.SetBookReaderScheme(bookId, schemeId))
                            onReaderIntent(ReaderIntent.SetBookReaderScheme(schemeId))
                        },
                        onCreateReaderScheme = { scheme ->
                            onSettingsIntent(SettingsIntent.CreateReaderPersonalizedScheme(scheme))
                        },
                        onRenameReaderScheme = { schemeId, name ->
                            onSettingsIntent(SettingsIntent.RenameReaderPersonalizedScheme(schemeId, name))
                        },
                        onDeleteReaderScheme = { schemeId ->
                            onSettingsIntent(SettingsIntent.DeleteReaderPersonalizedScheme(schemeId))
                            if (book?.info?.readerSchemeId == schemeId) {
                                onReaderIntent(ReaderIntent.SetBookReaderScheme(null))
                            }
                        },
                        onUpdateGlobalReaderSettings = {
                            onSettingsIntent(SettingsIntent.UpdateGlobalReaderSettings(it))
                        },
                        onUpdateReaderSchemeSettings = { schemeId, schemeSettings ->
                            onSettingsIntent(SettingsIntent.UpdateReaderPersonalizedSchemeSettings(schemeId, schemeSettings))
                        },
                        openAdvanced = openReadingSchemeAdvanced,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            reader.lookupStack.forEachIndexed { index, lookup ->
                ReaderLookupPopup(
                    lookup = lookup,
                    popupIndex = index,
                    reader = reader,
                    settings = settings,
                    ankiDuplicateExpression = ankiState.duplicateExpression,
                    isDark = isDark,
                    materialEInkMode = materialEInkMode,
                    monetEnabled = settings.appearance.monetEnabled,
                    monetKeyColor = settings.appearance.monetKeyColor,
                    isVertical = index == 0 && reader.verticalWriting,
                    viewportWidth = maxWidth,
                    viewportHeight = maxHeight,
                    readerTopPadding = readerContentTopPadding,
                    readerStartPadding = readerContentStartPadding,
                    readerBottomPadding = readerPopupBottomPadding,
                    blurEnabled = effectiveBlurEnabled,
                    backdrop = popupBackdrop,
                    onReaderIntent = onReaderIntent,
                    onAnkiIntent = onAnkiIntent,
                    onDismiss = { onReaderIntent(ReaderIntent.DismissLookup(index)) },
                    onSwipeDismiss = { onReaderIntent(ReaderIntent.DismissLookup(index)) },
                )
            }
        }
    }

    ReaderSasayakiSheet(
        show = sasayakiOpen,
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = settings.appearance.monetEnabled,
        monetKeyColor = settings.appearance.monetKeyColor,
        player = reader.sasayakiPlayer,
        currentCueText =
            reader.sasayakiPlayer.currentCueId?.let { cueId ->
                reader.sasayakiMatches.firstOrNull { it.id == cueId }?.text
            },
        enabled = reader.sasayakiMatches.isNotEmpty(),
        autoScroll = settings.sasayaki.autoScroll,
        autoPauseOnLookup = settings.sasayaki.autoPauseOnLookup,
        highlightEnabled = settings.sasayaki.highlightEnabled,
        highlightColor = settings.sasayaki.highlightColor,
        onDismiss = { sasayakiOpen = false },
        onPlayPause = { onReaderIntent(ReaderIntent.TogglePlayback) },
        onPrevious = { onReaderIntent(ReaderIntent.PreviousCue) },
        onNext = { onReaderIntent(ReaderIntent.NextCue) },
        onSeek = { onReaderIntent(ReaderIntent.SeekTo(it)) },
        onDelay = { onReaderIntent(ReaderIntent.SetDelay(it)) },
        onRate = { onReaderIntent(ReaderIntent.SetRate(it)) },
        onAutoScroll = { onSettingsIntent(SettingsIntent.SetSasayakiAutoScroll(it)) },
        onAutoPauseOnLookup = { onSettingsIntent(SettingsIntent.SetSasayakiAutoPauseOnLookup(it)) },
        onHighlightEnabled = { onSettingsIntent(SettingsIntent.SetSasayakiHighlightEnabled(it)) },
        onHighlightColor = { onSettingsIntent(SettingsIntent.SetSasayakiHighlightColor(it)) },
    )
}
