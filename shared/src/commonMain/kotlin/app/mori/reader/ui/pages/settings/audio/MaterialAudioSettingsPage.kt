package app.mori.reader.ui.pages.settings.audio

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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.AudioPlaybackMode
import app.mori.reader.data.settings.AudioSource
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.audio_auto_play_summary
import app.mori.reader.shared.generated.resources.audio_auto_play_title
import app.mori.reader.shared.generated.resources.audio_background_title
import app.mori.reader.shared.generated.resources.audio_dialog_summary
import app.mori.reader.shared.generated.resources.audio_edit_source_title
import app.mori.reader.shared.generated.resources.audio_local_description
import app.mori.reader.shared.generated.resources.audio_local_importing
import app.mori.reader.shared.generated.resources.audio_local_title
import app.mori.reader.shared.generated.resources.audio_name_label
import app.mori.reader.shared.generated.resources.audio_playback_title
import app.mori.reader.shared.generated.resources.audio_source_title
import app.mori.reader.shared.generated.resources.audio_title
import app.mori.reader.shared.generated.resources.audio_url_label
import app.mori.reader.shared.generated.resources.btn_add
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_delete
import app.mori.reader.shared.generated.resources.btn_import
import app.mori.reader.shared.generated.resources.btn_save
import app.mori.reader.shared.generated.resources.cd_add_source
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.cd_delete_source
import app.mori.reader.shared.generated.resources.cd_drag_sort
import app.mori.reader.ui.components.material.MaterialBackButton
import app.mori.reader.ui.components.material.MaterialExpressiveSwitch
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MaterialSettingsSection
import app.mori.reader.ui.components.settings.MaterialSettingsSurface
import app.mori.reader.ui.components.settings.materialSettingsSegmentedItemShape
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem

@Composable
internal fun MaterialAudioSettingsPage(
    settings: AppSettings,
    controller: AudioSettingsController,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    MoriPageScaffold(
        title = stringResource(Res.string.audio_title),
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            Row {
                MaterialBackButton(onClick = onBack, contentDescription = stringResource(Res.string.cd_back))
                Spacer(modifier = Modifier.size(16.dp))
            }
        },
    ) { paddingValues ->
        LazyColumn(
            state = controller.listState,
            modifier =
                Modifier
                    .fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                MaterialSettingsSection(title = stringResource(Res.string.audio_playback_title)) {
                    MaterialPlaybackCard(settings = settings, onIntent = onIntent)
                }
            }
            item {
                MaterialAudioSectionTitle(text = stringResource(Res.string.audio_source_title))
            }
            itemsIndexed(
                items = controller.localSources,
                key = { _, source -> source.url },
            ) { index, source ->
                MaterialAudioSourceRow(
                    source = source,
                    index = index,
                    count = controller.localSources.size,
                    localAudioImported = settings.audio.localAudioDatabaseSizeBytes > 0L,
                    reorderableState = controller.reorderableState,
                    onIntent = onIntent,
                    onEdit = controller.requestEditSource,
                    onDeleteRequest = controller.requestDeleteSource,
                )
            }
            item {
                MaterialAddAudioSourceRow(onClick = controller.requestAddSource)
            }
            item {
                MaterialSettingsSection(title = stringResource(Res.string.audio_local_title)) {
                    MaterialLocalAudioDescriptionCard()
                    MaterialLocalAudioCard(
                        settings = settings,
                        onImport = controller.launchLocalAudioDatabasePicker,
                        isImporting = controller.isImportingLocalAudio,
                        onDeleteRequest = controller.requestDeleteLocalDatabase,
                    )
                }
            }
        }
    }

    MaterialAudioSourceDialog(
        show = controller.showAddSourceDialog,
        title = stringResource(Res.string.cd_add_source),
        confirmText = stringResource(Res.string.btn_add),
        initialName = "",
        initialUrl = "",
        onDismiss = controller.dismissAddSourceDialog,
        onConfirm = controller.confirmAddSource,
    )
    controller.editingSource?.let { source ->
        MaterialAudioSourceDialog(
            show = true,
            title = stringResource(Res.string.audio_edit_source_title),
            confirmText = stringResource(Res.string.btn_save),
            initialName = source.name,
            initialUrl = source.url,
            onDismiss = controller.dismissEditSourceDialog,
            onConfirm = { name, url -> controller.confirmEditSource(source, name, url) },
        )
    }
    MaterialDeleteAudioDialog(
        pendingDeletion = controller.pendingDeletion,
        onDismiss = controller.dismissDeleteDialog,
        onConfirm = controller.confirmDeletion,
    )
}

@Composable
private fun MaterialPlaybackCard(
    settings: AppSettings,
    onIntent: (SettingsIntent) -> Unit,
) {
    val modes = remember { AudioPlaybackMode.entries.toList() }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        MaterialSwitchRow(
            title = stringResource(Res.string.audio_auto_play_title),
            summary = stringResource(Res.string.audio_auto_play_summary),
            checked = settings.audio.enableAutoplay,
            shape = materialSettingsSegmentedItemShape(index = 0, count = 2),
            onCheckedChange = { onIntent(SettingsIntent.SetAudioEnableAutoplay(it)) },
        )
        MaterialSettingsSurface(
            shape = materialSettingsSegmentedItemShape(index = 1, count = 2),
            onClick = { menuExpanded = true },
        ) {
            ListItem(
                headlineContent = { Text(text = stringResource(Res.string.audio_background_title)) },
                supportingContent = { Text(text = settings.audio.playbackMode.localizedLabel()) },
                trailingContent = {
                    Box {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = settings.audio.playbackMode.localizedLabel(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            modes.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(text = mode.localizedLabel()) },
                                    onClick = {
                                        onIntent(SettingsIntent.SetAudioPlaybackMode(mode))
                                        menuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@Composable
private fun LazyItemScope.MaterialAudioSourceRow(
    source: AudioSource,
    index: Int,
    count: Int,
    localAudioImported: Boolean,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onIntent: (SettingsIntent) -> Unit,
    onEdit: (AudioSource) -> Unit,
    onDeleteRequest: (AudioSource) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val enabled = !source.isLocal || localAudioImported

    ReorderableItem(
        state = reorderableState,
        key = source.url,
    ) {
        MaterialSettingsSurface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        with(this) {
                            Modifier.longPressDraggableHandle(
                                onDragStarted = {
                                    hapticFeedback.performHapticFeedback(
                                        HapticFeedbackType.GestureThresholdActivate,
                                    )
                                },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                },
                            )
                        },
                    ).animateItem(),
            shape = materialSettingsSegmentedItemShape(index = index, count = count),
        ) {
            ListItem(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !source.isLocal) { onEdit(source) },
                headlineContent = {
                    Text(
                        text = audioSourceTitle(source),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                    )
                },
                supportingContent = {
                    Text(
                        text = audioSourceSummary(source),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(Res.string.cd_drag_sort),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MaterialExpressiveSwitch(
                            checked = source.isEnabled,
                            enabled = enabled,
                            onCheckedChange = {
                                onIntent(
                                    SettingsIntent.SetAudioSourceEnabled(
                                        source.url,
                                        it,
                                    ),
                                )
                            },
                        )
                        IconButton(
                            enabled = !source.isDefault && !source.isLocal,
                            onClick = { onDeleteRequest(source) },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(Res.string.cd_delete_source),
                                tint =
                                    if (!source.isDefault && !source.isLocal) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                    },
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@Composable
private fun MaterialAddAudioSourceRow(onClick: () -> Unit) {
    MaterialSettingsSurface(
        shape = materialSettingsSegmentedItemShape(index = 0, count = 1),
        onClick = onClick,
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(Res.string.cd_add_source),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun MaterialLocalAudioDescriptionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = materialInfoCardContainerColor(),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
    ) {
        Text(
            text = stringResource(Res.string.audio_local_description),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun MaterialLocalAudioCard(
    settings: AppSettings,
    onImport: () -> Unit,
    isImporting: Boolean,
    onDeleteRequest: () -> Unit,
) {
    val imported = settings.audio.localAudioDatabaseSizeBytes > 0

    MaterialSettingsSurface(
        shape = materialSettingsSegmentedItemShape(index = 0, count = 1),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            if (isImporting) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        text = stringResource(Res.string.audio_local_importing),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            ListItem(
                headlineContent = { Text(text = "android.db") },
                supportingContent = {
                    Text(text = formatAudioDatabaseSize(settings.audio.localAudioDatabaseSizeBytes))
                },
                trailingContent = {
                    TextButton(
                        enabled = !isImporting,
                        onClick = if (imported) onDeleteRequest else onImport,
                    ) {
                        Text(
                            text =
                                if (imported) {
                                    stringResource(Res.string.btn_delete)
                                } else {
                                    stringResource(Res.string.btn_import)
                                },
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@Composable
private fun MaterialSwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    shape: Shape,
    onCheckedChange: (Boolean) -> Unit,
) {
    MaterialSettingsSurface(
        shape = shape,
        onClick = { onCheckedChange(!checked) },
    ) {
        ListItem(
            headlineContent = { Text(text = title) },
            supportingContent = { Text(text = summary) },
            trailingContent = {
                MaterialExpressiveSwitch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun MaterialAudioSourceDialog(
    show: Boolean,
    title: String,
    confirmText: String,
    initialName: String,
    initialUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    if (!show) return

    var name by remember(show, initialName) { mutableStateOf(initialName) }
    var url by remember(show, initialUrl) { mutableStateOf(initialUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(Res.string.audio_dialog_summary),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = stringResource(Res.string.audio_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(text = stringResource(Res.string.audio_url_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && url.isNotBlank(),
                onClick = { onConfirm(name, url) },
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.btn_cancel))
            }
        },
    )
}

@Composable
private fun MaterialDeleteAudioDialog(
    pendingDeletion: PendingAudioDeletion?,
    onDismiss: () -> Unit,
    onConfirm: (PendingAudioDeletion) -> Unit,
) {
    val current = pendingDeletion ?: return
    val dialogText = audioDeletionDialogText(current)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = dialogText.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = dialogText.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(text = dialogText.message)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(current) }) {
                Text(text = stringResource(Res.string.btn_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.btn_cancel))
            }
        },
    )
}

@Composable
private fun MaterialAudioSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
private fun materialInfoCardContainerColor(): Color =
    MaterialTheme.colorScheme.secondaryContainer
