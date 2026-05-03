package app.mori.reader.ui.pages.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
import app.mori.reader.ui.ReaderLookupState
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme
import app.mori.reader.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val SearchFieldHeight = 56.dp
private val SearchFieldContentGap = 14.dp

@Composable
fun DictionaryPage(
    state: AppState,
    message: String?,
    fixedPadding: PaddingValues,
    onIntent: (AppIntent) -> Unit,
    onWebViewVerticalScrollActiveChange: (Boolean) -> Unit = {},
) {
    val query = state.dictionary.query
    val shouldComposeWebView =
        query.isNotBlank() || state.dictionary.isSearching || state.dictionary.hasSearched
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomPadding = maxOf(
        fixedPadding.calculateBottomPadding(),
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
    )
    val searchTopPadding = statusBarPadding + 12.dp
    val blurEnabled = state.settings.blurEnabled
    val contentBackdrop = rememberContentBackdrop(blurEnabled)
    val isDark = when (state.settings.themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    LaunchedEffect(shouldComposeWebView) {
        if (!shouldComposeWebView) {
            onWebViewVerticalScrollActiveChange(false)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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
                        query = query,
                        entries = state.dictionary.entries,
                        dictionaryStyles = state.dictionary.dictionaryStyles,
                        isSearching = state.dictionary.isSearching,
                        hasSearched = state.dictionary.hasSearched,
                        errorMessage = state.dictionary.errorMessage,
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
                        contentTopPadding = searchTopPadding + SearchFieldHeight + SearchFieldContentGap,
                        onVerticalScrollActiveChange = onWebViewVerticalScrollActiveChange,
                        onPopupTextSelected = { text, rect ->
                            onIntent(
                                AppIntent.DictionaryPopupTextSelected(
                                    parentIndex = null,
                                    text = text,
                                    rect = rect
                                )
                            )
                        },
                        onAddAnkiCard = { onIntent(AppIntent.AddAnkiCard(it)) },
                        enableInternalPopup = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 12.dp,
                                end = 12.dp,
                            )
                            // This 0.1dp rounded corner clip is a hack for flickering.
                            // When blur effect is enabled, the WebView will keep flickering without the rounded corner.
                            // I don't know why but this magically fix it. DO NOT REMOVE IT!!!
                            .clip(RoundedCornerShape(0.1.dp)),
                        contentBottomPadding = bottomPadding,
                    )
                } else {
                    DictionaryPlaceholder(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = searchTopPadding + SearchFieldHeight + SearchFieldContentGap + 36.dp,
                                bottom = bottomPadding,
                            ),
                    )
                }
            }

            SearchField(
                query = query,
                backdrop = contentBackdrop,
                blurEnabled = blurEnabled,
                onQueryChange = { onIntent(AppIntent.UpdateDictionaryQuery(it)) },
                onSearch = { onIntent(AppIntent.ExecuteDictionarySearch) },
                onClear = { onIntent(AppIntent.ClearDictionaryQuery) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = searchTopPadding,
                    ),
            )

            if (message != null) {
                EffectBanner(
                    message = message,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = searchTopPadding + SearchFieldHeight + 12.dp),
                )
            }
        }

        state.dictionary.popupStack.forEachIndexed { index, lookup ->
            DictionaryLookupPopup(
                lookup = lookup,
                popupIndex = index,
                state = state,
                isDark = isDark,
                viewportWidth = maxWidth,
                viewportHeight = maxHeight,
                topInset = searchTopPadding + SearchFieldHeight + SearchFieldContentGap,
                bottomInset = bottomPadding + 8.dp,
                blurEnabled = blurEnabled,
                backdrop = contentBackdrop,
                onIntent = onIntent,
                onVerticalScrollActiveChange = onWebViewVerticalScrollActiveChange,
                onSwipeDismiss = { onIntent(AppIntent.DismissDictionaryPopup(index)) },
                onDismiss = { onIntent(AppIntent.DismissDictionaryPopup(index)) },
            )
        }
    }
}

@Composable
private fun DictionaryLookupPopup(
    lookup: ReaderLookupState,
    popupIndex: Int,
    state: AppState,
    isDark: Boolean,
    viewportWidth: Dp,
    viewportHeight: Dp,
    topInset: Dp,
    bottomInset: Dp,
    blurEnabled: Boolean,
    backdrop: LayerBackdrop?,
    onIntent: (AppIntent) -> Unit,
    onVerticalScrollActiveChange: (Boolean) -> Unit,
    onSwipeDismiss: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!lookup.visible) return

    val layout = remember(
        lookup.rect,
        viewportWidth,
        viewportHeight,
        topInset,
        bottomInset,
        state.settings.popupWidth,
        state.settings.popupHeight,
        state.settings.popupFullWidth,
    ) {
        lookup.rect?.let { rect ->
            PopupLayout(
                selectionLeft = rect.x.dp,
                selectionTop = rect.y.dp,
                selectionRight = rect.x.dp + rect.width.dp,
                selectionBottom = rect.y.dp + rect.height.dp,
                screenWidth = viewportWidth,
                screenHeight = viewportHeight,
                maxWidth = state.settings.popupWidth.dp,
                maxHeight = state.settings.popupHeight.dp,
                isVertical = false,
                isFullWidth = popupIndex == 0 && state.settings.popupFullWidth,
                topInset = topInset,
                bottomInset = bottomInset,
            )
        } ?: PopupLayout(
            selectionLeft = viewportWidth / 2f,
            selectionTop = viewportHeight - bottomInset - 1.dp,
            selectionRight = viewportWidth / 2f,
            selectionBottom = viewportHeight - bottomInset,
            screenWidth = viewportWidth,
            screenHeight = viewportHeight,
            maxWidth = state.settings.popupWidth.dp,
            maxHeight = state.settings.popupHeight.dp,
            isVertical = false,
            isFullWidth = popupIndex == 0 && state.settings.popupFullWidth,
            topInset = topInset,
            bottomInset = bottomInset,
        )
    }

    val outsideInteractionSource = remember { MutableInteractionSource() }
    val popupInteractionSource = remember { MutableInteractionSource() }
    val popupShape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(20f + popupIndex)
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
                enableInternalPopup = false,
                swipeDismissThreshold = if (state.settings.popupSwipeToDismiss) {
                    state.settings.popupSwipeThreshold
                } else {
                    0
                },
                edgeToEdgeContent = true,
                transparentBackground = blurEnabled,
                onVerticalScrollActiveChange = onVerticalScrollActiveChange,
                onPopupTextSelected = { text, rect ->
                    onIntent(
                        AppIntent.DictionaryPopupTextSelected(
                            parentIndex = popupIndex,
                            text = text,
                            rect = rect
                        )
                    )
                },
                onSwipeDismiss = onSwipeDismiss,
                onAddAnkiCard = { onIntent(AppIntent.AddAnkiCard(it)) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun DictionaryPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(Res.string.dict_placeholder),
            style = MiuixTheme.textStyles.title3,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun rememberContentBackdrop(blurEnabled: Boolean): LayerBackdrop? {
    if (!blurEnabled || !isRenderEffectSupported()) return null
    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

@Composable
private fun SearchField(
    query: String,
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SearchFieldHeight)
            .then(
                if (blurEnabled && backdrop != null) {
                    Modifier.textureBlur(
                        backdrop = backdrop,
                        shape = RoundedCornerShape(22.dp),
                        blurRadius = 25f * density.density,
                        colors = BlurColors(
                            blendColors = listOf(
                                BlendColorEntry(
                                    color = MiuixTheme.colorScheme.surfaceContainer.copy(
                                        0.8f
                                    )
                                ),
                            ),
                        ),
                    )
                } else {
                    Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f))
                }
            )
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = MiuixIcons.Search,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus(force = true)
                    onSearch()
                },
            ),
            textStyle = MiuixTheme.textStyles.main.copy(
                color = MiuixTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.dict_search_hint),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                innerTextField()
            },
        )

        if (query.isNotEmpty()) {
            IconButton(
                modifier = Modifier.padding(end = 2.dp),
                onClick = onClear,
            ) {
                Icon(
                    imageVector = MiuixIcons.Clear,
                    contentDescription = stringResource(Res.string.cd_clear_search),
                )
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
        }
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

@Composable
private fun EffectBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
        ),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontWeight = FontWeight.Medium,
        )
    }
}
