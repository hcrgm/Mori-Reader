package app.mori.reader.ui.pages.settings.reader

import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.ReaderThemeMode
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.appearance_avoid_page_break_off
import app.mori.reader.shared.generated.resources.appearance_avoid_page_break_on
import app.mori.reader.shared.generated.resources.appearance_avoid_page_break_title
import app.mori.reader.shared.generated.resources.appearance_action_bar_pinned_title
import app.mori.reader.shared.generated.resources.appearance_character_spacing
import app.mori.reader.shared.generated.resources.appearance_font_size
import app.mori.reader.shared.generated.resources.appearance_fullscreen_title
import app.mori.reader.shared.generated.resources.appearance_hide_furigana_title
import app.mori.reader.shared.generated.resources.appearance_horizontal_margin
import app.mori.reader.shared.generated.resources.appearance_justify_title
import app.mori.reader.shared.generated.resources.appearance_line_height
import app.mori.reader.shared.generated.resources.appearance_page_mode_summary
import app.mori.reader.shared.generated.resources.appearance_page_mode_title
import app.mori.reader.shared.generated.resources.appearance_reader_theme_title
import app.mori.reader.shared.generated.resources.appearance_reading_continuous
import app.mori.reader.shared.generated.resources.appearance_reading_pagination
import app.mori.reader.shared.generated.resources.appearance_show_reading_info_title
import app.mori.reader.shared.generated.resources.appearance_vertical_margin
import app.mori.reader.shared.generated.resources.appearance_writing_direction_summary
import app.mori.reader.shared.generated.resources.appearance_writing_direction_title
import app.mori.reader.shared.generated.resources.appearance_writing_horizontal
import app.mori.reader.shared.generated.resources.appearance_writing_vertical
import app.mori.reader.shared.generated.resources.reader_settings_display_title
import app.mori.reader.shared.generated.resources.reader_settings_typography_title
import app.mori.reader.shared.generated.resources.reader_theme_follow_app
import app.mori.reader.shared.generated.resources.theme_dark
import app.mori.reader.shared.generated.resources.theme_light
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

internal data class MaterialReaderSection(
    val title: String,
    val items: List<MaterialReaderItem>,
)

internal sealed interface MaterialReaderItem {
    val title: String
    val summary: String?

    data class Choice(
        override val title: String,
        override val summary: String?,
        val selectedLabel: String,
        val options: List<MaterialReaderChoiceOption>,
    ) : MaterialReaderItem

    data class Switch(
        override val title: String,
        override val summary: String?,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
    ) : MaterialReaderItem

    data class Slider(
        override val title: String,
        override val summary: String?,
        val value: Float,
        val valueRange: ClosedFloatingPointRange<Float>,
        val steps: Int,
        val valueText: (Float) -> String,
        val onCommit: (Float) -> Unit,
    ) : MaterialReaderItem
}

internal data class MaterialReaderChoiceOption(
    val label: String,
    val selected: Boolean,
    val onSelected: () -> Unit,
)

@Composable
internal fun materialReaderSections(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
): List<MaterialReaderSection> =
    listOf(
        MaterialReaderSection(
            title = stringResource(Res.string.reader_settings_display_title),
            items = materialReaderDisplayItems(settings, onSettingsIntent),
        ),
        MaterialReaderSection(
            title = stringResource(Res.string.reader_settings_typography_title),
            items = materialReaderTypographyItems(settings, onSettingsIntent),
        ),
    )

@Composable
private fun materialReaderDisplayItems(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
): List<MaterialReaderItem> {
    val readerThemeOptions =
        ReaderThemeMode.entries.map { mode ->
            MaterialReaderChoiceOption(
                label = mode.localizedLabel(),
                selected = mode == settings.appearance.readerThemeMode,
                onSelected = { onSettingsIntent(SettingsIntent.SetReaderThemeMode(mode)) },
            )
        }
    val verticalWritingLabel = stringResource(Res.string.appearance_writing_vertical)
    val horizontalWritingLabel = stringResource(Res.string.appearance_writing_horizontal)
    val paginationLabel = stringResource(Res.string.appearance_reading_pagination)
    val continuousLabel = stringResource(Res.string.appearance_reading_continuous)

    return listOf(
        MaterialReaderItem.Switch(
            title = stringResource(Res.string.appearance_fullscreen_title),
            summary = null,
            checked = settings.reader.fullscreen,
            onCheckedChange = { onSettingsIntent(SettingsIntent.SetReaderFullscreen(it)) },
        ),
        MaterialReaderItem.Switch(
            title = stringResource(Res.string.appearance_action_bar_pinned_title),
            summary = null,
            checked = settings.reader.actionBarPinned,
            onCheckedChange = { onSettingsIntent(SettingsIntent.SetReaderActionBarPinned(it)) },
        ),
        MaterialReaderItem.Switch(
            title = stringResource(Res.string.appearance_show_reading_info_title),
            summary = null,
            checked = settings.reader.showReadingInfo,
            onCheckedChange = { onSettingsIntent(SettingsIntent.SetReaderShowReadingInfo(it)) },
        ),
        MaterialReaderItem.Choice(
            title = stringResource(Res.string.appearance_reader_theme_title),
            summary = null,
            selectedLabel = readerThemeOptions.firstOrNull { it.selected }?.label.orEmpty(),
            options = readerThemeOptions,
        ),
        MaterialReaderItem.Choice(
            title = stringResource(Res.string.appearance_writing_direction_title),
            summary = stringResource(Res.string.appearance_writing_direction_summary),
            selectedLabel = if (settings.reader.verticalWriting) verticalWritingLabel else horizontalWritingLabel,
            options =
                listOf(
                    MaterialReaderChoiceOption(
                        label = verticalWritingLabel,
                        selected = settings.reader.verticalWriting,
                        onSelected = { onSettingsIntent(SettingsIntent.SetReaderVerticalWriting(true)) },
                    ),
                    MaterialReaderChoiceOption(
                        label = horizontalWritingLabel,
                        selected = !settings.reader.verticalWriting,
                        onSelected = { onSettingsIntent(SettingsIntent.SetReaderVerticalWriting(false)) },
                    ),
                ),
        ),
        MaterialReaderItem.Choice(
            title = stringResource(Res.string.appearance_page_mode_title),
            summary = stringResource(Res.string.appearance_page_mode_summary),
            selectedLabel = if (settings.reader.continuousMode) continuousLabel else paginationLabel,
            options =
                listOf(
                    MaterialReaderChoiceOption(
                        label = paginationLabel,
                        selected = !settings.reader.continuousMode,
                        onSelected = { onSettingsIntent(SettingsIntent.SetReaderContinuousMode(false)) },
                    ),
                    MaterialReaderChoiceOption(
                        label = continuousLabel,
                        selected = settings.reader.continuousMode,
                        onSelected = { onSettingsIntent(SettingsIntent.SetReaderContinuousMode(true)) },
                    ),
                ),
        ),
        MaterialReaderItem.Switch(
            title = stringResource(Res.string.appearance_hide_furigana_title),
            summary = null,
            checked = settings.reader.hideFurigana,
            onCheckedChange = { onSettingsIntent(SettingsIntent.SetReaderHideFurigana(it)) },
        ),
    )
}

@Composable
private fun materialReaderTypographyItems(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
): List<MaterialReaderItem> =
    listOf(
        MaterialReaderItem.Slider(
            title = stringResource(Res.string.appearance_font_size),
            summary = null,
            value = settings.reader.fontSize.toFloat(),
            valueRange = 16f..40f,
            steps = 23,
            valueText = { it.roundToInt().toString() },
            onCommit = { onSettingsIntent(SettingsIntent.SetReaderFontSize(it.roundToInt())) },
        ),
        MaterialReaderItem.Slider(
            title = stringResource(Res.string.appearance_horizontal_margin),
            summary = null,
            value = settings.reader.horizontalPadding.toFloat(),
            valueRange = 0f..50f,
            steps = 49,
            valueText = { "${it.roundToInt()}%" },
            onCommit = { onSettingsIntent(SettingsIntent.SetReaderHorizontalPadding(it.roundToInt())) },
        ),
        MaterialReaderItem.Slider(
            title = stringResource(Res.string.appearance_vertical_margin),
            summary = null,
            value = settings.reader.verticalPadding.toFloat(),
            valueRange = 0f..50f,
            steps = 49,
            valueText = { "${it.roundToInt()}%" },
            onCommit = { onSettingsIntent(SettingsIntent.SetReaderVerticalPadding(it.roundToInt())) },
        ),
        MaterialReaderItem.Slider(
            title = stringResource(Res.string.appearance_line_height),
            summary = null,
            value = settings.reader.lineHeight.toFloat(),
            valueRange = 1.0f..2.5f,
            steps = 29,
            valueText = { ((it * 20f).roundToInt() / 20.0).formatTwoDecimals() },
            onCommit = {
                onSettingsIntent(
                    SettingsIntent.SetReaderLineHeight((it * 20f).roundToInt() / 20.0),
                )
            },
        ),
        MaterialReaderItem.Slider(
            title = stringResource(Res.string.appearance_character_spacing),
            summary = null,
            value = settings.reader.characterSpacing.toFloat(),
            valueRange = -10f..10f,
            steps = 19,
            valueText = { "${it.roundToInt()}%" },
            onCommit = {
                onSettingsIntent(
                    SettingsIntent.SetReaderCharacterSpacing(it.roundToInt().toDouble()),
                )
            },
        ),
        MaterialReaderItem.Switch(
            title = stringResource(Res.string.appearance_avoid_page_break_title),
            summary =
                if (settings.reader.avoidPageBreak) {
                    stringResource(Res.string.appearance_avoid_page_break_on)
                } else {
                    stringResource(Res.string.appearance_avoid_page_break_off)
                },
            checked = settings.reader.avoidPageBreak,
            onCheckedChange = { onSettingsIntent(SettingsIntent.SetReaderAvoidPageBreak(it)) },
        ),
        MaterialReaderItem.Switch(
            title = stringResource(Res.string.appearance_justify_title),
            summary = null,
            checked = settings.reader.justifyText,
            onCheckedChange = { onSettingsIntent(SettingsIntent.SetReaderJustifyText(it)) },
        ),
    )

@Composable
private fun ReaderThemeMode.localizedLabel(): String =
    when (this) {
        ReaderThemeMode.FollowApp -> stringResource(Res.string.reader_theme_follow_app)
        ReaderThemeMode.Light -> stringResource(Res.string.theme_light)
        ReaderThemeMode.Dark -> stringResource(Res.string.theme_dark)
    }

private fun Double.formatTwoDecimals(): String {
    val rounded = (this * 100.0).roundToInt() / 100.0
    return "%.2f".format(rounded)
}
