package app.mori.reader.ui.pages.settings.audio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.audio_local_title
import app.mori.reader.shared.generated.resources.audio_playback_title
import app.mori.reader.shared.generated.resources.audio_source_title
import app.mori.reader.shared.generated.resources.audio_title
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MoriSettingsHorizontalPadding
import app.mori.reader.ui.components.settings.MoriSettingsSection
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
internal fun MiuixAudioSettingsPage(
    settings: AppSettings,
    controller: AudioSettingsController,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    MoriPageScaffold(
        title = stringResource(Res.string.audio_title),
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = controller.listState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .overScrollVertical(),
            contentPadding =
                    PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    MoriSettingsSection(title = stringResource(Res.string.audio_playback_title)) {
                        PlaybackCard(settings = settings, onIntent = onIntent)
                    }
                }
                item {
                    SmallTitle(text = stringResource(Res.string.audio_source_title))
                }
                audioSourceItems(
                    sources = controller.localSources,
                    localAudioImported = settings.audio.localAudioDatabaseSizeBytes > 0L,
                    reorderableState = controller.reorderableState,
                    onIntent = onIntent,
                    onEdit = controller.requestEditSource,
                    onDeleteRequest = controller.requestDeleteSource,
                )
                item {
                    AddAudioSourceCard(onClick = controller.requestAddSource)
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallTitle(text = stringResource(Res.string.audio_local_title))
                        LocalAudioDescriptionCard()
                    }
                }
                item {
                    Card(modifier = Modifier.padding(horizontal = MoriSettingsHorizontalPadding)) {
                        LocalAudioCard(
                            settings = settings,
                            onImport = controller.launchLocalAudioDatabasePicker,
                            isImporting = controller.isImportingLocalAudio,
                            onDeleteRequest = controller.requestDeleteLocalDatabase,
                        )
                    }
                }
            }

            AddAudioSourceDialog(
                show = controller.showAddSourceDialog,
                onDismiss = controller.dismissAddSourceDialog,
                onConfirm = controller.confirmAddSource,
            )
            EditAudioSourceDialog(
                source = controller.editingSource,
                onDismiss = controller.dismissEditSourceDialog,
                onConfirm = controller.confirmEditSource,
            )
            DeleteAudioDialog(
                pendingDeletion = controller.pendingDeletion,
                onDismiss = controller.dismissDeleteDialog,
                onConfirm = controller.confirmDeletion,
            )
        }
    }
}
