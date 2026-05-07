package app.mori.reader.ui.pages.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.dictionary.presentation.DictionaryIntent
import app.mori.reader.features.lookup.presentation.ReaderLookupState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_play_pronunciation
import app.mori.reader.shared.generated.resources.dict_no_results
import app.mori.reader.shared.generated.resources.dict_placeholder
import app.mori.reader.shared.generated.resources.dict_searching
import app.mori.reader.ui.text.asString
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun DictionaryLookupPopup(
    lookup: ReaderLookupState,
    popupIndex: Int,
    settings: AppSettings,
    isDark: Boolean,
    viewportWidth: Dp,
    viewportHeight: Dp,
    topInset: Dp,
    bottomInset: Dp,
    blurEnabled: Boolean,
    backdrop: LayerBackdrop?,
    onDictionaryIntent: (DictionaryIntent) -> Unit,
    onVerticalScrollActiveChange: (Boolean) -> Unit,
    onSwipeDismiss: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!lookup.visible) return

    val layout =
        remember(
            lookup.rect,
            viewportWidth,
            viewportHeight,
            topInset,
            bottomInset,
            settings.popup.width,
            settings.popup.height,
            settings.popup.fullWidth,
        ) {
            lookup.rect?.let { rect ->
                calculateDictionaryPopupLayout(
                    selectionLeft = rect.x.dp,
                    selectionTop = rect.y.dp,
                    selectionRight = rect.x.dp + rect.width.dp,
                    selectionBottom = rect.y.dp + rect.height.dp,
                    screenWidth = viewportWidth,
                    screenHeight = viewportHeight,
                    maxWidth = settings.popup.width.dp,
                    maxHeight = settings.popup.height.dp,
                    isVertical = false,
                    isFullWidth = popupIndex == 0 && settings.popup.fullWidth,
                    topInset = topInset,
                    bottomInset = bottomInset,
                )
            } ?: calculateDictionaryPopupLayout(
                selectionLeft = viewportWidth / 2f,
                selectionTop = viewportHeight - bottomInset - 1.dp,
                selectionRight = viewportWidth / 2f,
                selectionBottom = viewportHeight - bottomInset,
                screenWidth = viewportWidth,
                screenHeight = viewportHeight,
                maxWidth = settings.popup.width.dp,
                maxHeight = settings.popup.height.dp,
                isVertical = false,
                isFullWidth = popupIndex == 0 && settings.popup.fullWidth,
                topInset = topInset,
                bottomInset = bottomInset,
            )
        }

    val outsideInteractionSource = remember { MutableInteractionSource() }
    val popupInteractionSource = remember { MutableInteractionSource() }
    val popupShape = RoundedCornerShape(10.dp)
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .zIndex(20f + popupIndex)
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
                        edgeToEdgeContent = true,
                        transparentBackground = blurEnabled,
                    ),
                callbacks =
                    DictionaryWebViewCallbacks(
                        onVerticalScrollActiveChange = onVerticalScrollActiveChange,
                        onPopupTextSelected = { text, rect ->
                            onDictionaryIntent(
                                DictionaryIntent.PopupTextSelected(
                                    parentIndex = popupIndex,
                                    text = text,
                                    rect = rect,
                                ),
                            )
                        },
                        onSwipeDismiss = onSwipeDismiss,
                    ),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
