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
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.appearance_app_title
import app.mori.reader.shared.generated.resources.appearance_reader_title
import app.mori.reader.shared.generated.resources.cd_appearance
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
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
    state: AppState,
    message: String?,
    onIntent: (AppIntent) -> Unit,
    onBack: () -> Unit,
) {
    MoriPageScaffold(
        title = stringResource(Res.string.cd_appearance),
        subtitle = "",
        blurEnabled = state.settings.blurEnabled,
        message = message,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
        actions = {},
    ) { paddingValues, scrollBehavior ->
        AppearanceSettingsContent(
            state = state,
            onIntent = onIntent,
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
            ),
        )
    }
}

@Composable
private fun AppearanceSettingsContent(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
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
                themeMode = state.settings.themeMode,
                languageMode = state.settings.languageMode,
                blurEnabled = state.settings.blurEnabled,
                modifier = Modifier.padding(horizontal = 12.dp),
                onThemeModeSelected = { onIntent(AppIntent.SetThemeMode(it)) },
                onLanguageModeSelected = { onIntent(AppIntent.SetLanguageMode(it)) },
                onBlurEnabledChanged = { onIntent(AppIntent.SetBlurEnabled(it)) },
            )
        }

        item {
            SmallTitle(text = stringResource(Res.string.appearance_reader_title))
            ReaderAppearanceSettingsCard(
                readerThemeMode = state.settings.readerThemeMode,
                verticalWriting = state.reader.verticalWriting,
                continuousMode = state.settings.readerContinuousMode,
                hideFurigana = state.settings.readerHideFurigana,
                fullscreen = state.settings.readerFullscreen,
                fontSize = state.settings.readerFontSize,
                lineHeight = state.settings.readerLineHeight,
                horizontalPadding = state.settings.readerHorizontalPadding,
                verticalPadding = state.settings.readerVerticalPadding,
                avoidPageBreak = state.settings.readerAvoidPageBreak,
                justifyText = state.settings.readerJustifyText,
                characterSpacing = state.settings.readerCharacterSpacing,
                popupWidth = state.settings.popupWidth,
                popupHeight = state.settings.popupHeight,
                popupFullWidth = state.settings.popupFullWidth,
                popupSwipeToDismiss = state.settings.popupSwipeToDismiss,
                popupSwipeThreshold = state.settings.popupSwipeThreshold,
                modifier = Modifier.padding(horizontal = 12.dp),
                onReaderThemeModeSelected = { onIntent(AppIntent.SetReaderThemeMode(it)) },
                onToggleWritingMode = { onIntent(AppIntent.ToggleReaderWritingMode) },
                onToggleContinuousMode = { onIntent(AppIntent.ToggleReaderContinuousMode) },
                onToggleHideFurigana = { onIntent(AppIntent.ToggleReaderHideFurigana) },
                onFullscreenChanged = { onIntent(AppIntent.SetReaderFullscreen(it)) },
                onFontSizeChanged = { onIntent(AppIntent.SetReaderFontSize(it)) },
                onLineHeightChanged = { onIntent(AppIntent.SetReaderLineHeight(it)) },
                onHorizontalPaddingChanged = { onIntent(AppIntent.SetReaderHorizontalPadding(it)) },
                onVerticalPaddingChanged = { onIntent(AppIntent.SetReaderVerticalPadding(it)) },
                onAvoidPageBreakChanged = { onIntent(AppIntent.SetReaderAvoidPageBreak(it)) },
                onJustifyTextChanged = { onIntent(AppIntent.SetReaderJustifyText(it)) },
                onCharacterSpacingChanged = { onIntent(AppIntent.SetReaderCharacterSpacing(it)) },
                onPopupWidthChanged = { onIntent(AppIntent.SetPopupWidth(it)) },
                onPopupHeightChanged = { onIntent(AppIntent.SetPopupHeight(it)) },
                onTogglePopupFullWidth = { onIntent(AppIntent.TogglePopupFullWidth) },
                onTogglePopupSwipeToDismiss = { onIntent(AppIntent.TogglePopupSwipeToDismiss) },
                onPopupSwipeThresholdChanged = { onIntent(AppIntent.SetPopupSwipeThreshold(it)) },
            )
        }
    }
}
