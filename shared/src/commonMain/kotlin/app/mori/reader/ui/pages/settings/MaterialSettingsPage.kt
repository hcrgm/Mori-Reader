package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.tab_settings
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MaterialSettingsGroup
import app.mori.reader.ui.components.settings.MaterialSettingsSurface
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MaterialSettingsPage(
    settings: AppSettings,
    fixedPadding: PaddingValues,
    onOpenAppearanceSettings: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onOpenAnkiSettings: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val entries =
        settingsEntries(
            onOpenAppearanceSettings = onOpenAppearanceSettings,
            onOpenReaderSettings = onOpenReaderSettings,
            onOpenDictionarySettings = onOpenDictionarySettings,
            onOpenAudioSettings = onOpenAudioSettings,
            onOpenAnkiSettings = onOpenAnkiSettings,
        )
    val aboutEntries = settingsAboutEntries(onOpenAbout = onOpenAbout)

    MoriPageScaffold(
        title = stringResource(Res.string.tab_settings),
        blurEnabled = settings.appearance.blurEnabled,
        fixedPadding = fixedPadding,
    ) { paddingValues ->
        val scrollReserve =
            (paddingValues.calculateTopPadding() - paddingValues.calculateBottomPadding())
                .coerceAtLeast(0.dp)
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues) 
                    .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            MaterialSegmentedColumn(
                entries = entries,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            MaterialSegmentedColumn(
                entries = aboutEntries,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Spacer(
                modifier =
                    Modifier.height(
                        scrollReserve +
                            fixedPadding.calculateBottomPadding() +
                            24.dp,
                    ),
            )
        }
    }
}

@Composable
private fun MaterialSegmentedColumn(
    entries: List<SettingsEntry>,
    modifier: Modifier = Modifier,
) {
    MaterialSettingsGroup(modifier = modifier) {
        entries.forEachIndexed { index, entry ->
            MaterialSettingsEntry(
                entry = entry,
                shape = segmentedItemShape(index = index, count = entries.size),
                showDivider = index > 0,
            )
        }
    }
}

@Composable
private fun MaterialSettingsEntry(
    entry: SettingsEntry,
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
                .clickable(onClick = entry.onClick),
    ) {
        ListItem(
            headlineContent = { Text(text = entry.title) },
            leadingContent = {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = entry.title,
                    modifier = Modifier.size(24.dp),
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            },
            colors =
                ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
        )
    }
}

@Composable
private fun segmentedItemShape(
    index: Int,
    count: Int,
): Shape =
    when {
        count == 1 -> {
            MaterialTheme.shapes.large
        }

        index == 0 -> {
            RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 4.dp,
                bottomEnd = 4.dp,
            )
        }

        index == count - 1 -> {
            RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 4.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp,
            )
        }

        else -> {
            RoundedCornerShape(4.dp)
        }
    }
