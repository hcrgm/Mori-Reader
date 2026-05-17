package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.ReaderThemeMode
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.features.anki.presentation.AnkiIntent
import app.mori.reader.features.anki.presentation.AnkiState
import app.mori.reader.features.reader.presentation.ReaderIntent
import app.mori.reader.features.reader.presentation.ReaderState
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.reader_loading_epub
import app.mori.reader.shared.generated.resources.reader_no_chapter
import app.mori.reader.ui.text.asString
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
    val fullscreen = settings.appearance.readerFullscreen
    var chaptersOpen by remember { mutableStateOf(false) }
    var appearanceOpen by remember { mutableStateOf(false) }
    var sasayakiOpen by rememberSaveable(bookId) { mutableStateOf(false) }
    var exitingReader by remember { mutableStateOf(false) }
    val handleBack = {
        exitingReader = true
        onBack()
    }
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
    ReaderFullscreenEffect(
        enabled = fullscreen && !exitingReader,
        onBack = handleBack,
    )
    val materialEInkMode = materialReaderEInkMode(settings)
    val effectiveBlurEnabled = settings.appearance.blurEnabled && !materialEInkMode
    val readerBackground = readerBackgroundColor(isDark = isDark, materialEInkMode = materialEInkMode)
    val readerContentTopPadding = if (fullscreen) 52.dp else statusBarPadding + 52.dp
    val readerContentBottomPadding = if (fullscreen) 58.dp else navigationBarPadding + 58.dp
    val readerPopupBottomPadding = readerContentBottomPadding + 24.dp
    val popupBackdrop =
        rememberReaderPopupBackdrop(
            blurEnabled = effectiveBlurEnabled,
            readerBackground = readerBackground,
        )
    val popupBlurActive = popupBackdrop != null && reader.lookupStack.any { it.visible }

    LaunchedEffect(bookId) {
        onReaderIntent(ReaderIntent.LoadBook(bookId))
    }

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
                    ReaderWebView(
                        state =
                            ReaderWebViewState(
                                chapter = chapter,
                                progress = reader.chapterProgress,
                                navigationVersion = reader.navigationVersion,
                                fragment = reader.fragment,
                                selectionHighlightLength = reader.lookupStack.firstOrNull()?.highlightLength,
                                sasayakiCues = reader.currentChapterSasayakiCues,
                                highlightedSasayakiCueId = reader.sasayakiPlayer.currentCueId,
                            ),
                        config =
                            ReaderWebViewSettings(
                                verticalWriting = reader.verticalWriting,
                                isDark = isDark,
                                eInkMode = materialEInkMode,
                                scanLength = settings.dictionary.scanLength,
                                fontSize = settings.reader.fontSize,
                                lineHeight = settings.reader.lineHeight,
                                horizontalPadding = settings.reader.horizontalPadding,
                                verticalPadding = settings.reader.verticalPadding,
                                avoidPageBreak = settings.reader.avoidPageBreak,
                                justifyText = settings.reader.justifyText,
                                characterSpacing = settings.reader.characterSpacing,
                                continuousMode = settings.reader.continuousMode,
                                hideFurigana = settings.reader.hideFurigana,
                                sasayakiAutoScroll = settings.sasayaki.autoScroll,
                                sasayakiHighlightEnabled = settings.sasayaki.highlightEnabled,
                                sasayakiHighlightColor = settings.sasayaki.highlightColor,
                                stabilizeForBackdrop = popupBlurActive,
                            ),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(
                                    top = readerContentTopPadding,
                                    bottom = readerContentBottomPadding,
                                ),
                        callbacks =
                            ReaderWebViewCallbacks(
                                onProgressChanged = { onReaderIntent(ReaderIntent.UpdateProgress(it)) },
                                onProgressSaved = { onReaderIntent(ReaderIntent.SaveProgress(it)) },
                                onTextSelected = { text, sentence, rect ->
                                    onReaderIntent(ReaderIntent.TextSelected(text, sentence, rect))
                                },
                                onLinkActivated = { onReaderIntent(ReaderIntent.JumpToLink(it)) },
                                onTapOutside = { onReaderIntent(ReaderIntent.DismissLookup()) },
                                onNextChapter = { onReaderIntent(ReaderIntent.OpenNextChapter) },
                                onPreviousChapter = { onReaderIntent(ReaderIntent.OpenPreviousChapter) },
                            ),
                    )
                }
            }

            ReaderHeaderInfo(
                title = chapter?.title ?: book?.info?.title.orEmpty(),
                progress =
                    book?.let {
                        "${reader.currentCharacter} / ${it.totalCharacterCount} ${reader.progressPercent.formatPercent()}%"
                    },
                isDark = isDark,
                materialEInkMode = materialEInkMode,
                monetEnabled = settings.appearance.monetEnabled,
                monetKeyColor = settings.appearance.monetKeyColor,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            top = if (fullscreen) 26.dp else statusBarPadding + 26.dp,
                            start = 28.dp,
                            end = 28.dp,
                        ),
            )

            ReaderBottomChrome(
                isDark = isDark,
                materialEInkMode = materialEInkMode,
                monetEnabled = settings.appearance.monetEnabled,
                monetKeyColor = settings.appearance.monetKeyColor,
                bottomPadding = if (fullscreen) 0.dp else navigationBarPadding,
                onBack = handleBack,
                onMenu = { chaptersOpen = true },
                onAppearance = { appearanceOpen = true },
                onSasayaki = { sasayakiOpen = true },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
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

    ReaderChapterSheet(
        show = chaptersOpen,
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = settings.appearance.monetEnabled,
        monetKeyColor = settings.appearance.monetKeyColor,
        title = book?.info?.title.orEmpty(),
        currentCharacter = reader.currentCharacter,
        totalCharacters = book?.totalCharacterCount ?: 0,
        rows = book?.tableOfContents.orEmpty(),
        currentChapterIndex = reader.chapterIndex,
        onDismiss = { chaptersOpen = false },
        onSelect = { row ->
            onReaderIntent(ReaderIntent.OpenChapter(row.chapterIndex, row.fragment))
            chaptersOpen = false
        },
        onJumpToCharacter = { characterCount ->
            onReaderIntent(ReaderIntent.JumpToCharacter(characterCount))
            chaptersOpen = false
        },
    )

    ReaderAppearanceSheet(
        show = appearanceOpen,
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = settings.appearance.monetEnabled,
        monetKeyColor = settings.appearance.monetKeyColor,
        readerThemeMode = settings.appearance.readerThemeMode,
        verticalWriting = reader.verticalWriting,
        continuousMode = settings.reader.continuousMode,
        hideFurigana = settings.reader.hideFurigana,
        fullscreen = settings.appearance.readerFullscreen,
        fontSize = settings.reader.fontSize,
        lineHeight = settings.reader.lineHeight,
        horizontalPadding = settings.reader.horizontalPadding,
        verticalPadding = settings.reader.verticalPadding,
        avoidPageBreak = settings.reader.avoidPageBreak,
        justifyText = settings.reader.justifyText,
        characterSpacing = settings.reader.characterSpacing,
        popupWidth = settings.popup.width,
        popupHeight = settings.popup.height,
        popupFullWidth = settings.popup.fullWidth,
        popupSwipeToDismiss = settings.popup.swipeToDismiss,
        popupSwipeThreshold = settings.popup.swipeThreshold,
        onDismiss = { appearanceOpen = false },
        onReaderThemeModeSelected = { onSettingsIntent(SettingsIntent.SetReaderThemeMode(it)) },
        onToggleWritingMode = {
            onSettingsIntent(SettingsIntent.SetReaderVerticalWriting(!settings.reader.verticalWriting))
        },
        onToggleContinuousMode = {
            onSettingsIntent(SettingsIntent.SetReaderContinuousMode(!settings.reader.continuousMode))
        },
        onToggleHideFurigana = {
            onSettingsIntent(SettingsIntent.SetReaderHideFurigana(!settings.reader.hideFurigana))
        },
        onFullscreenChanged = { onSettingsIntent(SettingsIntent.SetReaderFullscreen(it)) },
        onFontSizeChanged = { onSettingsIntent(SettingsIntent.SetReaderFontSize(it)) },
        onLineHeightChanged = { onSettingsIntent(SettingsIntent.SetReaderLineHeight(it)) },
        onHorizontalPaddingChanged = { onSettingsIntent(SettingsIntent.SetReaderHorizontalPadding(it)) },
        onVerticalPaddingChanged = { onSettingsIntent(SettingsIntent.SetReaderVerticalPadding(it)) },
        onAvoidPageBreakChanged = { onSettingsIntent(SettingsIntent.SetReaderAvoidPageBreak(it)) },
        onJustifyTextChanged = { onSettingsIntent(SettingsIntent.SetReaderJustifyText(it)) },
        onCharacterSpacingChanged = { onSettingsIntent(SettingsIntent.SetReaderCharacterSpacing(it)) },
        onPopupWidthChanged = { onSettingsIntent(SettingsIntent.SetPopupWidth(it)) },
        onPopupHeightChanged = { onSettingsIntent(SettingsIntent.SetPopupHeight(it)) },
        onTogglePopupFullWidth = {
            onSettingsIntent(SettingsIntent.SetPopupFullWidth(!settings.popup.fullWidth))
        },
        onTogglePopupSwipeToDismiss = {
            onSettingsIntent(SettingsIntent.SetPopupSwipeToDismiss(!settings.popup.swipeToDismiss))
        },
        onPopupSwipeThresholdChanged = { onSettingsIntent(SettingsIntent.SetPopupSwipeThreshold(it)) },
    )

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
