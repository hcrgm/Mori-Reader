package app.mori.reader.ui.pages.dictionary

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mori.reader.data.anki.AnkiMiningContext
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.anki.presentation.AnkiIntent
import app.mori.reader.features.dictionary.presentation.DictionaryIntent
import app.mori.reader.features.lookup.presentation.ReaderLookupState
import app.mori.reader.ui.pages.lookup.LookupPopupSurface
import app.mori.reader.ui.pages.lookup.calculateLookupPopupLayout
import top.yukonga.miuix.kmp.blur.LayerBackdrop

@Composable
internal fun DictionaryLookupPopup(
    lookup: ReaderLookupState,
    popupIndex: Int,
    settings: AppSettings,
    ankiDuplicateExpression: String?,
    isDark: Boolean,
    viewportWidth: Dp,
    viewportHeight: Dp,
    topInset: Dp,
    bottomInset: Dp,
    blurEnabled: Boolean,
    backdrop: LayerBackdrop?,
    onDictionaryIntent: (DictionaryIntent) -> Unit,
    onAnkiIntent: (AnkiIntent) -> Unit,
    onVerticalScrollActiveChange: (Boolean) -> Unit,
    onSwipeDismiss: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!lookup.visible) return

    val layout =
        lookup.rect?.let { rect ->
            calculateLookupPopupLayout(
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
        } ?: calculateLookupPopupLayout(
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

    LookupPopupSurface(
        lookup = lookup,
        layout = layout,
        settings = settings,
        ankiDuplicateExpression = ankiDuplicateExpression,
        isDark = isDark,
        blurEnabled = blurEnabled,
        backdrop = backdrop,
        zIndex = 20f + popupIndex,
        onDismiss = onDismiss,
        onSwipeDismiss = onSwipeDismiss,
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
        onMineEntry = { content ->
            onAnkiIntent(
                AnkiIntent.MineNote(
                    content = content,
                    context = AnkiMiningContext(sentence = lookup.selectedText),
                ),
            )
        },
        onCheckDuplicate = { expression ->
            onAnkiIntent(AnkiIntent.CheckDuplicate(expression))
        },
    )
}
