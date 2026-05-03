package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.mori.reader.data.book.ReaderTocItem
import app.mori.reader.data.settings.ReaderThemeMode
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
import app.mori.reader.ui.ReaderLookupState
import app.mori.reader.ui.components.settings.ReaderAppearanceSettingsCard
import app.mori.reader.ui.pages.dictionary.DictionaryWebView
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import app.mori.reader.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReaderPage(
    state: AppState,
    bookId: String,
    onIntent: (AppIntent) -> Unit,
    onBack: () -> Unit,
) {
    val reader = state.reader
    val book = reader.book
    val chapter = reader.currentChapter
    val fullscreen = state.settings.readerFullscreen
    var chaptersOpen by remember { mutableStateOf(false) }
    var appearanceOpen by remember { mutableStateOf(false) }
    var exitingReader by remember { mutableStateOf(false) }
    val handleBack = {
        exitingReader = true
        onBack()
    }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isDark = when (state.settings.readerThemeMode) {
        ReaderThemeMode.FollowApp -> when (state.settings.themeMode) {
            ThemeMode.System -> isSystemInDarkTheme()
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }

        ReaderThemeMode.Light -> false
        ReaderThemeMode.Dark -> true
    }
    ReaderFullscreenEffect(
        enabled = fullscreen && !exitingReader,
        onBack = handleBack,
    )
    val readerBackground = if (isDark) Color(0xFF101010) else Color(0xFFFBFAF7)
    val readerContentTopPadding = if (fullscreen) 52.dp else statusBarPadding + 52.dp
    val readerContentBottomPadding = if (fullscreen) 58.dp else navigationBarPadding + 58.dp
    val readerPopupBottomPadding = readerContentBottomPadding + 24.dp
    val popupBackdrop = rememberReaderPopupBackdrop(
        blurEnabled = state.settings.blurEnabled,
        readerBackground = readerBackground,
    )
    val popupBlurActive = popupBackdrop != null && reader.lookupStack.any { it.visible }

    LaunchedEffect(bookId) {
        onIntent(AppIntent.LoadReaderBook(bookId))
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(readerBackground),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (popupBackdrop != null) Modifier.layerBackdrop(popupBackdrop) else Modifier),
        ) {
            when {
                reader.isLoading -> ReaderStatus(text = stringResource(Res.string.reader_loading_epub))
                reader.errorMessage != null -> ReaderStatus(text = reader.errorMessage)
                chapter == null -> ReaderStatus(text = stringResource(Res.string.reader_no_chapter))
                else -> {
                    ReaderWebView(
                        chapter = chapter,
                        progress = reader.chapterProgress,
                        navigationVersion = reader.navigationVersion,
                        fragment = reader.fragment,
                        verticalWriting = reader.verticalWriting,
                        isDark = isDark,
                        scanLength = state.settings.scanLength,
                        fontSize = state.settings.readerFontSize,
                        lineHeight = state.settings.readerLineHeight,
                        horizontalPadding = state.settings.readerHorizontalPadding,
                        verticalPadding = state.settings.readerVerticalPadding,
                        avoidPageBreak = state.settings.readerAvoidPageBreak,
                        justifyText = state.settings.readerJustifyText,
                        characterSpacing = state.settings.readerCharacterSpacing,
                        continuousMode = state.settings.readerContinuousMode,
                        hideFurigana = state.settings.readerHideFurigana,
                        selectionHighlightLength = reader.lookupStack.firstOrNull()?.highlightLength,
                        stabilizeForBackdrop = popupBlurActive,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = readerContentTopPadding,
                                bottom = readerContentBottomPadding,
                            ),
                        onProgressChanged = { onIntent(AppIntent.UpdateReaderProgress(it)) },
                        onProgressSaved = { onIntent(AppIntent.SaveReaderProgress(it)) },
                        onTextSelected = { text, sentence, rect ->
                            onIntent(AppIntent.ReaderTextSelected(text, sentence, rect))
                        },
                        onLinkActivated = { onIntent(AppIntent.JumpReaderToLink(it)) },
                        onTapOutside = { onIntent(AppIntent.DismissReaderLookup()) },
                        onNextChapter = { onIntent(AppIntent.OpenReaderNextChapter) },
                        onPreviousChapter = { onIntent(AppIntent.OpenReaderPreviousChapter) },
                    )
                }
            }

            ReaderHeaderInfo(
                title = chapter?.title ?: book?.info?.title.orEmpty(),
                progress = book?.let {
                    "${reader.currentCharacter} / ${it.totalCharacterCount} ${reader.progressPercent.formatPercent()}%"
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = if (fullscreen) 26.dp else statusBarPadding + 26.dp,
                        start = 28.dp,
                        end = 28.dp,
                    ),
            )

            ReaderBottomChrome(
                isDark = isDark,
                bottomPadding = if (fullscreen) 0.dp else navigationBarPadding,
                onBack = handleBack,
                onMenu = { chaptersOpen = true },
                onAppearance = { appearanceOpen = true },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        reader.lookupStack.forEachIndexed { index, lookup ->
            ReaderLookupPopup(
                lookup = lookup,
                popupIndex = index,
                state = state,
                isDark = isDark,
                isVertical = index == 0 && reader.verticalWriting,
                viewportWidth = maxWidth,
                viewportHeight = maxHeight,
                readerTopPadding = readerContentTopPadding,
                readerBottomPadding = readerPopupBottomPadding,
                blurEnabled = state.settings.blurEnabled,
                backdrop = popupBackdrop,
                onIntent = onIntent,
                onDismiss = { onIntent(AppIntent.DismissReaderLookup(index)) },
                onSwipeDismiss = { onIntent(AppIntent.DismissReaderLookup(index)) },
            )
        }
    }

    ChapterSheet(
        show = chaptersOpen,
        isDark = isDark,
        title = book?.info?.title.orEmpty(),
        currentCharacter = reader.currentCharacter,
        totalCharacters = book?.totalCharacterCount ?: 0,
        rows = book?.tableOfContents.orEmpty(),
        currentChapterIndex = reader.chapterIndex,
        onDismiss = { chaptersOpen = false },
        onSelect = { row ->
            onIntent(AppIntent.OpenReaderChapter(row.chapterIndex, row.fragment))
            chaptersOpen = false
        },
    )

    AppearanceSheet(
        show = appearanceOpen,
        isDark = isDark,
        readerThemeMode = state.settings.readerThemeMode,
        verticalWriting = reader.verticalWriting,
        continuousMode = state.settings.readerContinuousMode,
        hideFurigana = state.settings.readerHideFurigana,
        fullscreen = state.settings.readerFullscreen,
        fontSize = state.settings.readerFontSize,
        lineHeight = state.settings.readerLineHeight,
        horizontalPadding = state.settings.readerHorizontalPadding,
        verticalPadding = state.settings.readerVerticalPadding,
        avoidPageBreak = state.settings.readerAvoidPageBreak,
        justifyText = state.settings.readerJustifyText,
        characterSpacing = state.settings.readerCharacterSpacing,
        popupWidth = state.settings.popupWidth,
        popupHeight = state.settings.popupHeight,
        popupFullWidth = state.settings.popupFullWidth,
        popupSwipeToDismiss = state.settings.popupSwipeToDismiss,
        popupSwipeThreshold = state.settings.popupSwipeThreshold,
        onDismiss = { appearanceOpen = false },
        onReaderThemeModeSelected = { onIntent(AppIntent.SetReaderThemeMode(it)) },
        onToggleWritingMode = { onIntent(AppIntent.ToggleReaderWritingMode) },
        onToggleContinuousMode = { onIntent(AppIntent.ToggleReaderContinuousMode) },
        onToggleHideFurigana = { onIntent(AppIntent.ToggleReaderHideFurigana) },
        onFullscreenChanged = { onIntent(AppIntent.SetReaderFullscreen(it)) },
        onFontSizeChanged = { onIntent(AppIntent.SetReaderFontSize(it)) },
        onLineHeightChanged = { onIntent(AppIntent.SetReaderLineHeight(it)) },
        onHorizontalPaddingChanged = { onIntent(AppIntent.SetReaderHorizontalPadding(it)) },
        onVerticalPaddingChanged = { onIntent(AppIntent.SetReaderVerticalPadding(it)) },
        onAvoidPageBreakChanged = { onIntent(AppIntent.SetReaderAvoidPageBreak(it)) },
        onJustifyTextChanged = { onIntent(AppIntent.SetReaderJustifyText(it)) },
        onCharacterSpacingChanged = { onIntent(AppIntent.SetReaderCharacterSpacing(it)) },
        onPopupWidthChanged = { onIntent(AppIntent.SetPopupWidth(it)) },
        onPopupHeightChanged = { onIntent(AppIntent.SetPopupHeight(it)) },
        onTogglePopupFullWidth = { onIntent(AppIntent.TogglePopupFullWidth) },
        onTogglePopupSwipeToDismiss = { onIntent(AppIntent.TogglePopupSwipeToDismiss) },
        onPopupSwipeThresholdChanged = { onIntent(AppIntent.SetPopupSwipeThreshold(it)) },
    )

}

@Composable
private fun ReaderStatus(text: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.orEmpty(),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun ReaderHeaderInfo(
    title: String,
    progress: String?,
    modifier: Modifier = Modifier,
) {
    if (title.isBlank() && progress.isNullOrBlank()) return
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.44f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        progress?.let {
            Text(
                text = it,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.44f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ReaderBottomChrome(
    isDark: Boolean,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onAppearance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonContentColor = if (isDark) Color(0xFFF3F1EA) else Color(0xFF1C1B18)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 22.dp,
                end = 22.dp,
                bottom = bottomPadding + 12.dp,
            ),
    ) {
        FloatingReaderButton(
            isDark = isDark,
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart),
        ) {
            Icon(
                MiuixIcons.Back,
                tint = buttonContentColor,
                contentDescription = stringResource(Res.string.cd_back)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FloatingReaderButton(
                isDark = isDark,
                onClick = onAppearance,
            ) {
                Icon(
                    MiuixIcons.Background,
                    tint = buttonContentColor,
                    contentDescription = stringResource(Res.string.cd_appearance)
                )
            }
            FloatingReaderButton(
                isDark = isDark,
                onClick = onMenu,
            ) {
                Icon(
                    MiuixIcons.ListView,
                    tint = buttonContentColor,
                    contentDescription = stringResource(Res.string.cd_table_of_contents)
                )
            }
        }
    }
}

@Composable
private fun FloatingReaderButton(
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val buttonBackground = if (isDark) {
        Color(0xFF242424).copy(alpha = 0.94f)
    } else {
        Color(0xFFFFFCF5).copy(alpha = 0.94f)
    }
    val buttonShadow = if (isDark) {
        Color.Black.copy(alpha = 0.34f)
    } else {
        Color(0xFF1C1B18).copy(alpha = 0.16f)
    }
    Box(
        modifier = modifier
            .size(52.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                spotColor = buttonShadow,
            )
            .clip(CircleShape)
            .background(buttonBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun ChapterSheet(
    show: Boolean,
    isDark: Boolean,
    title: String,
    currentCharacter: Int,
    totalCharacters: Int,
    rows: List<ReaderTocItem>,
    currentChapterIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (ReaderTocItem) -> Unit,
) {
    ReaderSheetTheme(isDark = isDark) {
        WindowBottomSheet(
            show = show,
            title = stringResource(Res.string.cd_table_of_contents),
            onDismissRequest = onDismiss,
        ) {
            Column(
                modifier = Modifier.padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = title,
                    color = MiuixTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$currentCharacter / $totalCharacters",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(rows) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(row) }
                                .background(
                                    if (row.chapterIndex == currentChapterIndex) {
                                        MiuixTheme.colorScheme.surfaceContainerHighest
                                    } else {
                                        MiuixTheme.colorScheme.surface
                                    },
                                )
                                .padding(
                                    start = 12.dp + 16.dp * row.indentLevel,
                                    end = 12.dp,
                                    top = 10.dp,
                                    bottom = 10.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = row.label,
                                modifier = Modifier.weight(1f),
                                color = MiuixTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            row.characterCount?.let {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = it.toString(),
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceSheet(
    show: Boolean,
    isDark: Boolean,
    readerThemeMode: ReaderThemeMode,
    verticalWriting: Boolean,
    continuousMode: Boolean,
    hideFurigana: Boolean,
    fullscreen: Boolean,
    fontSize: Int,
    lineHeight: Double,
    horizontalPadding: Int,
    verticalPadding: Int,
    avoidPageBreak: Boolean,
    justifyText: Boolean,
    characterSpacing: Double,
    popupWidth: Int,
    popupHeight: Int,
    popupFullWidth: Boolean,
    popupSwipeToDismiss: Boolean,
    popupSwipeThreshold: Int,
    onDismiss: () -> Unit,
    onReaderThemeModeSelected: (ReaderThemeMode) -> Unit,
    onToggleWritingMode: () -> Unit,
    onToggleContinuousMode: () -> Unit,
    onToggleHideFurigana: () -> Unit,
    onFullscreenChanged: (Boolean) -> Unit,
    onFontSizeChanged: (Int) -> Unit,
    onLineHeightChanged: (Double) -> Unit,
    onHorizontalPaddingChanged: (Int) -> Unit,
    onVerticalPaddingChanged: (Int) -> Unit,
    onAvoidPageBreakChanged: (Boolean) -> Unit,
    onJustifyTextChanged: (Boolean) -> Unit,
    onCharacterSpacingChanged: (Double) -> Unit,
    onPopupWidthChanged: (Int) -> Unit,
    onPopupHeightChanged: (Int) -> Unit,
    onTogglePopupFullWidth: () -> Unit,
    onTogglePopupSwipeToDismiss: () -> Unit,
    onPopupSwipeThresholdChanged: (Int) -> Unit,
) {
    ReaderSheetTheme(isDark = isDark) {
        WindowBottomSheet(
            show = show,
            title = stringResource(Res.string.cd_appearance),
            onDismissRequest = onDismiss,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    ReaderAppearanceSettingsCard(
                        readerThemeMode = readerThemeMode,
                        verticalWriting = verticalWriting,
                        continuousMode = continuousMode,
                        hideFurigana = hideFurigana,
                        fullscreen = fullscreen,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        horizontalPadding = horizontalPadding,
                        verticalPadding = verticalPadding,
                        avoidPageBreak = avoidPageBreak,
                        justifyText = justifyText,
                        characterSpacing = characterSpacing,
                        popupWidth = popupWidth,
                        popupHeight = popupHeight,
                        popupFullWidth = popupFullWidth,
                        popupSwipeToDismiss = popupSwipeToDismiss,
                        popupSwipeThreshold = popupSwipeThreshold,
                        modifier = Modifier.fillMaxWidth(),
                        onReaderThemeModeSelected = onReaderThemeModeSelected,
                        onToggleWritingMode = onToggleWritingMode,
                        onToggleContinuousMode = onToggleContinuousMode,
                        onToggleHideFurigana = onToggleHideFurigana,
                        onFullscreenChanged = onFullscreenChanged,
                        onFontSizeChanged = onFontSizeChanged,
                        onLineHeightChanged = onLineHeightChanged,
                        onHorizontalPaddingChanged = onHorizontalPaddingChanged,
                        onVerticalPaddingChanged = onVerticalPaddingChanged,
                        onAvoidPageBreakChanged = onAvoidPageBreakChanged,
                        onJustifyTextChanged = onJustifyTextChanged,
                        onCharacterSpacingChanged = onCharacterSpacingChanged,
                        onPopupWidthChanged = onPopupWidthChanged,
                        onPopupHeightChanged = onPopupHeightChanged,
                        onTogglePopupFullWidth = onTogglePopupFullWidth,
                        onTogglePopupSwipeToDismiss = onTogglePopupSwipeToDismiss,
                        onPopupSwipeThresholdChanged = onPopupSwipeThresholdChanged,
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ReaderSheetTheme(
    isDark: Boolean,
    content: @Composable () -> Unit,
) {
    val controller = remember(isDark) {
        ThemeController(
            colorSchemeMode = if (isDark) ColorSchemeMode.Dark else ColorSchemeMode.Light,
        )
    }
    MiuixTheme(
        controller = controller,
        smoothRounding = true,
        content = content,
    )
}

@Composable
private fun ReaderLookupPopup(
    lookup: ReaderLookupState,
    popupIndex: Int,
    state: AppState,
    isDark: Boolean,
    isVertical: Boolean,
    viewportWidth: Dp,
    viewportHeight: Dp,
    readerTopPadding: Dp,
    readerBottomPadding: Dp,
    blurEnabled: Boolean,
    backdrop: LayerBackdrop?,
    onIntent: (AppIntent) -> Unit,
    onDismiss: () -> Unit,
    onSwipeDismiss: () -> Unit,
) {
    if (!lookup.visible) return

    val layout = remember(
        lookup.rect,
        popupIndex,
        isVertical,
        viewportWidth,
        viewportHeight,
        readerTopPadding,
        readerBottomPadding,
        state.settings.popupWidth,
        state.settings.popupHeight,
        state.settings.popupFullWidth,
    ) {
        lookup.rect?.let { rect ->
            val selectionTop = if (popupIndex == 0) readerTopPadding + rect.y.dp else rect.y.dp
            val selectionBottom = selectionTop + rect.height.dp
            PopupLayout(
                selectionLeft = rect.x.dp,
                selectionTop = selectionTop,
                selectionRight = rect.x.dp + rect.width.dp,
                selectionBottom = selectionBottom,
                screenWidth = viewportWidth,
                screenHeight = viewportHeight,
                maxWidth = state.settings.popupWidth.dp,
                maxHeight = state.settings.popupHeight.dp,
                isVertical = isVertical,
                isFullWidth = popupIndex == 0 && state.settings.popupFullWidth,
                topInset = readerTopPadding,
                bottomInset = readerBottomPadding,
            )
        } ?: PopupLayout(
            selectionLeft = viewportWidth / 2f,
            selectionTop = viewportHeight - readerBottomPadding - 1.dp,
            selectionRight = viewportWidth / 2f,
            selectionBottom = viewportHeight - readerBottomPadding,
            screenWidth = viewportWidth,
            screenHeight = viewportHeight,
            maxWidth = state.settings.popupWidth.dp,
            maxHeight = state.settings.popupHeight.dp,
            isVertical = false,
            isFullWidth = popupIndex == 0 && state.settings.popupFullWidth,
            topInset = readerTopPadding,
            bottomInset = readerBottomPadding,
        )
    }

    val outsideInteractionSource = remember { MutableInteractionSource() }
    val popupInteractionSource = remember { MutableInteractionSource() }
    val popupShape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f)
            .clickable(
                interactionSource = outsideInteractionSource,
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        Box(
            modifier = Modifier
                .offset(x = layout.left, y = layout.top)
                .width(layout.width)
                .height(layout.height)
                .shadow(
                    elevation = 18.dp,
                    shape = popupShape,
                    spotColor = Color.Black.copy(alpha = 0.22f),
                )
                .clip(popupShape)
                .clickable(
                    interactionSource = popupInteractionSource,
                    indication = null,
                    onClick = {},
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (blurEnabled && backdrop != null) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = popupShape,
                                blurRadius = 28f,
                                noiseCoefficient = 0f,
                                colors = BlurColors(
                                    blendColors = listOf(
                                        BlendColorEntry(
                                            color = MiuixTheme.colorScheme.surface.copy(
                                                alpha = if (isDark) 0.9f else 0.86f,
                                            ),
                                        ),
                                    ),
                                ),
                            )
                        } else {
                            Modifier.background(MiuixTheme.colorScheme.surface.copy(alpha = 0.96f))
                        }
                    ),
            )
            DictionaryWebView(
                query = lookup.selectedText,
                entries = lookup.entries,
                dictionaryStyles = lookup.dictionaryStyles,
                isSearching = lookup.isSearching,
                hasSearched = lookup.selectedText.isNotBlank(),
                errorMessage = lookup.errorMessage,
                maxResults = state.settings.maxResults,
                scanLength = state.settings.scanLength,
                collapseDictionaries = state.settings.collapseDictionaries,
                compactGlossaries = state.settings.compactGlossaries,
                showExpressionTags = state.settings.showExpressionTags,
                harmonicFrequency = state.settings.harmonicFrequency,
                deduplicatePitchAccents = state.settings.deduplicatePitchAccents,
                isDark = isDark,
                audioSources = state.settings.audioSources,
                audioEnableAutoplay = state.settings.audioEnableAutoplay,
                audioPlaybackMode = state.settings.audioPlaybackMode,
                ankiEnabled = state.settings.anki.enabled,
                onAddAnkiCard = { onIntent(AppIntent.AddAnkiCard(it)) },
                enableInternalPopup = false,
                swipeDismissThreshold = if (state.settings.popupSwipeToDismiss) {
                    state.settings.popupSwipeThreshold
                } else {
                    0
                },
                onPopupTextSelected = { text, rect ->
                    onIntent(AppIntent.ReaderPopupTextSelected(popupIndex, text, rect))
                },
                onSwipeDismiss = onSwipeDismiss,
                modifier = Modifier.fillMaxSize(),
                contentBottomPadding = 0.dp,
                edgeToEdgeContent = true,
                transparentBackground = blurEnabled,
            )
        }
    }
}

@Composable
private fun rememberReaderPopupBackdrop(
    blurEnabled: Boolean,
    readerBackground: Color,
): LayerBackdrop? {
    if (!blurEnabled || !isRenderEffectSupported()) return null
    return rememberLayerBackdrop {
        drawRect(readerBackground)
        drawContent()
    }
}

private data class PopupLayout(
    val width: Dp,
    val height: Dp,
    val left: Dp,
    val top: Dp,
)

private fun PopupLayout(
    selectionLeft: Dp,
    selectionTop: Dp,
    selectionRight: Dp,
    selectionBottom: Dp,
    screenWidth: Dp,
    screenHeight: Dp,
    maxWidth: Dp,
    maxHeight: Dp,
    isVertical: Boolean,
    isFullWidth: Boolean,
    topInset: Dp,
    bottomInset: Dp,
): PopupLayout {
    val popupPadding = 4.dp
    val screenBorderPadding = 6.dp
    val spaceLeft = selectionLeft - popupPadding
    val spaceRight = screenWidth - selectionRight - popupPadding
    val spaceAbove = selectionTop - topInset - popupPadding
    val spaceBelow = screenHeight - bottomInset - selectionBottom - popupPadding
    val width = when {
        isFullWidth -> screenWidth - screenBorderPadding * 2f
        isVertical -> minOf(maxOf(spaceLeft, spaceRight) - screenBorderPadding, maxWidth)
        else -> minOf(screenWidth - screenBorderPadding * 2f, maxWidth)
    }.coerceAtLeast(1.dp)
    val height = when {
        isVertical || isFullWidth -> maxHeight
        else -> minOf(maxOf(spaceAbove, spaceBelow) - screenBorderPadding, maxHeight)
    }.coerceAtLeast(1.dp)

    val centerX: Dp
    val centerY: Dp
    if (isFullWidth) {
        centerX = width / 2f + screenBorderPadding
        centerY = screenHeight - bottomInset - height / 2f - screenBorderPadding
    } else if (isVertical) {
        val unclampedX = if (spaceRight >= spaceLeft) {
            selectionRight + popupPadding + width / 2f
        } else {
            selectionLeft - popupPadding - width / 2f
        }
        centerX = unclampedX.coerceIn(width / 2f, screenWidth - width / 2f)
        val unclampedY = selectionTop + height / 2f
        centerY = unclampedY.coerceIn(
            height / 2f + screenBorderPadding + topInset,
            screenHeight - bottomInset - height / 2f - screenBorderPadding,
        )
    } else {
        val unclampedX = selectionLeft + width / 2f
        centerX = unclampedX.coerceIn(
            width / 2f + screenBorderPadding,
            screenWidth - width / 2f - screenBorderPadding,
        )
        val showBelow = spaceBelow >= height
        val unclampedY = if (showBelow) {
            selectionBottom + popupPadding + height / 2f
        } else {
            selectionTop - popupPadding - height / 2f
        }
        centerY = unclampedY.coerceIn(
            height / 2f + topInset + screenBorderPadding,
            screenHeight - bottomInset - height / 2f - screenBorderPadding,
        )
    }

    return PopupLayout(
        width = width,
        height = height,
        left = centerX - width / 2f,
        top = centerY - height / 2f,
    )
}

private fun Double.formatPercent(): String {
    val rounded = (this * 10.0).toInt() / 10.0
    return rounded.toString()
}
