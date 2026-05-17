package app.mori.reader.ui.pages.lookup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.mori.reader.data.anki.AnkiConnectionMode
import app.mori.reader.data.anki.AnkiMiningContent
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.lookup.presentation.ReaderLookupState
import app.mori.reader.features.lookup.presentation.ReaderSelectionRect
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_play_pronunciation
import app.mori.reader.shared.generated.resources.dict_no_results
import app.mori.reader.shared.generated.resources.dict_placeholder
import app.mori.reader.shared.generated.resources.dict_searching
import app.mori.reader.ui.pages.dictionary.DictionaryPopupActionBar
import app.mori.reader.ui.pages.dictionary.DictionaryWebView
import app.mori.reader.ui.pages.dictionary.DictionaryWebViewCallbacks
import app.mori.reader.ui.pages.dictionary.DictionaryWebViewSettings
import app.mori.reader.ui.pages.dictionary.DictionaryWebViewState
import app.mori.reader.ui.text.asString
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun LookupPopupSurface(
    lookup: ReaderLookupState,
    layout: LookupPopupLayout,
    settings: AppSettings,
    ankiDuplicateExpression: String?,
    isDark: Boolean,
    materialEInkMode: Boolean,
    blurEnabled: Boolean,
    backdrop: LayerBackdrop?,
    zIndex: Float,
    onDismiss: () -> Unit,
    onSwipeDismiss: () -> Unit,
    onPopupTextSelected: (String, ReaderSelectionRect?) -> Unit,
    onMineEntry: (AnkiMiningContent) -> Unit,
    onCheckDuplicate: (String) -> Unit,
    modifier: Modifier = Modifier,
    onVerticalScrollActiveChange: (Boolean) -> Unit = {},
    topContent: @Composable ColumnScope.() -> Unit = {},
) {
    val outsideInteractionSource = remember { MutableInteractionSource() }
    val popupInteractionSource = remember { MutableInteractionSource() }
    val popupShape =
        if (materialEInkMode) {
            RoundedCornerShape(0.dp)
        } else {
            RoundedCornerShape(10.dp)
        }
    val effectiveBlurEnabled = blurEnabled && !materialEInkMode
    val ankiSettings = settings.anki
    val ankiNeedsAudio = ankiSettings.fieldMappings.values.any { it.contains("{audio}") }
    var canNavigateBack by remember(lookup.id) { mutableStateOf(false) }
    var canNavigateForward by remember(lookup.id) { mutableStateOf(false) }
    var navigateBackToken by remember(lookup.id) { mutableIntStateOf(0) }
    var navigateForwardToken by remember(lookup.id) { mutableIntStateOf(0) }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .zIndex(zIndex)
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
                        elevation = if (materialEInkMode) 0.dp else 18.dp,
                        shape = popupShape,
                        spotColor = Color.Black.copy(alpha = 0.22f),
                    ).clip(popupShape)
                    .then(
                        if (materialEInkMode) {
                            Modifier.border(
                                width = 2.dp,
                                color = if (isDark) Color.White else Color.Black,
                                shape = popupShape,
                            )
                        } else {
                            Modifier
                        }
                    )
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
                            if (effectiveBlurEnabled && backdrop != null) {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = popupShape,
                                    blurRadius = 9f,
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
                                Modifier.background(
                                    if (materialEInkMode) {
                                        MiuixTheme.colorScheme.surface
                                    } else {
                                        MiuixTheme.colorScheme.surface.copy(alpha = 0.96f)
                                    },
                                )
                            },
                        ),
            )
            Column(modifier = Modifier.fillMaxSize()) {
                DictionaryPopupActionBar(
                    canNavigateBack = canNavigateBack,
                    canNavigateForward = canNavigateForward,
                    blurEnabled = effectiveBlurEnabled,
                    backdrop = backdrop,
                    isDark = isDark,
                    materialEInkMode = materialEInkMode,
                    onNavigateBack = { navigateBackToken++ },
                    onNavigateForward = { navigateForwardToken++ },
                    onClose = onDismiss,
                )
                topContent()
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
                            eInkMode = materialEInkMode,
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
                            transparentBackground = effectiveBlurEnabled,
                            eInkEntryBorderEnabled = false,
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
                            onVerticalScrollActiveChange = onVerticalScrollActiveChange,
                            onPopupTextSelected = onPopupTextSelected,
                            onMineEntry = onMineEntry,
                            onCheckDuplicate = onCheckDuplicate,
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
