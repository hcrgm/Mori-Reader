package app.mori.reader.ui.components.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.LanguageMode
import app.mori.reader.data.settings.ReaderThemeMode
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.appearance_avoid_page_break_off
import app.mori.reader.shared.generated.resources.appearance_avoid_page_break_on
import app.mori.reader.shared.generated.resources.appearance_avoid_page_break_title
import app.mori.reader.shared.generated.resources.appearance_blur_summary
import app.mori.reader.shared.generated.resources.appearance_blur_title
import app.mori.reader.shared.generated.resources.appearance_character_spacing
import app.mori.reader.shared.generated.resources.appearance_font_size
import app.mori.reader.shared.generated.resources.appearance_fullscreen_title
import app.mori.reader.shared.generated.resources.appearance_hide_furigana_title
import app.mori.reader.shared.generated.resources.appearance_horizontal_margin
import app.mori.reader.shared.generated.resources.appearance_justify_title
import app.mori.reader.shared.generated.resources.appearance_language_summary
import app.mori.reader.shared.generated.resources.appearance_language_title
import app.mori.reader.shared.generated.resources.appearance_line_height
import app.mori.reader.shared.generated.resources.appearance_monet_key_color_title
import app.mori.reader.shared.generated.resources.appearance_monet_summary
import app.mori.reader.shared.generated.resources.appearance_monet_title
import app.mori.reader.shared.generated.resources.appearance_page_mode_summary
import app.mori.reader.shared.generated.resources.appearance_page_mode_title
import app.mori.reader.shared.generated.resources.appearance_popup_full_width_off
import app.mori.reader.shared.generated.resources.appearance_popup_full_width_on
import app.mori.reader.shared.generated.resources.appearance_popup_full_width_title
import app.mori.reader.shared.generated.resources.appearance_popup_height
import app.mori.reader.shared.generated.resources.appearance_popup_swipe_dismiss_off
import app.mori.reader.shared.generated.resources.appearance_popup_swipe_dismiss_on
import app.mori.reader.shared.generated.resources.appearance_popup_swipe_dismiss_title
import app.mori.reader.shared.generated.resources.appearance_popup_swipe_threshold
import app.mori.reader.shared.generated.resources.appearance_popup_title
import app.mori.reader.shared.generated.resources.appearance_popup_width
import app.mori.reader.shared.generated.resources.appearance_reader_theme_title
import app.mori.reader.shared.generated.resources.appearance_reading_continuous
import app.mori.reader.shared.generated.resources.appearance_reading_pagination
import app.mori.reader.shared.generated.resources.appearance_theme_summary
import app.mori.reader.shared.generated.resources.appearance_theme_title
import app.mori.reader.shared.generated.resources.appearance_typography
import app.mori.reader.shared.generated.resources.appearance_vertical_margin
import app.mori.reader.shared.generated.resources.appearance_writing_direction_summary
import app.mori.reader.shared.generated.resources.appearance_writing_direction_title
import app.mori.reader.shared.generated.resources.appearance_writing_horizontal
import app.mori.reader.shared.generated.resources.appearance_writing_vertical
import app.mori.reader.shared.generated.resources.color_amber
import app.mori.reader.shared.generated.resources.color_blue
import app.mori.reader.shared.generated.resources.color_blue_grey
import app.mori.reader.shared.generated.resources.color_brown
import app.mori.reader.shared.generated.resources.color_cyan
import app.mori.reader.shared.generated.resources.color_deep_purple
import app.mori.reader.shared.generated.resources.color_green
import app.mori.reader.shared.generated.resources.color_indigo
import app.mori.reader.shared.generated.resources.color_orange
import app.mori.reader.shared.generated.resources.color_pink
import app.mori.reader.shared.generated.resources.color_purple
import app.mori.reader.shared.generated.resources.color_red
import app.mori.reader.shared.generated.resources.color_sakura
import app.mori.reader.shared.generated.resources.color_teal
import app.mori.reader.shared.generated.resources.color_yellow
import app.mori.reader.shared.generated.resources.language_chinese
import app.mori.reader.shared.generated.resources.language_english
import app.mori.reader.shared.generated.resources.language_follow_system
import app.mori.reader.shared.generated.resources.monet_key_color_default
import app.mori.reader.shared.generated.resources.reader_theme_follow_app
import app.mori.reader.shared.generated.resources.theme_dark
import app.mori.reader.shared.generated.resources.theme_follow_system
import app.mori.reader.shared.generated.resources.theme_light
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults.SliderHapticEffect
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

@Composable
fun AppThemeSettingsCard(
    themeMode: ThemeMode,
    languageMode: LanguageMode,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    blurEnabled: Boolean,
    modifier: Modifier = Modifier,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLanguageModeSelected: (LanguageMode) -> Unit,
    onMonetEnabledChanged: (Boolean) -> Unit,
    onMonetKeyColorSelected: (Long) -> Unit,
    onBlurEnabledChanged: (Boolean) -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        AppThemeSettingsGroup(
            themeMode = themeMode,
            languageMode = languageMode,
            monetEnabled = monetEnabled,
            monetKeyColor = monetKeyColor,
            blurEnabled = blurEnabled,
            onThemeModeSelected = onThemeModeSelected,
            onLanguageModeSelected = onLanguageModeSelected,
            onMonetEnabledChanged = onMonetEnabledChanged,
            onMonetKeyColorSelected = onMonetKeyColorSelected,
            onBlurEnabledChanged = onBlurEnabledChanged,
        )
    }
}

@Composable
internal fun AppThemeSettingsGroup(
    themeMode: ThemeMode,
    languageMode: LanguageMode,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    blurEnabled: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLanguageModeSelected: (LanguageMode) -> Unit,
    onMonetEnabledChanged: (Boolean) -> Unit,
    onMonetKeyColorSelected: (Long) -> Unit,
    onBlurEnabledChanged: (Boolean) -> Unit,
) {
    val themeModes = remember { ThemeMode.entries.toList() }
    val themeModeItems = themeModes.map { SpinnerEntry(title = it.localizedLabel()) }
    val languageModes = remember { LanguageMode.entries.toList() }
    val keyColorValues = remember { listOf(0L) + monetKeyColorOptions }
    val followSystemLabel = stringResource(Res.string.language_follow_system)
    val englishLabel = stringResource(Res.string.language_english)
    val chineseLabel = stringResource(Res.string.language_chinese)
    val languageModeItems =
        remember(languageModes, followSystemLabel, englishLabel, chineseLabel) {
            languageModes.map { mode ->
                SpinnerEntry(
                    title =
                        when (mode) {
                            LanguageMode.System -> followSystemLabel
                            LanguageMode.English -> englishLabel
                            LanguageMode.Chinese -> chineseLabel
                        },
                )
            }
        }

    WindowSpinnerPreference(
        items = themeModeItems,
        selectedIndex = themeModes.indexOf(themeMode).coerceAtLeast(0),
        title = stringResource(Res.string.appearance_theme_title),
        summary = stringResource(Res.string.appearance_theme_summary),
        onSelectedIndexChange = { index ->
            onThemeModeSelected(themeModes[index])
        },
    )
    WindowSpinnerPreference(
        items = languageModeItems,
        selectedIndex = languageModes.indexOf(languageMode).coerceAtLeast(0),
        title = stringResource(Res.string.appearance_language_title),
        summary = stringResource(Res.string.appearance_language_summary),
        onSelectedIndexChange = { index ->
            onLanguageModeSelected(languageModes[index])
        },
    )
    SwitchPreference(
        checked = monetEnabled,
        onCheckedChange = onMonetEnabledChanged,
        title = stringResource(Res.string.appearance_monet_title),
        summary = stringResource(Res.string.appearance_monet_summary),
    )
    AnimatedVisibility(
        visible = monetEnabled,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        WindowSpinnerPreference(
            items = monetKeyColorItems(),
            selectedIndex = keyColorValues.indexOf(monetKeyColor).coerceAtLeast(0),
            title = stringResource(Res.string.appearance_monet_key_color_title),
            onSelectedIndexChange = { index ->
                onMonetKeyColorSelected(keyColorValues[index])
            },
        )
    }
    if (isRenderEffectSupported()) {
        SwitchPreference(
            checked = blurEnabled,
            onCheckedChange = onBlurEnabledChanged,
            title = stringResource(Res.string.appearance_blur_title),
            summary = stringResource(Res.string.appearance_blur_summary),
        )
    }
}

@Composable
private fun monetKeyColorItems(): List<SpinnerEntry> =
    listOf(
        SpinnerEntry(title = stringResource(Res.string.monet_key_color_default)),
        SpinnerEntry(title = stringResource(Res.string.color_red)),
        SpinnerEntry(title = stringResource(Res.string.color_pink)),
        SpinnerEntry(title = stringResource(Res.string.color_purple)),
        SpinnerEntry(title = stringResource(Res.string.color_deep_purple)),
        SpinnerEntry(title = stringResource(Res.string.color_indigo)),
        SpinnerEntry(title = stringResource(Res.string.color_blue)),
        SpinnerEntry(title = stringResource(Res.string.color_cyan)),
        SpinnerEntry(title = stringResource(Res.string.color_teal)),
        SpinnerEntry(title = stringResource(Res.string.color_green)),
        SpinnerEntry(title = stringResource(Res.string.color_yellow)),
        SpinnerEntry(title = stringResource(Res.string.color_amber)),
        SpinnerEntry(title = stringResource(Res.string.color_orange)),
        SpinnerEntry(title = stringResource(Res.string.color_brown)),
        SpinnerEntry(title = stringResource(Res.string.color_blue_grey)),
        SpinnerEntry(title = stringResource(Res.string.color_sakura)),
    )

@Composable
fun ReaderAppearanceSettingsCard(
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
    modifier: Modifier = Modifier,
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
    Card(modifier = modifier.fillMaxWidth()) {
        ReaderAppearanceSettingsGroup(
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
}

@Composable
internal fun ReaderAppearanceSettingsGroup(
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
    ReaderDisplaySettingsGroup(
        readerThemeMode = readerThemeMode,
        verticalWriting = verticalWriting,
        continuousMode = continuousMode,
        hideFurigana = hideFurigana,
        fullscreen = fullscreen,
        avoidPageBreak = avoidPageBreak,
        justifyText = justifyText,
        onReaderThemeModeSelected = onReaderThemeModeSelected,
        onToggleWritingMode = onToggleWritingMode,
        onToggleContinuousMode = onToggleContinuousMode,
        onToggleHideFurigana = onToggleHideFurigana,
        onFullscreenChanged = onFullscreenChanged,
        onAvoidPageBreakChanged = onAvoidPageBreakChanged,
        onJustifyTextChanged = onJustifyTextChanged,
    )
    HorizontalDivider(modifier = Modifier.padding(start = 20.dp))
    SmallTitle(
        text = stringResource(Res.string.appearance_typography),
        insideMargin = PaddingValues(14.dp),
    )
    ReaderTypographySettingsGroup(
        fontSize = fontSize,
        lineHeight = lineHeight,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        characterSpacing = characterSpacing,
        onFontSizeChanged = onFontSizeChanged,
        onLineHeightChanged = onLineHeightChanged,
        onHorizontalPaddingChanged = onHorizontalPaddingChanged,
        onVerticalPaddingChanged = onVerticalPaddingChanged,
        onCharacterSpacingChanged = onCharacterSpacingChanged,
    )
    HorizontalDivider(modifier = Modifier.padding(start = 20.dp))
    SmallTitle(
        text = stringResource(Res.string.appearance_popup_title),
        insideMargin = PaddingValues(14.dp),
    )
    ReaderPopupSettingsGroup(
        popupWidth = popupWidth,
        popupHeight = popupHeight,
        popupFullWidth = popupFullWidth,
        popupSwipeToDismiss = popupSwipeToDismiss,
        popupSwipeThreshold = popupSwipeThreshold,
        onPopupWidthChanged = onPopupWidthChanged,
        onPopupHeightChanged = onPopupHeightChanged,
        onTogglePopupFullWidth = onTogglePopupFullWidth,
        onTogglePopupSwipeToDismiss = onTogglePopupSwipeToDismiss,
        onPopupSwipeThresholdChanged = onPopupSwipeThresholdChanged,
    )
}

@Composable
internal fun ReaderDisplaySettingsGroup(
    readerThemeMode: ReaderThemeMode,
    verticalWriting: Boolean,
    continuousMode: Boolean,
    hideFurigana: Boolean,
    fullscreen: Boolean,
    avoidPageBreak: Boolean,
    justifyText: Boolean,
    onReaderThemeModeSelected: (ReaderThemeMode) -> Unit,
    onToggleWritingMode: () -> Unit,
    onToggleContinuousMode: () -> Unit,
    onToggleHideFurigana: () -> Unit,
    onFullscreenChanged: (Boolean) -> Unit,
    onAvoidPageBreakChanged: (Boolean) -> Unit,
    onJustifyTextChanged: (Boolean) -> Unit,
) {
    val readerThemeModes = remember { ReaderThemeMode.entries.toList() }
    val readerThemeModeItems = readerThemeModes.map { SpinnerEntry(title = it.localizedLabel()) }
    val verticalWritingLabel = stringResource(Res.string.appearance_writing_vertical)
    val horizontalWritingLabel = stringResource(Res.string.appearance_writing_horizontal)
    val paginationLabel = stringResource(Res.string.appearance_reading_pagination)
    val continuousLabel = stringResource(Res.string.appearance_reading_continuous)
    val writingModeItems =
        remember(verticalWritingLabel, horizontalWritingLabel) {
            listOf(
                SpinnerEntry(title = verticalWritingLabel),
                SpinnerEntry(title = horizontalWritingLabel),
            )
        }
    val readingModeItems =
        remember(paginationLabel, continuousLabel) {
            listOf(
                SpinnerEntry(title = paginationLabel),
                SpinnerEntry(title = continuousLabel),
            )
        }

    WindowSpinnerPreference(
        items = readerThemeModeItems,
        selectedIndex = readerThemeModes.indexOf(readerThemeMode).coerceAtLeast(0),
        title = stringResource(Res.string.appearance_reader_theme_title),
        onSelectedIndexChange = { index ->
            onReaderThemeModeSelected(readerThemeModes[index])
        },
    )
    WindowSpinnerPreference(
        items = writingModeItems,
        selectedIndex = if (verticalWriting) 0 else 1,
        title = stringResource(Res.string.appearance_writing_direction_title),
        summary = stringResource(Res.string.appearance_writing_direction_summary),
        onSelectedIndexChange = { index ->
            val wantsVertical = index == 0
            if (wantsVertical != verticalWriting) onToggleWritingMode()
        },
    )
    WindowSpinnerPreference(
        items = readingModeItems,
        selectedIndex = if (continuousMode) 1 else 0,
        title = stringResource(Res.string.appearance_page_mode_title),
        summary = stringResource(Res.string.appearance_page_mode_summary),
        onSelectedIndexChange = { index ->
            val wantsContinuous = index == 1
            if (wantsContinuous != continuousMode) onToggleContinuousMode()
        },
    )
    SwitchPreference(
        checked = hideFurigana,
        onCheckedChange = { onToggleHideFurigana() },
        title = stringResource(Res.string.appearance_hide_furigana_title),
    )
    SwitchPreference(
        checked = fullscreen,
        onCheckedChange = onFullscreenChanged,
        title = stringResource(Res.string.appearance_fullscreen_title),
    )
    SwitchPreference(
        checked = avoidPageBreak,
        onCheckedChange = onAvoidPageBreakChanged,
        title = stringResource(Res.string.appearance_avoid_page_break_title),
        summary =
            if (avoidPageBreak) {
                stringResource(Res.string.appearance_avoid_page_break_on)
            } else {
                stringResource(
                    Res.string.appearance_avoid_page_break_off,
                )
            },
    )
    SwitchPreference(
        checked = justifyText,
        onCheckedChange = onJustifyTextChanged,
        title = stringResource(Res.string.appearance_justify_title),
    )
}

@Composable
internal fun ReaderTypographySettingsGroup(
    fontSize: Int,
    lineHeight: Double,
    horizontalPadding: Int,
    verticalPadding: Int,
    characterSpacing: Double,
    onFontSizeChanged: (Int) -> Unit,
    onLineHeightChanged: (Double) -> Unit,
    onHorizontalPaddingChanged: (Int) -> Unit,
    onVerticalPaddingChanged: (Int) -> Unit,
    onCharacterSpacingChanged: (Double) -> Unit,
) {
    Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement =
            androidx.compose.foundation.layout.Arrangement
                .spacedBy(12.dp),
    ) {
        SettingSlider(
            label = stringResource(Res.string.appearance_font_size),
            value = fontSize.toFloat(),
            range = 16f..40f,
            steps = 23,
            valueText = { it.roundToInt().toString() },
            onCommit = { onFontSizeChanged(it.roundToInt()) },
        )
        SettingSlider(
            label = stringResource(Res.string.appearance_horizontal_margin),
            value = horizontalPadding.toFloat(),
            range = 0f..50f,
            steps = 49,
            valueText = { "${it.roundToInt()}%" },
            onCommit = { onHorizontalPaddingChanged(it.roundToInt()) },
        )
        SettingSlider(
            label = stringResource(Res.string.appearance_vertical_margin),
            value = verticalPadding.toFloat(),
            range = 0f..50f,
            steps = 49,
            valueText = { "${it.roundToInt()}%" },
            onCommit = { onVerticalPaddingChanged(it.roundToInt()) },
        )
        SettingSlider(
            label = stringResource(Res.string.appearance_line_height),
            value = lineHeight.toFloat(),
            range = 1.0f..2.5f,
            steps = 29,
            valueText = { ((it * 20f).roundToInt() / 20.0).formatTwoDecimals() },
            onCommit = { onLineHeightChanged((it * 20f).roundToInt() / 20.0) },
        )
        SettingSlider(
            label = stringResource(Res.string.appearance_character_spacing),
            value = characterSpacing.toFloat(),
            range = -10f..10f,
            steps = 19,
            valueText = { "${it.roundToInt()}%" },
            onCommit = { onCharacterSpacingChanged(it.roundToInt().toDouble()) },
        )
    }
}

@Composable
internal fun ReaderPopupSettingsGroup(
    popupWidth: Int,
    popupHeight: Int,
    popupFullWidth: Boolean,
    popupSwipeToDismiss: Boolean,
    popupSwipeThreshold: Int,
    onPopupWidthChanged: (Int) -> Unit,
    onPopupHeightChanged: (Int) -> Unit,
    onTogglePopupFullWidth: () -> Unit,
    onTogglePopupSwipeToDismiss: () -> Unit,
    onPopupSwipeThresholdChanged: (Int) -> Unit,
) {
    SwitchPreference(
        checked = popupFullWidth,
        onCheckedChange = { onTogglePopupFullWidth() },
        title = stringResource(Res.string.appearance_popup_full_width_title),
        summary =
            if (popupFullWidth) {
                stringResource(Res.string.appearance_popup_full_width_on)
            } else {
                stringResource(
                    Res.string.appearance_popup_full_width_off,
                )
            },
    )
    SwitchPreference(
        checked = popupSwipeToDismiss,
        onCheckedChange = { onTogglePopupSwipeToDismiss() },
        title = stringResource(Res.string.appearance_popup_swipe_dismiss_title),
        summary =
            if (popupSwipeToDismiss) {
                stringResource(Res.string.appearance_popup_swipe_dismiss_on)
            } else {
                stringResource(
                    Res.string.appearance_popup_swipe_dismiss_off,
                )
            },
    )
    Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingSlider(
            label = stringResource(Res.string.appearance_popup_width),
            value = popupWidth.toFloat(),
            range = 100f..700f,
            steps = 59,
            valueText = { ((it / 10f).roundToInt() * 10).toString() },
            onCommit = { onPopupWidthChanged((it / 10f).roundToInt() * 10) },
        )
        SettingSlider(
            label = stringResource(Res.string.appearance_popup_height),
            value = popupHeight.toFloat(),
            range = 100f..500f,
            steps = 39,
            valueText = { ((it / 10f).roundToInt() * 10).toString() },
            onCommit = { onPopupHeightChanged((it / 10f).roundToInt() * 10) },
        )
        if (popupSwipeToDismiss) {
            SettingSlider(
                label = stringResource(Res.string.appearance_popup_swipe_threshold),
                value = popupSwipeThreshold.toFloat(),
                range = 20f..80f,
                steps = 11,
                valueText = { ((it / 5f).roundToInt() * 5).toString() },
                onCommit = { onPopupSwipeThresholdChanged((it / 5f).roundToInt() * 5) },
            )
        }
    }
}

@Composable
fun SettingSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    keyPoints: List<Float>? = null,
    valueText: (Float) -> String,
    onCommit: (Float) -> Unit,
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value) }
    Column(
        verticalArrangement =
            androidx.compose.foundation.layout.Arrangement
                .spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = MiuixTheme.colorScheme.onSurface,
            )
            Text(
                text = valueText(sliderValue),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = range,
            steps = steps,
            keyPoints = keyPoints,
            showKeyPoints = keyPoints != null,
            hapticEffect = SliderHapticEffect.Step,
            onValueChangeFinished = { onCommit(sliderValue) },
        )
    }
}

private fun Double.formatOneDecimal(): String {
    val rounded = (this * 10.0).roundToInt() / 10.0
    return rounded.toString()
}

private fun Double.formatTwoDecimals(): String {
    val rounded = (this * 100.0).roundToInt() / 100.0
    return "%.2f".format(rounded)
}

@Composable
private fun ThemeMode.localizedLabel(): String =
    when (this) {
        ThemeMode.System -> stringResource(Res.string.theme_follow_system)
        ThemeMode.Light -> stringResource(Res.string.theme_light)
        ThemeMode.Dark -> stringResource(Res.string.theme_dark)
    }

@Composable
private fun ReaderThemeMode.localizedLabel(): String =
    when (this) {
        ReaderThemeMode.FollowApp -> stringResource(Res.string.reader_theme_follow_app)
        ReaderThemeMode.Light -> stringResource(Res.string.theme_light)
        ReaderThemeMode.Dark -> stringResource(Res.string.theme_dark)
    }

private val monetKeyColorOptions =
    listOf(
        0xFFF44336,
        0xFFE91E63,
        0xFF9C27B0,
        0xFF673AB7,
        0xFF3F51B5,
        0xFF2196F3,
        0xFF00BCD4,
        0xFF009688,
        0xFF4FAF50,
        0xFFFFEB3B,
        0xFFFFC107,
        0xFFFF9800,
        0xFF795548,
        0xFF607D8F,
        0xFFFF9CA8,
    )
