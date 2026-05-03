package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mori.reader.data.audio.rememberLocalAudioDatabasePicker
import app.mori.reader.data.settings.AudioPlaybackMode
import app.mori.reader.data.settings.AudioSource
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import app.mori.reader.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val AudioSettingsHorizontalPadding = 12.dp

private enum class PendingAudioDeletionType {
    Source,
    LocalDatabase,
}

private data class PendingAudioDeletion(
    val type: PendingAudioDeletionType,
    val source: AudioSource? = null,
)

@Composable
fun AudioSettingsPage(
    state: AppState,
    message: String?,
    onIntent: (AppIntent) -> Unit,
    onBack: () -> Unit,
) {
    val launchDbPicker = rememberLocalAudioDatabasePicker { uri ->
        onIntent(AppIntent.ImportLocalAudioDatabase(uri))
    }
    val isImportingLocalAudio = state.settings.isImportingLocalAudio
    val listState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    val onIntentState by rememberUpdatedState(onIntent)
    var showAddSourceDialog by remember { mutableStateOf(false) }
    var editingSource by remember { mutableStateOf<AudioSource?>(null) }
    var localSources by remember { mutableStateOf(state.settings.audioSources) }
    var pendingDeletion by remember { mutableStateOf<PendingAudioDeletion?>(null) }

    LaunchedEffect(state.settings.audioSources) {
        localSources = state.settings.audioSources
    }

    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        val listStartIndex = 4
        val fromRelative = from.index - listStartIndex
        val toRelative = to.index - listStartIndex
        if (
            fromRelative !in localSources.indices ||
            toRelative !in localSources.indices
        ) {
            return@rememberReorderableLazyListState
        }

        localSources = localSources.toMutableList().apply {
            add(toRelative, removeAt(fromRelative))
        }
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LaunchedEffect(reorderableState.isAnyItemDragging, state.settings.audioSources, localSources) {
        if (!reorderableState.isAnyItemDragging) {
            val updatedUrls = localSources.map(AudioSource::url)
            if (updatedUrls != state.settings.audioSources.map(AudioSource::url)) {
                onIntentState(AppIntent.ReorderAudioSources(updatedUrls))
            }
        }
    }

    MoriPageScaffold(
title = stringResource(Res.string.audio_title),
        blurEnabled = state.settings.blurEnabled,
        message = message,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(end = 6.dp, bottom = 6.dp),
                onClick = {
                    if (!isImportingLocalAudio) {
                        showAddSourceDialog = true
                    }
                },
                containerColor = MiuixTheme.colorScheme.background,
            ) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = stringResource(Res.string.cd_add_source),
                    modifier = Modifier.size(26.dp),
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
        },
    ) { paddingValues, scrollBehavior ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    SmallTitle(text = stringResource(Res.string.audio_playback_title))
                }
                item {
                    Card(modifier = Modifier.padding(horizontal = AudioSettingsHorizontalPadding)) {
                        PlaybackCard(state = state, onIntent = onIntent)
                    }
                }
                item {
                    SmallTitle(text = stringResource(Res.string.audio_source_title))
                }
                audioSourceItems(
                    sources = localSources,
                    localAudioImported = state.settings.localAudioDatabaseSizeBytes > 0L,
                    reorderableState = reorderableState,
                    onIntent = onIntent,
                    onEdit = { source -> editingSource = source },
                    onDeleteRequest = { source ->
                        pendingDeletion = PendingAudioDeletion(
                            type = PendingAudioDeletionType.Source,
                            source = source,
                        )
                    },
                )
                item {
                    SmallTitle(text = stringResource(Res.string.audio_local_title))
                }
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = AudioSettingsHorizontalPadding),
                        colors = CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            contentColor = MiuixTheme.colorScheme.onSurface
                        ),
                        insideMargin = PaddingValues(16.dp),
                    ) {
                        Text(
text = stringResource(Res.string.audio_local_description),
                            color = MiuixTheme.colorScheme.primary,
                        )
                    }
                }
                item {
                    Card(modifier = Modifier.padding(horizontal = AudioSettingsHorizontalPadding)) {
                        LocalAudioCard(
                            state = state,
                            onImport = launchDbPicker,
                            isImporting = isImportingLocalAudio,
                            onDeleteRequest = {
                                pendingDeletion = PendingAudioDeletion(
                                    type = PendingAudioDeletionType.LocalDatabase,
                                )
                            },
                        )
                    }
                }
            }

            AddAudioSourceDialog(
                show = showAddSourceDialog,
                onDismiss = { showAddSourceDialog = false },
                onConfirm = { name, url ->
                    onIntent(AppIntent.AddAudioSource(name, url))
                    showAddSourceDialog = false
                },
            )
            EditAudioSourceDialog(
                source = editingSource,
                onDismiss = { editingSource = null },
                onConfirm = { source, name, url ->
                    onIntent(AppIntent.UpdateAudioSource(source.url, name, url))
                    editingSource = null
                },
            )
            DeleteAudioDialog(
                pendingDeletion = pendingDeletion,
                onDismiss = { pendingDeletion = null },
                onConfirm = { deletion ->
                    when (deletion.type) {
                        PendingAudioDeletionType.Source -> {
                            deletion.source?.let { source ->
                                onIntent(AppIntent.DeleteAudioSource(source.url))
                            }
                        }

                        PendingAudioDeletionType.LocalDatabase -> {
                            onIntent(AppIntent.DeleteLocalAudioDatabase)
                        }
                    }
                    pendingDeletion = null
                },
            )
        }
    }
}

@Composable
private fun PlaybackCard(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
) {
    val modes = remember { AudioPlaybackMode.entries.toList() }
    val modeItems = remember(modes) { modes.map { SpinnerEntry(title = it.label) } }
    Column {
        SwitchPreference(
            checked = state.settings.audioEnableAutoplay,
            onCheckedChange = { onIntent(AppIntent.SetAudioEnableAutoplay(it)) },
            title = stringResource(Res.string.audio_auto_play_title),
            summary = stringResource(Res.string.audio_auto_play_summary),
        )
        OverlaySpinnerPreference(
            items = modeItems,
            selectedIndex = modes.indexOf(state.settings.audioPlaybackMode).coerceAtLeast(0),
            title = stringResource(Res.string.audio_background_title),
            summary = state.settings.audioPlaybackMode.label,
            onSelectedIndexChange = { index ->
                onIntent(AppIntent.SetAudioPlaybackMode(modes[index]))
            },
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.audioSourceItems(
    sources: List<AudioSource>,
    localAudioImported: Boolean,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onIntent: (AppIntent) -> Unit,
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
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onIntent: (AppIntent) -> Unit,
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
            modifier = Modifier
                .padding(horizontal = AudioSettingsHorizontalPadding)
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
                )
                .animateItem(),
        ) {
            Row(
                modifier = Modifier
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
                        color = if (source.isEnabled) {
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
                    onCheckedChange = { onIntent(AppIntent.SetAudioSourceEnabled(source.url, it)) },
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
private fun AddAudioSourceDialog(
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
private fun EditAudioSourceDialog(
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

    OverlayDialog(
        title = title,
        summary = "支持 Yomitan JSON 音源，URL 需要包含 {term} 或 {reading}。",
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
                label = "URL",
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
private fun LocalAudioCard(
    state: AppState,
    onImport: () -> Unit,
    isImporting: Boolean,
    onDeleteRequest: () -> Unit,
) {
    val imported = state.settings.localAudioDatabaseSizeBytes > 0

    Column {
        if (isImporting) {
            Column(
                modifier = Modifier
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "android.db")
                Text(
                    text = formatBytes(state.settings.localAudioDatabaseSizeBytes),
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
private fun DeleteAudioDialog(
    pendingDeletion: PendingAudioDeletion?,
    onDismiss: () -> Unit,
    onConfirm: (PendingAudioDeletion) -> Unit,
) {
    val current = pendingDeletion ?: return
    val title = when (current.type) {
        PendingAudioDeletionType.Source -> stringResource(Res.string.cd_delete_source)
        PendingAudioDeletionType.LocalDatabase -> stringResource(Res.string.audio_delete_local_title)
    }
    val summary = when (current.type) {
        PendingAudioDeletionType.Source -> stringResource(Res.string.audio_delete_source_summary)
        PendingAudioDeletionType.LocalDatabase -> stringResource(Res.string.audio_delete_local_summary)
    }
    val message = when (current.type) {
        PendingAudioDeletionType.Source -> "确认删除「${current.source?.name.orEmpty()}」吗？"
        PendingAudioDeletionType.LocalDatabase -> "确认删除本地音频数据库 android.db 吗？"
    }

    OverlayDialog(
        title = title,
        summary = summary,
        show = true,
        onDismissRequest = onDismiss,
        onDismissFinished = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = message,
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

private fun audioSourceTitle(source: AudioSource): String = when {
    source.isLocal -> "Local"
    else -> source.name
}

@Composable
private fun audioSourceSummary(source: AudioSource): String = when {
    source.isLocal -> stringResource(Res.string.audio_local_summary)
    else -> source.url
}

@Composable
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return stringResource(Res.string.audio_local_not_imported)
    val units = listOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    return if (unitIndex == 0) {
        "${bytes} B"
    } else {
        "${(value * 10).toInt() / 10.0} ${units[unitIndex]}"
    }
}
