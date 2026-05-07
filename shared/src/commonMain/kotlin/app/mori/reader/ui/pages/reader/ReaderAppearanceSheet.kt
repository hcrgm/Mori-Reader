package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.ReaderThemeMode
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_appearance
import app.mori.reader.ui.components.settings.ReaderAppearanceSettingsCard
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@Composable
internal fun ReaderAppearanceSheet(
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
