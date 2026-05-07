package app.mori.reader.ui.components.settings

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
import app.mori.reader.data.settings.ReaderThemeMode
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.data.settings.LanguageMode

import app.mori.reader.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults.SliderHapticEffect
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

@Composable
fun AppThemeSettingsCard(
    themeMode: ThemeMode,
    languageMode: LanguageMode,
    blurEnabled: Boolean,
    modifier: Modifier = Modifier,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onLanguageModeSelected: (LanguageMode) -> Unit,
    onBlurEnabledChanged: (Boolean) -> Unit,
) {
    val themeModes = remember { ThemeMode.entries.toList() }
    val themeModeItems = themeModes.map { SpinnerEntry(title = it.localizedLabel()) }
    val languageModes = remember { LanguageMode.entries.toList() }
    val followSystemLabel = stringResource(Res.string.language_follow_system)
    val englishLabel = stringResource(Res.string.language_english)
    val chineseLabel = stringResource(Res.string.language_chinese)
    val languageModeItems = remember(languageModes, followSystemLabel, englishLabel, chineseLabel) {
        languageModes.map { mode ->
            SpinnerEntry(
                title = when (mode) {
                    LanguageMode.System -> followSystemLabel
                    LanguageMode.English -> englishLabel
                    LanguageMode.Chinese -> chineseLabel
                }
            )
        }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        OverlaySpinnerPreference(
            items = themeModeItems,
            selectedIndex = themeModes.indexOf(themeMode).coerceAtLeast(0),
            title = stringResource(Res.string.appearance_theme_title),
            summary = stringResource(Res.string.appearance_theme_summary),
            onSelectedIndexChange = { index ->
                onThemeModeSelected(themeModes[index])
            },
        )
        OverlaySpinnerPreference(
            items = languageModeItems,
            selectedIndex = languageModes.indexOf(languageMode).coerceAtLeast(0),
            title = stringResource(Res.string.appearance_language_title),
            summary = stringResource(Res.string.appearance_language_summary),
            onSelectedIndexChange = { index ->
                onLanguageModeSelected(languageModes[index])
            },
        )
        if (isRenderEffectSupported()) {
            SwitchPreference(
                checked = blurEnabled,
                onCheckedChange = onBlurEnabledChanged,
                title = stringResource(Res.string.appearance_blur_title),
                summary = stringResource(Res.string.appearance_blur_summary),
            )
        }
    }
}

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
    val readerThemeModes = remember { ReaderThemeMode.entries.toList() }
    val readerThemeModeItems = readerThemeModes.map { SpinnerEntry(title = it.localizedLabel()) }
    val verticalWritingLabel = stringResource(Res.string.appearance_writing_vertical)
    val horizontalWritingLabel = stringResource(Res.string.appearance_writing_horizontal)
    val paginationLabel = stringResource(Res.string.appearance_reading_pagination)
    val continuousLabel = stringResource(Res.string.appearance_reading_continuous)
    val writingModeItems = remember(verticalWritingLabel, horizontalWritingLabel) {
        listOf(
            SpinnerEntry(title = verticalWritingLabel),
            SpinnerEntry(title = horizontalWritingLabel),
        )
    }
    val readingModeItems = remember(paginationLabel, continuousLabel) {
        listOf(
            SpinnerEntry(title = paginationLabel),
            SpinnerEntry(title = continuousLabel),
        )
    }

    Card(modifier = modifier.fillMaxWidth()) {
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
            summary = if (avoidPageBreak) stringResource(Res.string.appearance_avoid_page_break_on) else stringResource(
                Res.string.appearance_avoid_page_break_off
            ),
        )
        SwitchPreference(
            checked = justifyText,
            onCheckedChange = onJustifyTextChanged,
            title = stringResource(Res.string.appearance_justify_title),
        )
        HorizontalDivider(modifier = Modifier.padding(start = 20.dp))
        SmallTitle(
            text = stringResource(Res.string.appearance_typography),
            insideMargin = PaddingValues(14.dp),
        )
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
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
        HorizontalDivider(modifier = Modifier.padding(start = 20.dp))
        SmallTitle(
            text = stringResource(Res.string.appearance_popup_title),
            insideMargin = PaddingValues(14.dp),
        )
        SwitchPreference(
            checked = popupFullWidth,
            onCheckedChange = { onTogglePopupFullWidth() },
            title = stringResource(Res.string.appearance_popup_full_width_title),
            summary = if (popupFullWidth) stringResource(Res.string.appearance_popup_full_width_on) else stringResource(
                Res.string.appearance_popup_full_width_off
            ),
        )
        SwitchPreference(
            checked = popupSwipeToDismiss,
            onCheckedChange = { onTogglePopupSwipeToDismiss() },
            title = stringResource(Res.string.appearance_popup_swipe_dismiss_title),
            summary = if (popupSwipeToDismiss) stringResource(Res.string.appearance_popup_swipe_dismiss_on) else stringResource(
                Res.string.appearance_popup_swipe_dismiss_off
            ),
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
    Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
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
private fun ThemeMode.localizedLabel(): String = when (this) {
    ThemeMode.System -> stringResource(Res.string.theme_follow_system)
    ThemeMode.Light -> stringResource(Res.string.theme_light)
    ThemeMode.Dark -> stringResource(Res.string.theme_dark)
}

@Composable
private fun ReaderThemeMode.localizedLabel(): String = when (this) {
    ReaderThemeMode.FollowApp -> stringResource(Res.string.reader_theme_follow_app)
    ReaderThemeMode.Light -> stringResource(Res.string.theme_light)
    ReaderThemeMode.Dark -> stringResource(Res.string.theme_dark)
}
