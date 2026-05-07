package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import app.mori.reader.core.platform.rememberLocalAudioDatabasePicker
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.AudioSource
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.features.settings.presentation.SettingsUiState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.audio_local_description
import app.mori.reader.shared.generated.resources.audio_local_title
import app.mori.reader.shared.generated.resources.audio_playback_title
import app.mori.reader.shared.generated.resources.audio_source_title
import app.mori.reader.shared.generated.resources.audio_title
import app.mori.reader.shared.generated.resources.cd_add_source
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.rememberReorderableLazyListState
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun AudioSettingsPage(
    settings: AppSettings,
    settingsUi: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    val launchDbPicker =
        rememberLocalAudioDatabasePicker { uri ->
            onIntent(SettingsIntent.ImportLocalAudioDatabase(uri))
        }
    val isImportingLocalAudio = settingsUi.isImportingLocalAudio
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

    val reorderableState =
        rememberReorderableLazyListState(listState) { from, to ->
            val listStartIndex = 4
            val fromRelative = from.index - listStartIndex
            val toRelative = to.index - listStartIndex
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

    MoriPageScaffold(
        title = stringResource(Res.string.audio_title),
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier =
                    Modifier
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
                modifier =
                    Modifier
                        .fillMaxSize()
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding =
                    PaddingValues(
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
                        PlaybackCard(settings = settings, onIntent = onIntent)
                    }
                }
                item {
                    SmallTitle(text = stringResource(Res.string.audio_source_title))
                }
                audioSourceItems(
                    sources = localSources,
                    localAudioImported = settings.audio.localAudioDatabaseSizeBytes > 0L,
                    reorderableState = reorderableState,
                    onIntent = onIntent,
                    onEdit = { source -> editingSource = source },
                    onDeleteRequest = { source ->
                        pendingDeletion =
                            PendingAudioDeletion(
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
                        colors =
                            CardDefaults.defaultColors(
                                color = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                contentColor = MiuixTheme.colorScheme.onSurface,
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
                            settings = settings,
                            onImport = launchDbPicker,
                            isImporting = isImportingLocalAudio,
                            onDeleteRequest = {
                                pendingDeletion =
                                    PendingAudioDeletion(
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
                    onIntent(SettingsIntent.AddAudioSource(name, url))
                    showAddSourceDialog = false
                },
            )
            EditAudioSourceDialog(
                source = editingSource,
                onDismiss = { editingSource = null },
                onConfirm = { source, name, url ->
                    onIntent(SettingsIntent.UpdateAudioSource(source.url, name, url))
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
    }
}
