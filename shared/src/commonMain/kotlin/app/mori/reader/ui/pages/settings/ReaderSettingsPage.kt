package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.reader_settings_display_title
import app.mori.reader.shared.generated.resources.reader_settings_popup_title
import app.mori.reader.shared.generated.resources.reader_settings_typography_title
import app.mori.reader.shared.generated.resources.settings_reader_title
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MoriSettingsSection
import app.mori.reader.ui.components.settings.ReaderDisplaySettingsGroup
import app.mori.reader.ui.components.settings.ReaderPopupSettingsGroup
import app.mori.reader.ui.components.settings.ReaderTypographySettingsGroup
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun ReaderSettingsPage(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    MoriPageScaffold(
        title = stringResource(Res.string.settings_reader_title),
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
    ) { paddingValues, scrollBehavior ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                MoriSettingsSection(title = stringResource(Res.string.reader_settings_display_title)) {
                    ReaderDisplaySettingsGroup(
                        readerThemeMode = settings.appearance.readerThemeMode,
                        verticalWriting = settings.reader.verticalWriting,
                        continuousMode = settings.reader.continuousMode,
                        hideFurigana = settings.reader.hideFurigana,
                        fullscreen = settings.appearance.readerFullscreen,
                        avoidPageBreak = settings.reader.avoidPageBreak,
                        justifyText = settings.reader.justifyText,
                        onReaderThemeModeSelected = { onSettingsIntent(SettingsIntent.SetReaderThemeMode(it)) },
                        onToggleWritingMode = {
                            onSettingsIntent(SettingsIntent.SetReaderVerticalWriting(!settings.reader.verticalWriting))
                        },
                        onToggleContinuousMode = {
                            onSettingsIntent(SettingsIntent.SetReaderContinuousMode(!settings.reader.continuousMode))
                        },
                        onToggleHideFurigana = {
                            onSettingsIntent(SettingsIntent.SetReaderHideFurigana(!settings.reader.hideFurigana))
                        },
                        onFullscreenChanged = { onSettingsIntent(SettingsIntent.SetReaderFullscreen(it)) },
                        onAvoidPageBreakChanged = {
                            onSettingsIntent(SettingsIntent.SetReaderAvoidPageBreak(it))
                        },
                        onJustifyTextChanged = {
                            onSettingsIntent(SettingsIntent.SetReaderJustifyText(it))
                        },
                    )
                }
            }

            item {
                MoriSettingsSection(title = stringResource(Res.string.reader_settings_typography_title)) {
                    ReaderTypographySettingsGroup(
                        fontSize = settings.reader.fontSize,
                        lineHeight = settings.reader.lineHeight,
                        horizontalPadding = settings.reader.horizontalPadding,
                        verticalPadding = settings.reader.verticalPadding,
                        characterSpacing = settings.reader.characterSpacing,
                        onFontSizeChanged = { onSettingsIntent(SettingsIntent.SetReaderFontSize(it)) },
                        onLineHeightChanged = { onSettingsIntent(SettingsIntent.SetReaderLineHeight(it)) },
                        onHorizontalPaddingChanged = {
                            onSettingsIntent(SettingsIntent.SetReaderHorizontalPadding(it))
                        },
                        onVerticalPaddingChanged = {
                            onSettingsIntent(SettingsIntent.SetReaderVerticalPadding(it))
                        },
                        onCharacterSpacingChanged = {
                            onSettingsIntent(SettingsIntent.SetReaderCharacterSpacing(it))
                        },
                    )
                }
            }

            item {
                MoriSettingsSection(title = stringResource(Res.string.reader_settings_popup_title)) {
                    ReaderPopupSettingsGroup(
                        popupWidth = settings.popup.width,
                        popupHeight = settings.popup.height,
                        popupFullWidth = settings.popup.fullWidth,
                        popupSwipeToDismiss = settings.popup.swipeToDismiss,
                        popupSwipeThreshold = settings.popup.swipeThreshold,
                        onPopupWidthChanged = { onSettingsIntent(SettingsIntent.SetPopupWidth(it)) },
                        onPopupHeightChanged = { onSettingsIntent(SettingsIntent.SetPopupHeight(it)) },
                        onTogglePopupFullWidth = {
                            onSettingsIntent(SettingsIntent.SetPopupFullWidth(!settings.popup.fullWidth))
                        },
                        onTogglePopupSwipeToDismiss = {
                            onSettingsIntent(SettingsIntent.SetPopupSwipeToDismiss(!settings.popup.swipeToDismiss))
                        },
                        onPopupSwipeThresholdChanged = {
                            onSettingsIntent(SettingsIntent.SetPopupSwipeThreshold(it))
                        },
                    )
                }
            }
        }
    }
}
