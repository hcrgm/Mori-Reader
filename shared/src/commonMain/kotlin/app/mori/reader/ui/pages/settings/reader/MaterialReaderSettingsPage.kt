package app.mori.reader.ui.pages.settings.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.settings_reader_title
import app.mori.reader.ui.components.material.MaterialBackButton
import app.mori.reader.ui.components.material.MaterialDropdownMenuOption
import app.mori.reader.ui.components.material.MaterialDropdownSelectorRow
import app.mori.reader.ui.components.material.MaterialExpressiveSwitch
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MaterialSettingsGroup
import app.mori.reader.ui.components.settings.MaterialSettingsSurface
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MaterialReaderSettingsPage(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    val sections = materialReaderSections(settings = settings, onSettingsIntent = onSettingsIntent)

    MoriPageScaffold(
        title = stringResource(Res.string.settings_reader_title),
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            Row {
                MaterialBackButton(onClick = onBack, contentDescription = stringResource(Res.string.cd_back))
                Spacer(modifier = Modifier.size(16.dp))
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sections.forEach { section ->
                item {
                    MaterialReaderSection(
                        title = section.title,
                        items = section.items,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialReaderSection(
    title: String,
    items: List<MaterialReaderItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )
        MaterialSettingsGroup {
            items.forEachIndexed { index, item ->
                MaterialReaderRow(
                    item = item,
                    shape = materialReaderSegmentedItemShape(index = index, count = items.size),
                    showDivider = index > 0,
                )
            }
        }
    }
}

@Composable
private fun MaterialReaderRow(
    item: MaterialReaderItem,
    shape: Shape,
    showDivider: Boolean,
) {
    when (item) {
        is MaterialReaderItem.Choice -> MaterialReaderChoiceRow(item = item, shape = shape, showDivider = showDivider)
        is MaterialReaderItem.Slider -> MaterialReaderSliderRow(item = item, shape = shape, showDivider = showDivider)
        is MaterialReaderItem.Switch -> MaterialReaderSwitchRow(item = item, shape = shape, showDivider = showDivider)
    }
}

@Composable
private fun MaterialReaderChoiceRow(
    item: MaterialReaderItem.Choice,
    shape: Shape,
    showDivider: Boolean,
) {
    MaterialDropdownSelectorRow(
        title = item.title,
        summary = item.summary,
        selectedLabel = item.selectedLabel,
        options =
            item.options.map { option ->
                MaterialDropdownMenuOption(
                    label = option.label,
                    selected = option.selected,
                    onSelected = option.onSelected,
                )
            },
        shape = shape,
        groupedInSection = true,
        showDivider = showDivider,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape),
    )
}

@Composable
private fun MaterialReaderSwitchRow(
    item: MaterialReaderItem.Switch,
    shape: Shape,
    showDivider: Boolean,
) {
    MaterialSettingsSurface(
        shape = shape,
        groupedInSection = true,
        showDivider = showDivider,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable { item.onCheckedChange(!item.checked) },
    ) {
        ListItem(
            headlineContent = { Text(text = item.title) },
            supportingContent = { item.summary?.let { Text(text = it) } },
            trailingContent = {
                MaterialExpressiveSwitch(
                    checked = item.checked,
                    onCheckedChange = item.onCheckedChange,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun MaterialReaderSliderRow(
    item: MaterialReaderItem.Slider,
    shape: Shape,
    showDivider: Boolean,
) {
    var sliderValue by remember(item.value) { mutableFloatStateOf(item.value) }

    MaterialSettingsSurface(
        shape = shape,
        groupedInSection = true,
        showDivider = showDivider,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(text = item.title, style = MaterialTheme.typography.bodyLarge)
                    item.summary?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Text(
                    text = item.valueText(sliderValue),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = item.valueRange,
                steps = item.steps,
                onValueChangeFinished = { item.onCommit(sliderValue) },
            )
        }
    }
}

@Composable
private fun materialReaderSegmentedItemShape(
    index: Int,
    count: Int,
): Shape =
    when {
        count == 1 -> {
            MaterialTheme.shapes.large
        }

        index == 0 -> {
            MaterialTheme.shapes.large.copy(
                bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
                bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd,
            )
        }

        index == count - 1 -> {
            MaterialTheme.shapes.large.copy(
                topStart = MaterialTheme.shapes.extraSmall.topStart,
                topEnd = MaterialTheme.shapes.extraSmall.topEnd,
            )
        }

        else -> {
            MaterialTheme.shapes.extraSmall
        }
    }
