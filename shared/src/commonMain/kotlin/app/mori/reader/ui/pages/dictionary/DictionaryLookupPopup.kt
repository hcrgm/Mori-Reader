package app.mori.reader.ui.pages.dictionary

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mori.reader.data.anki.AnkiMiningContent
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.lookup.presentation.ReaderLookupState
import app.mori.reader.features.lookup.presentation.ReaderSelectionRect
import app.mori.reader.ui.pages.lookup.LookupPopupLayout
import app.mori.reader.ui.pages.lookup.LookupPopupSurface
import app.mori.reader.ui.pages.lookup.calculateLookupPopupLayout
import top.yukonga.miuix.kmp.blur.LayerBackdrop

internal enum class DictionaryFirstPopupPlacement {
    Selection,
    TopFullWidth,
}

@Composable
internal fun DictionaryLookupPopupStack(
    lookups: List<ReaderLookupState>,
    settings: AppSettings,
    ankiDuplicateExpression: String?,
    isDark: Boolean,
    materialEInkMode: Boolean,
    viewportWidth: Dp,
    viewportHeight: Dp,
    topInset: Dp,
    bottomInset: Dp,
    blurEnabled: Boolean,
    backdrop: LayerBackdrop?,
    onPopupTextSelected: (Int, String, ReaderSelectionRect?) -> Unit,
    onMineEntry: (ReaderLookupState, AnkiMiningContent) -> Unit,
    onCheckDuplicate: (String) -> Unit,
    onVerticalScrollActiveChange: (Boolean) -> Unit = {},
    onSwipeDismiss: (Int) -> Unit,
    onDismiss: (Int) -> Unit,
    onOutsideClick: (Int) -> Unit = onDismiss,
    firstPopupPlacement: DictionaryFirstPopupPlacement = DictionaryFirstPopupPlacement.Selection,
) {
    lookups.forEachIndexed { popupIndex, lookup ->
        if (!lookup.visible) return@forEachIndexed

        val layout =
            if (popupIndex == 0 && firstPopupPlacement == DictionaryFirstPopupPlacement.TopFullWidth) {
                LookupPopupLayout(
                    width = (viewportWidth - 12.dp).coerceAtLeast(1.dp),
                    height = settings.popup.height.dp,
                    left = 6.dp,
                    top = topInset + 6.dp,
                )
            } else {
                lookupPopupLayout(
                    lookup = lookup,
                    popupIndex = popupIndex,
                    settings = settings,
                    viewportWidth = viewportWidth,
                    viewportHeight = viewportHeight,
                    topInset = topInset,
                    bottomInset = bottomInset,
                    allowFirstPopupFullWidth =
                        firstPopupPlacement == DictionaryFirstPopupPlacement.Selection,
                )
            }

        LookupPopupSurface(
            lookup = lookup,
            layout = layout,
            settings = settings,
            ankiDuplicateExpression = ankiDuplicateExpression,
            isDark = isDark,
            materialEInkMode = materialEInkMode,
            blurEnabled = blurEnabled,
            backdrop = backdrop,
            zIndex = 20f + popupIndex,
            onDismiss = { onDismiss(popupIndex) },
            onSwipeDismiss = { onSwipeDismiss(popupIndex) },
            onVerticalScrollActiveChange = onVerticalScrollActiveChange,
            onOutsideClick = { onOutsideClick(popupIndex) },
            onPopupTextSelected = { text, rect ->
                onPopupTextSelected(popupIndex, text, rect)
            },
            onMineEntry = { content ->
                onMineEntry(lookup, content)
            },
            onCheckDuplicate = onCheckDuplicate,
        )
    }
}

private fun lookupPopupLayout(
    lookup: ReaderLookupState,
    popupIndex: Int,
    settings: AppSettings,
    viewportWidth: Dp,
    viewportHeight: Dp,
    topInset: Dp,
    bottomInset: Dp,
    allowFirstPopupFullWidth: Boolean,
): LookupPopupLayout =
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
            isFullWidth = allowFirstPopupFullWidth && popupIndex == 0 && settings.popup.fullWidth,
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
        isFullWidth = allowFirstPopupFullWidth && popupIndex == 0 && settings.popup.fullWidth,
        topInset = topInset,
        bottomInset = bottomInset,
    )
