package app.mori.reader.ui.pages.settings.audio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import app.mori.reader.shared.generated.resources.audio_name_label
import app.mori.reader.shared.generated.resources.audio_url_label
import app.mori.reader.shared.generated.resources.btn_add
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_delete
import app.mori.reader.shared.generated.resources.btn_import
import app.mori.reader.shared.generated.resources.btn_save
import app.mori.reader.shared.generated.resources.cd_add_source
import app.mori.reader.shared.generated.resources.cd_delete_source
import app.mori.reader.shared.generated.resources.cd_drag_sort
import app.mori.reader.ui.components.settings.MoriInfoCard
import app.mori.reader.ui.components.settings.MoriSettingsHorizontalPadding
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun PlaybackCard(
    settings: AppSettings,
    onIntent: (SettingsIntent) -> Unit,
) {
    val modes = remember { AudioPlaybackMode.entries.toList() }
    val modeItems = modes.map { SpinnerEntry(title = it.localizedLabel()) }
    Column {
        SwitchPreference(
            checked = settings.audio.enableAutoplay,
            onCheckedChange = { onIntent(SettingsIntent.SetAudioEnableAutoplay(it)) },
            title = stringResource(Res.string.audio_auto_play_title),
            summary = stringResource(Res.string.audio_auto_play_summary),
        )
        WindowSpinnerPreference(
            items = modeItems,
            selectedIndex = modes.indexOf(settings.audio.playbackMode).coerceAtLeast(0),
            title = stringResource(Res.string.audio_background_title),
            summary = settings.audio.playbackMode.localizedLabel(),
            onSelectedIndexChange = { index ->
                onIntent(SettingsIntent.SetAudioPlaybackMode(modes[index]))
            },
        )
    }
}

internal fun LazyListScope.audioSourceItems(
    sources: List<AudioSource>,
    localAudioImported: Boolean,
    reorderableState: ReorderableLazyListState,
    onIntent: (SettingsIntent) -> Unit,
    onEdit: (AudioSource) -> Unit,
    onDeleteRequest: (AudioSource) -> Unit,
) {
    items(
        items = sources,
        key = { it.url },
    ) { source ->
        AudioSourceRow(
            source = source,
            localAudioImported = localAudioImported,
            reorderableState = reorderableState,
            onIntent = onIntent,
            onEdit = onEdit,
            onDeleteRequest = onDeleteRequest,
        )
    }
}

@Composable
private fun LazyItemScope.AudioSourceRow(
    source: AudioSource,
    localAudioImported: Boolean,
    reorderableState: ReorderableLazyListState,
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
        Card(
            modifier =
                Modifier
                    .padding(horizontal = MoriSettingsHorizontalPadding)
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
        ) {
            Row(
                modifier =
                    Modifier
                        .clickable(enabled = !source.isLocal) { onEdit(source) }
                        .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = audioSourceTitle(source),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                        color =
                            if (source.isEnabled) {
                                MiuixTheme.colorScheme.onSurface
                            } else {
                                MiuixTheme.colorScheme.onSurfaceVariantSummary
                            },
                    )
                    Text(
                        text = audioSourceSummary(source),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                Icon(
                    imageVector = MiuixIcons.Sort,
                    contentDescription = stringResource(Res.string.cd_drag_sort),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Switch(
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
                        imageVector = MiuixIcons.Delete,
                        contentDescription = stringResource(Res.string.cd_delete_source),
                        tint = MiuixTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AddAudioSourceCard(onClick: () -> Unit) {
    Card(
        modifier =
            Modifier
                .padding(horizontal = MoriSettingsHorizontalPadding)
                .fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(Res.string.cd_add_source),
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
internal fun AddAudioSourceDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    AudioSourceDialog(
        show = show,
        title = stringResource(Res.string.cd_add_source),
        confirmText = stringResource(Res.string.btn_add),
        initialName = "",
        initialUrl = "",
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
internal fun EditAudioSourceDialog(
    source: AudioSource?,
    onDismiss: () -> Unit,
    onConfirm: (AudioSource, String, String) -> Unit,
) {
    val editingSource = source ?: return
    AudioSourceDialog(
        show = true,
        title = stringResource(Res.string.audio_edit_source_title),
        confirmText = stringResource(Res.string.btn_save),
        initialName = editingSource.name,
        initialUrl = editingSource.url,
        onDismiss = onDismiss,
        onConfirm = { name, url -> onConfirm(editingSource, name, url) },
    )
}

@Composable
private fun AudioSourceDialog(
    show: Boolean,
    title: String,
    confirmText: String,
    initialName: String,
    initialUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var name by remember(show, initialName) { mutableStateOf(initialName) }
    var url by remember(show, initialUrl) { mutableStateOf(initialUrl) }

    WindowDialog(
        title = title,
        summary = stringResource(Res.string.audio_dialog_summary),
        show = show,
        onDismissRequest = onDismiss,
        onDismissFinished = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AudioTextField(
                value = name,
                label = stringResource(Res.string.audio_name_label),
                onValueChange = { name = it },
            )
            AudioTextField(
                value = url,
                label = stringResource(Res.string.audio_url_label),
                onValueChange = { url = it },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    text = stringResource(Res.string.btn_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = confirmText,
                    enabled = name.isNotBlank() && url.isNotBlank(),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = { onConfirm(name, url) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AudioTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        useLabelAsPlaceholder = true,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
internal fun LocalAudioDescriptionCard() {
    MoriInfoCard(text = stringResource(Res.string.audio_local_description))
}

@Composable
internal fun LocalAudioCard(
    settings: AppSettings,
    onImport: () -> Unit,
    isImporting: Boolean,
    onDeleteRequest: () -> Unit,
) {
    val imported = settings.audio.localAudioDatabaseSizeBytes > 0

    Column {
        if (isImporting) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LinearProgressIndicator()
                Text(
                    text = stringResource(Res.string.audio_local_importing),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "android.db")
                Text(
                    text = formatAudioDatabaseSize(settings.audio.localAudioDatabaseSizeBytes),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            if (!imported) {
                TextButton(
                    text = stringResource(Res.string.btn_import),
                    onClick = onImport,
                    enabled = !isImporting,
                )
            } else {
                TextButton(
                    text = stringResource(Res.string.btn_delete),
                    onClick = onDeleteRequest,
                    enabled = !isImporting,
                )
            }
        }
    }
}

@Composable
internal fun DeleteAudioDialog(
    pendingDeletion: PendingAudioDeletion?,
    onDismiss: () -> Unit,
    onConfirm: (PendingAudioDeletion) -> Unit,
) {
    val current = pendingDeletion ?: return
    val dialogText = audioDeletionDialogText(current)

    WindowDialog(
        title = dialogText.title,
        summary = dialogText.summary,
        show = true,
        onDismissRequest = onDismiss,
        onDismissFinished = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = dialogText.message,
                modifier = Modifier.fillMaxWidth(),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    text = stringResource(Res.string.btn_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(Res.string.btn_delete),
                    onClick = { onConfirm(current) },
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
