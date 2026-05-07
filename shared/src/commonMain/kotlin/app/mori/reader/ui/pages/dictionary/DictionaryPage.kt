package app.mori.reader.ui.pages.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.features.dictionary.presentation.DictionaryIntent
import app.mori.reader.features.dictionary.presentation.DictionaryState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.dict_no_results
import app.mori.reader.shared.generated.resources.dict_placeholder
import app.mori.reader.shared.generated.resources.dict_searching
import app.mori.reader.shared.generated.resources.cd_play_pronunciation
import app.mori.reader.ui.components.scaffold.moriFixedHorizontalPadding
import app.mori.reader.ui.text.asString
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DictionaryPage(
    dictionaryState: DictionaryState,
    settings: AppSettings,
    fixedPadding: PaddingValues,
    onDictionaryIntent: (DictionaryIntent) -> Unit,
    onWebViewVerticalScrollActiveChange: (Boolean) -> Unit = {},
) {
    val query = dictionaryState.query
    val shouldComposeWebView =
        query.isNotBlank() || dictionaryState.isSearching || dictionaryState.hasSearched
    val horizontalContentInset = Modifier.moriFixedHorizontalPadding(fixedPadding)
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomPadding = maxOf(
        fixedPadding.calculateBottomPadding(),
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
    )
    val searchTopPadding = statusBarPadding + 12.dp
    val blurEnabled = settings.appearance.blurEnabled
    val contentBackdrop = rememberDictionaryContentBackdrop(blurEnabled)
    val isDark = when (settings.appearance.themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    LaunchedEffect(shouldComposeWebView) {
        if (!shouldComposeWebView) {
            onWebViewVerticalScrollActiveChange(false)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (contentBackdrop != null) Modifier.layerBackdrop(contentBackdrop)
                        else Modifier
                    ),
            ) {
                if (shouldComposeWebView) {
                    DictionaryWebView(
                        state = DictionaryWebViewState(
                            query = query,
                            entries = dictionaryState.entries,
                            dictionaryStyles = dictionaryState.dictionaryStyles,
                            isSearching = dictionaryState.isSearching,
                            hasSearched = dictionaryState.hasSearched,
                            errorMessage = dictionaryState.errorMessage?.asString(),
                            searchingMessage = stringResource(Res.string.dict_searching),
                            noResultsMessage = stringResource(Res.string.dict_no_results),
                            idleMessage = stringResource(Res.string.dict_placeholder),
                            playPronunciationLabel = stringResource(Res.string.cd_play_pronunciation),
                        ),
                        config = DictionaryWebViewSettings(
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
                            contentTopPadding = searchTopPadding +
                                DictionarySearchFieldHeight +
                                DictionarySearchFieldContentGap,
                            enableInternalPopup = false,
                            contentBottomPadding = bottomPadding,
                        ),
                        callbacks = DictionaryWebViewCallbacks(
                            onVerticalScrollActiveChange = onWebViewVerticalScrollActiveChange,
                            onPopupTextSelected = { text, rect ->
                                onDictionaryIntent(
                                    DictionaryIntent.PopupTextSelected(
                                        parentIndex = null,
                                        text = text,
                                        rect = rect
                                    )
                                )
                            },
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .then(horizontalContentInset)
                            .padding(
                                start = 12.dp,
                                end = 12.dp,
                            )
                            // This 0.1dp rounded corner clip is a hack for flickering.
                            // When blur effect is enabled, the WebView will keep flickering without the rounded corner.
                            // I don't know why but this magically fix it. DO NOT REMOVE IT!!!
                            .clip(RoundedCornerShape(0.1.dp)),
                    )
                } else {
                    DictionaryPlaceholder(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(horizontalContentInset)
                            .padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = searchTopPadding +
                                    DictionarySearchFieldHeight +
                                    DictionarySearchFieldContentGap +
                                    36.dp,
                                bottom = bottomPadding,
                            ),
                    )
                }
            }

            DictionarySearchField(
                query = query,
                backdrop = contentBackdrop,
                blurEnabled = blurEnabled,
                onQueryChange = { onDictionaryIntent(DictionaryIntent.UpdateQuery(it)) },
                onSearch = { onDictionaryIntent(DictionaryIntent.ExecuteSearch) },
                onClear = { onDictionaryIntent(DictionaryIntent.ClearQuery) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .then(horizontalContentInset)
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = searchTopPadding,
                    ),
            )
        }

        dictionaryState.popupStack.forEachIndexed { index, lookup ->
            DictionaryLookupPopup(
                lookup = lookup,
                popupIndex = index,
                settings = settings,
                isDark = isDark,
                viewportWidth = maxWidth,
                viewportHeight = maxHeight,
                topInset = searchTopPadding +
                    DictionarySearchFieldHeight +
                    DictionarySearchFieldContentGap,
                bottomInset = bottomPadding + 8.dp,
                blurEnabled = blurEnabled,
                backdrop = contentBackdrop,
                onDictionaryIntent = onDictionaryIntent,
                onVerticalScrollActiveChange = onWebViewVerticalScrollActiveChange,
                onSwipeDismiss = { onDictionaryIntent(DictionaryIntent.DismissPopup(index)) },
                onDismiss = { onDictionaryIntent(DictionaryIntent.DismissPopup(index)) },
            )
        }
    }
}

@Composable
private fun rememberDictionaryContentBackdrop(blurEnabled: Boolean): LayerBackdrop? {
    if (!blurEnabled || !isRenderEffectSupported()) return null
    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}
