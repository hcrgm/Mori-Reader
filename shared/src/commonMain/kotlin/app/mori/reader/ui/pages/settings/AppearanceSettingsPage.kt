package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.appearance_app_title
import app.mori.reader.shared.generated.resources.appearance_reader_title
import app.mori.reader.shared.generated.resources.cd_appearance
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.AppThemeSettingsCard
import app.mori.reader.ui.components.settings.ReaderAppearanceSettingsCard
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun AppearanceSettingsPage(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    MoriPageScaffold(
        title = stringResource(Res.string.cd_appearance),
        subtitle = "",
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
        actions = {},
    ) { paddingValues, scrollBehavior ->
        AppearanceSettingsContent(
            settings = settings,
            onSettingsIntent = onSettingsIntent,
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
        )
    }
}

@Composable
private fun AppearanceSettingsContent(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
    modifier: Modifier,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SmallTitle(text = stringResource(Res.string.appearance_app_title))
            AppThemeSettingsCard(
                themeMode = settings.appearance.themeMode,
                languageMode = settings.appearance.languageMode,
                blurEnabled = settings.appearance.blurEnabled,
                modifier = Modifier.padding(horizontal = 12.dp),
                onThemeModeSelected = { onSettingsIntent(SettingsIntent.SetThemeMode(it)) },
                onLanguageModeSelected = { onSettingsIntent(SettingsIntent.SetLanguageMode(it)) },
                onBlurEnabledChanged = { onSettingsIntent(SettingsIntent.SetBlurEnabled(it)) },
            )
        }

        item {
            SmallTitle(text = stringResource(Res.string.appearance_reader_title))
            ReaderAppearanceSettingsCard(
                readerThemeMode = settings.appearance.readerThemeMode,
                verticalWriting = settings.reader.verticalWriting,
                continuousMode = settings.reader.continuousMode,
                hideFurigana = settings.reader.hideFurigana,
                fullscreen = settings.appearance.readerFullscreen,
                fontSize = settings.reader.fontSize,
                lineHeight = settings.reader.lineHeight,
                horizontalPadding = settings.reader.horizontalPadding,
                verticalPadding = settings.reader.verticalPadding,
                avoidPageBreak = settings.reader.avoidPageBreak,
                justifyText = settings.reader.justifyText,
                characterSpacing = settings.reader.characterSpacing,
                popupWidth = settings.popup.width,
                popupHeight = settings.popup.height,
                popupFullWidth = settings.popup.fullWidth,
                popupSwipeToDismiss = settings.popup.swipeToDismiss,
                popupSwipeThreshold = settings.popup.swipeThreshold,
                modifier = Modifier.padding(horizontal = 12.dp),
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
                onFontSizeChanged = { onSettingsIntent(SettingsIntent.SetReaderFontSize(it)) },
                onLineHeightChanged = { onSettingsIntent(SettingsIntent.SetReaderLineHeight(it)) },
                onHorizontalPaddingChanged = {
                    onSettingsIntent(
                        SettingsIntent.SetReaderHorizontalPadding(
                            it,
                        ),
                    )
                },
                onVerticalPaddingChanged = {
                    onSettingsIntent(
                        SettingsIntent.SetReaderVerticalPadding(
                            it,
                        ),
                    )
                },
                onAvoidPageBreakChanged = {
                    onSettingsIntent(
                        SettingsIntent.SetReaderAvoidPageBreak(
                            it,
                        ),
                    )
                },
                onJustifyTextChanged = { onSettingsIntent(SettingsIntent.SetReaderJustifyText(it)) },
                onCharacterSpacingChanged = {
                    onSettingsIntent(
                        SettingsIntent.SetReaderCharacterSpacing(
                            it,
                        ),
                    )
                },
                onPopupWidthChanged = { onSettingsIntent(SettingsIntent.SetPopupWidth(it)) },
                onPopupHeightChanged = { onSettingsIntent(SettingsIntent.SetPopupHeight(it)) },
                onTogglePopupFullWidth = {
                    onSettingsIntent(SettingsIntent.SetPopupFullWidth(!settings.popup.fullWidth))
                },
                onTogglePopupSwipeToDismiss = {
                    onSettingsIntent(SettingsIntent.SetPopupSwipeToDismiss(!settings.popup.swipeToDismiss))
                },
                onPopupSwipeThresholdChanged = {
                    onSettingsIntent(
                        SettingsIntent.SetPopupSwipeThreshold(
                            it,
                        ),
                    )
                },
            )
        }
    }
}
