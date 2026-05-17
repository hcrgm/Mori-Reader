package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mori.reader.data.anki.buildReaderAnkiMiningContext
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.anki.presentation.AnkiIntent
import app.mori.reader.features.lookup.presentation.ReaderLookupState
import app.mori.reader.features.reader.presentation.ReaderIntent
import app.mori.reader.features.reader.presentation.ReaderState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_pause
import app.mori.reader.shared.generated.resources.cd_play
import app.mori.reader.shared.generated.resources.sasayaki_continue_from_cue
import app.mori.reader.shared.generated.resources.sasayaki_replay
import app.mori.reader.ui.pages.lookup.LookupPopupSurface
import app.mori.reader.ui.pages.lookup.calculateLookupPopupLayout
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
    materialEInkMode: Boolean,
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
        lookup.rect?.let { rect ->
            val selectionTop = if (popupIndex == 0) readerTopPadding + rect.y.dp else rect.y.dp
            val selectionBottom = selectionTop + rect.height.dp
            calculateLookupPopupLayout(
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
        } ?: calculateLookupPopupLayout(
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

    ReaderSheetTheme(
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        LookupPopupSurface(
            lookup = lookup,
            layout = layout,
            settings = settings,
            ankiDuplicateExpression = ankiDuplicateExpression,
            isDark = isDark,
            materialEInkMode = materialEInkMode,
            blurEnabled = blurEnabled,
            backdrop = backdrop,
            zIndex = 20f,
            onDismiss = onDismiss,
            onSwipeDismiss = onSwipeDismiss,
            onPopupTextSelected = { text, rect ->
                onReaderIntent(
                    ReaderIntent.PopupTextSelected(
                        parentIndex = popupIndex,
                        text = text,
                        rect = rect,
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
        ) {
            lookup.sasayakiCueId?.let { cueId ->
                SasayakiPopupControls(
                    isPlaying = reader.sasayakiPlayer.isPlaying,
                    blurEnabled = blurEnabled,
                    backdrop = backdrop,
                    isDark = isDark,
                    materialEInkMode = materialEInkMode,
                    onReplay = { onReaderIntent(ReaderIntent.ReplayCue(cueId)) },
                    onToggle = { onReaderIntent(ReaderIntent.TogglePlayback) },
                    onContinue = { onReaderIntent(ReaderIntent.ContinueFromCue(cueId)) },
                )
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
    materialEInkMode: Boolean,
    onReplay: () -> Unit,
    onToggle: () -> Unit,
    onContinue: () -> Unit,
) {
    val topBarShape =
        if (materialEInkMode) {
            RoundedCornerShape(0.dp)
        } else {
            RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
        }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (blurEnabled && backdrop != null) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = topBarShape,
                            blurRadius = 25f,
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
                            if (materialEInkMode) {
                                MiuixTheme.colorScheme.surfaceContainerHighest
                            } else {
                                MiuixTheme.colorScheme.surfaceContainerHighest.copy(
                                    alpha = if (isDark) 0.78f else 0.92f,
                                )
                            },
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
            HorizontalDivider(
                thickness = if (materialEInkMode) 2.dp else 0.75.dp,
                color =
                    if (materialEInkMode) {
                        if (isDark) {
                            Color.White
                        } else {
                            Color.Black
                        }
                    } else {
                        MiuixTheme.colorScheme.dividerLine
                    },
            )
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
