package app.mori.reader.ui.pages.settings.audio

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import app.mori.reader.core.platform.rememberLocalAudioDatabasePicker
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.AudioSource
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.features.settings.presentation.SettingsUiState
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

internal class AudioSettingsController(
    val isImportingLocalAudio: Boolean,
    val listState: LazyListState,
    val reorderableState: ReorderableLazyListState,
    val localSources: List<AudioSource>,
    val showAddSourceDialog: Boolean,
    val editingSource: AudioSource?,
    val pendingDeletion: PendingAudioDeletion?,
    val launchLocalAudioDatabasePicker: () -> Unit,
    val requestAddSource: () -> Unit,
    val dismissAddSourceDialog: () -> Unit,
    val requestEditSource: (AudioSource) -> Unit,
    val dismissEditSourceDialog: () -> Unit,
    val requestDeleteSource: (AudioSource) -> Unit,
    val requestDeleteLocalDatabase: () -> Unit,
    val dismissDeleteDialog: () -> Unit,
    val confirmAddSource: (String, String) -> Unit,
    val confirmEditSource: (AudioSource, String, String) -> Unit,
    val confirmDeletion: (PendingAudioDeletion) -> Unit,
)

@Composable
internal fun rememberAudioSettingsController(
    settings: AppSettings,
    settingsUiState: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
): AudioSettingsController {
    val launchDbPicker =
        rememberLocalAudioDatabasePicker { uri ->
            onIntent(SettingsIntent.ImportLocalAudioDatabase(uri))
        }
    val isImportingLocalAudio = settingsUiState.isImportingLocalAudio
    val listState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    val onIntentState by rememberUpdatedState(onIntent)
    var showAddSourceDialog by remember { mutableStateOf(false) }
    var editingSource by remember { mutableStateOf<AudioSource?>(null) }
    var localSources by remember { mutableStateOf(settings.audio.sources) }
    var pendingDeletion by remember { mutableStateOf<PendingAudioDeletion?>(null) }

    LaunchedEffect(settings.audio.sources) {
        localSources = settings.audio.sources
    }

    val audioSourceListStartIndex = remember { 2 }
    val reorderableState =
        rememberReorderableLazyListState(listState) { from, to ->
            val fromRelative = from.index - audioSourceListStartIndex
            val toRelative = to.index - audioSourceListStartIndex
            if (
                fromRelative !in localSources.indices ||
                toRelative !in localSources.indices
            ) {
                return@rememberReorderableLazyListState
            }

            localSources =
                localSources.toMutableList().apply {
                    add(toRelative, removeAt(fromRelative))
                }
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }

    LaunchedEffect(reorderableState.isAnyItemDragging, settings.audio.sources, localSources) {
        if (!reorderableState.isAnyItemDragging) {
            val updatedUrls = localSources.map(AudioSource::url)
            if (updatedUrls != settings.audio.sources.map(AudioSource::url)) {
                onIntentState(SettingsIntent.ReorderAudioSources(updatedUrls))
            }
        }
    }

    return AudioSettingsController(
        isImportingLocalAudio = isImportingLocalAudio,
        listState = listState,
        reorderableState = reorderableState,
        localSources = localSources,
        showAddSourceDialog = showAddSourceDialog,
        editingSource = editingSource,
        pendingDeletion = pendingDeletion,
        launchLocalAudioDatabasePicker = launchDbPicker,
        requestAddSource = {
            if (!isImportingLocalAudio) {
                showAddSourceDialog = true
            }
        },
        dismissAddSourceDialog = { showAddSourceDialog = false },
        requestEditSource = { source -> editingSource = source },
        dismissEditSourceDialog = { editingSource = null },
        requestDeleteSource = { source ->
            pendingDeletion =
                PendingAudioDeletion(
                    type = PendingAudioDeletionType.Source,
                    source = source,
                )
        },
        requestDeleteLocalDatabase = {
            pendingDeletion =
                PendingAudioDeletion(
                    type = PendingAudioDeletionType.LocalDatabase,
                )
        },
        dismissDeleteDialog = { pendingDeletion = null },
        confirmAddSource = { name, url ->
            onIntent(SettingsIntent.AddAudioSource(name, url))
            showAddSourceDialog = false
        },
        confirmEditSource = { source, name, url ->
            onIntent(SettingsIntent.UpdateAudioSource(source.url, name, url))
            editingSource = null
        },
        confirmDeletion = { deletion ->
            when (deletion.type) {
                PendingAudioDeletionType.Source -> {
                    deletion.source?.let { source ->
                        onIntent(SettingsIntent.DeleteAudioSource(source.url))
                    }
                }

                PendingAudioDeletionType.LocalDatabase -> {
                    onIntent(SettingsIntent.DeleteLocalAudioDatabase)
                }
            }
            pendingDeletion = null
        },
    )
}
