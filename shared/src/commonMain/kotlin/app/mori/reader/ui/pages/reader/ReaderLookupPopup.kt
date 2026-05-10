package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.mori.reader.data.anki.AnkiConnectionMode
import app.mori.reader.data.anki.buildReaderAnkiMiningContext
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.anki.presentation.AnkiIntent
import app.mori.reader.features.lookup.presentation.ReaderLookupState
import app.mori.reader.features.reader.presentation.ReaderIntent
import app.mori.reader.features.reader.presentation.ReaderState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_pause
import app.mori.reader.shared.generated.resources.cd_play
import app.mori.reader.shared.generated.resources.cd_play_pronunciation
import app.mori.reader.shared.generated.resources.dict_no_results
import app.mori.reader.shared.generated.resources.dict_placeholder
import app.mori.reader.shared.generated.resources.dict_searching
import app.mori.reader.shared.generated.resources.sasayaki_continue_from_cue
import app.mori.reader.shared.generated.resources.sasayaki_replay
import app.mori.reader.ui.pages.dictionary.DictionaryPopupActionBar
import app.mori.reader.ui.pages.dictionary.DictionaryWebView
import app.mori.reader.ui.pages.dictionary.DictionaryWebViewCallbacks
import app.mori.reader.ui.pages.dictionary.DictionaryWebViewSettings
import app.mori.reader.ui.pages.dictionary.DictionaryWebViewState
import app.mori.reader.ui.text.asString
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Import
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun ReaderLookupPopup(
    lookup: ReaderLookupState,
    popupIndex: Int,
    reader: ReaderState,
    settings: AppSettings,
    ankiDuplicateExpression: String?,
    isDark: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    isVertical: Boolean,
    viewportWidth: Dp,
    viewportHeight: Dp,
    readerTopPadding: Dp,
    readerBottomPadding: Dp,
    blurEnabled: Boolean,
    backdrop: LayerBackdrop?,
    onReaderIntent: (ReaderIntent) -> Unit,
    onAnkiIntent: (AnkiIntent) -> Unit,
    onDismiss: () -> Unit,
    onSwipeDismiss: () -> Unit,
) {
    if (!lookup.visible) return

    val layout =
        remember(
            lookup.rect,
            popupIndex,
            isVertical,
            viewportWidth,
            viewportHeight,
            readerTopPadding,
            readerBottomPadding,
            settings.popup.width,
            settings.popup.height,
            settings.popup.fullWidth,
        ) {
            lookup.rect?.let { rect ->
                val selectionTop = if (popupIndex == 0) readerTopPadding + rect.y.dp else rect.y.dp
                val selectionBottom = selectionTop + rect.height.dp
                calculateReaderPopupLayout(
                    selectionLeft = rect.x.dp,
                    selectionTop = selectionTop,
                    selectionRight = rect.x.dp + rect.width.dp,
                    selectionBottom = selectionBottom,
                    screenWidth = viewportWidth,
                    screenHeight = viewportHeight,
                    maxWidth = settings.popup.width.dp,
                    maxHeight = settings.popup.height.dp,
                    isVertical = isVertical,
                    isFullWidth = popupIndex == 0 && settings.popup.fullWidth,
                    topInset = readerTopPadding,
                    bottomInset = readerBottomPadding,
                )
            } ?: calculateReaderPopupLayout(
                selectionLeft = viewportWidth / 2f,
                selectionTop = viewportHeight - readerBottomPadding - 1.dp,
                selectionRight = viewportWidth / 2f,
                selectionBottom = viewportHeight - readerBottomPadding,
                screenWidth = viewportWidth,
                screenHeight = viewportHeight,
                maxWidth = settings.popup.width.dp,
                maxHeight = settings.popup.height.dp,
                isVertical = false,
                isFullWidth = popupIndex == 0 && settings.popup.fullWidth,
                topInset = readerTopPadding,
                bottomInset = readerBottomPadding,
            )
        }

    val outsideInteractionSource = remember { MutableInteractionSource() }
    val popupInteractionSource = remember { MutableInteractionSource() }
    val popupShape = RoundedCornerShape(10.dp)
    val ankiSettings = settings.anki
    val ankiNeedsAudio = ankiSettings.fieldMappings.values.any { it.contains("{audio}") }
    var canNavigateBack by remember(lookup.id) { mutableStateOf(false) }
    var canNavigateForward by remember(lookup.id) { mutableStateOf(false) }
    var navigateBackToken by remember(lookup.id) { mutableIntStateOf(0) }
    var navigateForwardToken by remember(lookup.id) { mutableIntStateOf(0) }
    ReaderSheetTheme(
        isDark = isDark,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .zIndex(20f)
                    .clickable(
                        interactionSource = outsideInteractionSource,
                        indication = null,
                        onClick = onDismiss,
                    ),
        ) {
            Box(
                modifier =
                    Modifier
                        .offset(x = layout.left, y = layout.top)
                        .width(layout.width)
                        .height(layout.height)
                        .shadow(
                            elevation = 18.dp,
                            shape = popupShape,
                            spotColor = Color.Black.copy(alpha = 0.22f),
                        ).clip(popupShape)
                        .clickable(
                            interactionSource = popupInteractionSource,
                            indication = null,
                            onClick = {},
                        ),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .then(
                                if (blurEnabled && backdrop != null) {
                                    Modifier.textureBlur(
                                        backdrop = backdrop,
                                        shape = popupShape,
                                        blurRadius = 28f,
                                        noiseCoefficient = 0f,
                                        colors =
                                            BlurColors(
                                                blendColors =
                                                    listOf(
                                                        BlendColorEntry(
                                                            color =
                                                                MiuixTheme.colorScheme.surface.copy(
                                                                    alpha = if (isDark) 0.9f else 0.86f,
                                                                ),
                                                        ),
                                                    ),
                                            ),
                                    )
                                } else {
                                    Modifier.background(MiuixTheme.colorScheme.surface.copy(alpha = 0.96f))
                                },
                            ),
                )
                Column(modifier = Modifier.fillMaxSize()) {
                    DictionaryPopupActionBar(
                        canNavigateBack = canNavigateBack,
                        canNavigateForward = canNavigateForward,
                        blurEnabled = blurEnabled,
                        backdrop = backdrop,
                        isDark = isDark,
                        onNavigateBack = { navigateBackToken++ },
                        onNavigateForward = { navigateForwardToken++ },
                        onClose = onDismiss,
                    )
                    lookup.sasayakiCueId?.let { cueId ->
                        SasayakiPopupControls(
                            isPlaying = reader.sasayakiPlayer.isPlaying,
                            blurEnabled = blurEnabled,
                            backdrop = backdrop,
                            isDark = isDark,
                            onReplay = { onReaderIntent(ReaderIntent.ReplayCue(cueId)) },
                            onToggle = { onReaderIntent(ReaderIntent.TogglePlayback) },
                            onContinue = { onReaderIntent(ReaderIntent.ContinueFromCue(cueId)) },
                        )
                    }
                    DictionaryWebView(
                        state =
                            DictionaryWebViewState(
                                query = lookup.selectedText,
                                entries = lookup.entries,
                                dictionaryStyles = lookup.dictionaryStyles,
                                isSearching = lookup.isSearching,
                                hasSearched = lookup.selectedText.isNotBlank(),
                                errorMessage = lookup.errorMessage?.asString(),
                                searchingMessage = stringResource(Res.string.dict_searching),
                                noResultsMessage = stringResource(Res.string.dict_no_results),
                                idleMessage = stringResource(Res.string.dict_placeholder),
                                playPronunciationLabel = stringResource(Res.string.cd_play_pronunciation),
                            ),
                        config =
                            DictionaryWebViewSettings(
                                maxResults = settings.dictionary.maxResults,
                                scanLength = settings.dictionary.scanLength,
                                collapseDictionaries = settings.dictionary.collapseDictionaries,
                                compactGlossaries = settings.dictionary.compactGlossaries,
                                showExpressionTags = settings.dictionary.showExpressionTags,
                                harmonicFrequency = settings.dictionary.harmonicFrequency,
                                deduplicatePitchAccents = settings.dictionary.deduplicatePitchAccents,
                                isDark = isDark,
                                audioSources = settings.audio.sources,
                                audioEnableAutoplay = settings.audio.enableAutoplay,
                                audioPlaybackMode = settings.audio.playbackMode,
                                enableInternalPopup = false,
                                swipeDismissThreshold =
                                    if (settings.popup.swipeToDismiss) {
                                        settings.popup.swipeThreshold
                                    } else {
                                        0
                                    },
                                contentBottomPadding = 0.dp,
                                edgeToEdgeContent = true,
                                transparentBackground = blurEnabled,
                                ankiNeedsAudio = ankiNeedsAudio,
                                ankiAllowDuplicates = ankiSettings.allowDuplicates,
                                ankiUseAnkiConnect = ankiSettings.connectionMode == AnkiConnectionMode.AnkiConnect,
                                ankiEmbedMedia = ankiSettings.embedMedia,
                                ankiCompactGlossaries = ankiSettings.compactGlossaries,
                                ankiDuplicateExpression = ankiDuplicateExpression,
                                navigateBackToken = navigateBackToken,
                                navigateForwardToken = navigateForwardToken,
                            ),
                        callbacks =
                            DictionaryWebViewCallbacks(
                                onPopupTextSelected = { text, rect ->
                                    onReaderIntent(
                                        ReaderIntent.PopupTextSelected(
                                            popupIndex,
                                            text,
                                            rect,
                                        ),
                                    )
                                },
                                onMineEntry = { content ->
                                    onAnkiIntent(
                                        AnkiIntent.MineNote(
                                            content = content,
                                            context =
                                                buildReaderAnkiMiningContext(
                                                    book = reader.book,
                                                    sentence = lookup.sentence.ifBlank { lookup.selectedText },
                                                    sasayakiAudioAssetInfo = reader.sasayakiAudioAssetInfo,
                                                    sasayakiMatches = reader.sasayakiMatches,
                                                    sasayakiDelayMs = reader.sasayakiPlayer.delayMs,
                                                    sasayakiCueId = lookup.sasayakiCueId,
                                                ),
                                        ),
                                    )
                                },
                                onCheckDuplicate = { expression ->
                                    onAnkiIntent(AnkiIntent.CheckDuplicate(expression))
                                },
                                onSwipeDismiss = onSwipeDismiss,
                                onNavigationStateChange = { back, forward ->
                                    canNavigateBack = back
                                    canNavigateForward = forward
                                },
                            ),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SasayakiPopupControls(
    isPlaying: Boolean,
    blurEnabled: Boolean,
    backdrop: LayerBackdrop?,
    isDark: Boolean,
    onReplay: () -> Unit,
    onToggle: () -> Unit,
    onContinue: () -> Unit,
) {
    val density = LocalDensity.current
    val topBarShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (blurEnabled && backdrop != null) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = topBarShape,
                            blurRadius = 25f * density.density,
                            noiseCoefficient = 0f,
                            colors =
                                BlurColors(
                                    blendColors =
                                        listOf(
                                            BlendColorEntry(
                                                color =
                                                    MiuixTheme.colorScheme.surface.copy(
                                                        alpha = if (isDark) 0.82f else 0.74f,
                                                    ),
                                            ),
                                        ),
                                ),
                        )
                    } else {
                        Modifier.background(
                            MiuixTheme.colorScheme.surfaceContainerHighest.copy(
                                alpha = if (isDark) 0.78f else 0.92f,
                            ),
                            shape = topBarShape,
                        )
                    },
                ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            CompositionLocalProvider(LocalContentColor provides MiuixTheme.colorScheme.onSurface) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onReplay, minWidth = 32.dp, minHeight = 32.dp) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = MiuixIcons.Refresh,
                            contentDescription = stringResource(Res.string.sasayaki_replay),
                        )
                    }
                    IconButton(onClick = onToggle, minWidth = 32.dp, minHeight = 32.dp) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = if (isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
                            contentDescription =
                                if (isPlaying) {
                                    stringResource(Res.string.cd_pause)
                                } else {
                                    stringResource(Res.string.cd_play)
                                },
                        )
                    }
                    IconButton(onClick = onContinue, minWidth = 32.dp, minHeight = 32.dp) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = MiuixIcons.Import,
                            contentDescription = stringResource(Res.string.sasayaki_continue_from_cue),
                        )
                    }
                }
            }
            HorizontalDivider(color = MiuixTheme.colorScheme.dividerLine)
        }
    }
}

@Composable
internal fun rememberReaderPopupBackdrop(
    blurEnabled: Boolean,
    readerBackground: Color,
): LayerBackdrop? {
    if (!blurEnabled || !isRenderEffectSupported()) return null
    return rememberLayerBackdrop {
        drawRect(readerBackground)
        drawContent()
    }
}
