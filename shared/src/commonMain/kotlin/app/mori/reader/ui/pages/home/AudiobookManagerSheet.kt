package app.mori.reader.ui.pages.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mori.reader.core.platform.rememberAudiobookAudioPicker
import app.mori.reader.core.platform.rememberAudiobookSubtitlePicker
import app.mori.reader.data.audiobook.AudiobookAssetInfo
import app.mori.reader.data.audiobook.AudiobookAssetType
import app.mori.reader.data.audiobook.AudiobookStorageMode
import app.mori.reader.data.book.BookInfo
import app.mori.reader.features.audiobook.presentation.AudiobookIntent
import app.mori.reader.features.audiobook.presentation.AudiobookUiState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.audiobook_audio_title
import app.mori.reader.shared.generated.resources.audiobook_character_unit
import app.mori.reader.shared.generated.resources.audiobook_delete
import app.mori.reader.shared.generated.resources.audiobook_delete_asset_confirm
import app.mori.reader.shared.generated.resources.audiobook_delete_match_confirm
import app.mori.reader.shared.generated.resources.audiobook_description
import app.mori.reader.shared.generated.resources.audiobook_import_audio
import app.mori.reader.shared.generated.resources.audiobook_import_subtitle
import app.mori.reader.shared.generated.resources.audiobook_match_delete
import app.mori.reader.shared.generated.resources.audiobook_match_rematch
import app.mori.reader.shared.generated.resources.audiobook_match_start
import app.mori.reader.shared.generated.resources.audiobook_match_title
import app.mori.reader.shared.generated.resources.audiobook_match_unmatched
import app.mori.reader.shared.generated.resources.audiobook_missing
import app.mori.reader.shared.generated.resources.audiobook_reimport
import app.mori.reader.shared.generated.resources.audiobook_rematch_confirm
import app.mori.reader.shared.generated.resources.audiobook_replace_asset_confirm
import app.mori.reader.shared.generated.resources.audiobook_search_window
import app.mori.reader.shared.generated.resources.audiobook_status_import_subtitle_first
import app.mori.reader.shared.generated.resources.audiobook_status_matched
import app.mori.reader.shared.generated.resources.audiobook_status_matching
import app.mori.reader.shared.generated.resources.audiobook_status_not_matched
import app.mori.reader.shared.generated.resources.audiobook_storage_copy
import app.mori.reader.shared.generated.resources.audiobook_storage_reference
import app.mori.reader.shared.generated.resources.audiobook_storage_title
import app.mori.reader.shared.generated.resources.audiobook_subtitle_cue_count
import app.mori.reader.shared.generated.resources.audiobook_subtitle_parsed
import app.mori.reader.shared.generated.resources.audiobook_subtitle_storage_note
import app.mori.reader.shared.generated.resources.audiobook_subtitle_title
import app.mori.reader.shared.generated.resources.audiobook_title
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_confirm
import app.mori.reader.shared.generated.resources.btn_delete
import app.mori.reader.ui.components.settings.SettingSlider
import app.mori.reader.ui.text.asString
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

enum class PendingAudiobookAction {
    ImportAudio,
    ImportSubtitle,
    DeleteAudio,
    DeleteSubtitle,
    Rematch,
    DeleteMatch,
}

@Composable
fun AudiobookManagerSheet(
    book: BookInfo?,
    audiobook: AudiobookUiState,
    onAudiobookIntent: (AudiobookIntent) -> Unit,
    onDismiss: () -> Unit,
) {
    val current = book
    var pendingAction by remember(current?.id) { mutableStateOf<PendingAudiobookAction?>(null) }
    val audioPicker =
        rememberAudiobookAudioPicker { uri ->
            current?.let { onAudiobookIntent(AudiobookIntent.ImportAudiobookAudio(it.id, uri)) }
        }
    val subtitlePicker =
        rememberAudiobookSubtitlePicker { uri ->
            current?.let { onAudiobookIntent(AudiobookIntent.ImportAudiobookSubtitle(it.id, uri)) }
        }

    WindowBottomSheet(
        show = current != null,
        title = current?.title?.let { "$it ${stringResource(Res.string.audiobook_title)}" },
        onDismissRequest = onDismiss,
    ) {
        if (current == null) return@WindowBottomSheet
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.defaultColors(
                            color = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.24f),
                            contentColor = MiuixTheme.colorScheme.onSurface,
                        ),
                    insideMargin = PaddingValues(16.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.audiobook_description),
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    val modes = AudiobookStorageMode.entries
                    OverlaySpinnerPreference(
                        items =
                            modes.map { mode ->
                                SpinnerEntry(
                                    title =
                                        when (mode) {
                                            AudiobookStorageMode.Copy -> stringResource(Res.string.audiobook_storage_copy)
                                            AudiobookStorageMode.Reference -> stringResource(Res.string.audiobook_storage_reference)
                                        },
                                )
                            },
                        selectedIndex =
                            modes
                                .indexOf(audiobook.preferredStorageMode)
                                .coerceAtLeast(0),
                        title = stringResource(Res.string.audiobook_storage_title),
                        summary = stringResource(Res.string.audiobook_subtitle_storage_note),
                        onSelectedIndexChange = { index ->
                            onAudiobookIntent(AudiobookIntent.SetAudiobookStorageMode(modes[index]))
                        },
                    )
                }
            }
            audiobook.errorMessage?.let { error ->
                item {
                    ErrorCard(
                        message = error.asString(),
                        onDismiss = { onAudiobookIntent(AudiobookIntent.DismissAudiobookError) },
                    )
                }
            }
            item {
                AudiobookAssetCard(
                    title = stringResource(Res.string.audiobook_audio_title),
                    asset = audiobook.audioAssetInfo,
                    isImporting = audiobook.isImportingAudio,
                    primaryText = stringResource(Res.string.audiobook_import_audio),
                    details = audiobook.audioAssetInfo?.audioDetails(),
                    onImport = {
                        if (audiobook.audioAssetInfo == null) {
                            audioPicker()
                        } else {
                            pendingAction =
                                PendingAudiobookAction.ImportAudio
                        }
                    },
                    onDelete = {
                        pendingAction = PendingAudiobookAction.DeleteAudio
                    },
                )
            }
            item {
                AudiobookAssetCard(
                    title = stringResource(Res.string.audiobook_subtitle_title),
                    asset = audiobook.subtitleAssetInfo,
                    isImporting = audiobook.isImportingSubtitle,
                    primaryText = stringResource(Res.string.audiobook_import_subtitle),
                    details = audiobook.subtitleAssetInfo?.subtitleDetails(audiobook.subtitleData?.cues?.size),
                    parsedText =
                        audiobook.subtitleData?.let {
                            "${stringResource(Res.string.audiobook_subtitle_parsed)} · " +
                                stringResource(
                                    Res.string.audiobook_subtitle_cue_count,
                                    it.cues.size,
                                )
                        },
                    onImport = {
                        if (audiobook.subtitleAssetInfo == null) {
                            subtitlePicker()
                        } else {
                            pendingAction =
                                PendingAudiobookAction.ImportSubtitle
                        }
                    },
                    onDelete = {
                        pendingAction = PendingAudiobookAction.DeleteSubtitle
                    },
                )
            }
            item {
                AudiobookMatchCard(
                    audiobook = audiobook,
                    onSearchWindowChange = {
                        onAudiobookIntent(
                            AudiobookIntent.SetAudiobookSearchWindow(
                                it,
                            ),
                        )
                    },
                    onMatch = {
                        if (audiobook.matchData == null) {
                            onAudiobookIntent(
                                AudiobookIntent.RunAudiobookMatch(
                                    current.id,
                                    audiobook.searchWindow,
                                ),
                            )
                        } else {
                            pendingAction = PendingAudiobookAction.Rematch
                        }
                    },
                    onDelete = {
                        pendingAction = PendingAudiobookAction.DeleteMatch
                    },
                )
            }
        }
    }

    AudiobookConfirmDialog(
        action = pendingAction,
        onDismiss = { pendingAction = null },
        onConfirm = { action ->
            val bookId = current?.id
            when (action) {
                PendingAudiobookAction.ImportAudio -> {
                    audioPicker()
                }

                PendingAudiobookAction.ImportSubtitle -> {
                    subtitlePicker()
                }

                PendingAudiobookAction.DeleteAudio -> {
                    if (bookId != null) {
                        onAudiobookIntent(
                            AudiobookIntent.DeleteAudiobookAsset(
                                bookId,
                                AudiobookAssetType.Audio,
                            ),
                        )
                    }
                }

                PendingAudiobookAction.DeleteSubtitle -> {
                    if (bookId != null) {
                        onAudiobookIntent(
                            AudiobookIntent.DeleteAudiobookAsset(
                                bookId,
                                AudiobookAssetType.Subtitle,
                            ),
                        )
                    }
                }

                PendingAudiobookAction.Rematch -> {
                    if (bookId != null) {
                        onAudiobookIntent(
                            AudiobookIntent.RunAudiobookMatch(
                                bookId,
                                audiobook.searchWindow,
                            ),
                        )
                    }
                }

                PendingAudiobookAction.DeleteMatch -> {
                    if (bookId != null) {
                        onAudiobookIntent(AudiobookIntent.DeleteAudiobookMatch(bookId))
                    }
                }
            }
            pendingAction = null
        },
    )
}

@Composable
fun AudiobookMatchCard(
    audiobook: AudiobookUiState,
    onSearchWindowChange: (Int) -> Unit,
    onMatch: () -> Unit,
    onDelete: () -> Unit,
) {
    val subtitleReady = audiobook.subtitleData != null
    val matchData = audiobook.matchData
    val matched = matchData?.matches?.size ?: 0
    val total =
        matchData?.let { it.matches.size + it.unmatched } ?: (
            audiobook.subtitleData?.cues?.size
                ?: 0
        )
    val characterUnit = stringResource(Res.string.audiobook_character_unit)
    val status =
        when {
            !subtitleReady -> {
                stringResource(Res.string.audiobook_status_import_subtitle_first)
            }

            audiobook.isMatching -> {
                stringResource(Res.string.audiobook_status_matching)
            }

            matchData == null -> {
                stringResource(Res.string.audiobook_status_not_matched)
            }

            else -> {
                stringResource(
                    Res.string.audiobook_status_matched,
                    formatMatchRate(matched, total),
                )
            }
        }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.audiobook_match_title),
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = status,
                        color =
                            if (subtitleReady) {
                                MiuixTheme.colorScheme.onSurfaceVariantSummary
                            } else {
                                MiuixTheme.colorScheme.disabledOnSecondaryVariant
                            },
                    )
                    if (matchData != null) {
                        Text(
                            text =
                                stringResource(
                                    Res.string.audiobook_match_unmatched,
                                    matchData.unmatched,
                                    matchData.searchWindow,
                                ),
                            fontSize = 13.sp,
                            color = MiuixTheme.colorScheme.primary,
                        )
                    }
                }
                TextButton(
                    text =
                        if (matchData == null) {
                            stringResource(Res.string.audiobook_match_start)
                        } else {
                            stringResource(Res.string.audiobook_match_rematch)
                        },
                    enabled = subtitleReady && !audiobook.isMatching,
                    onClick = onMatch,
                )
            }
            if (subtitleReady) {
                SettingSlider(
                    label = stringResource(Res.string.audiobook_search_window),
                    value = audiobook.searchWindow.toFloat(),
                    range = 50f..350f,
                    steps = 11,
                    keyPoints = listOf(50f, 200f, 350f),
                    valueText = { "${it.toInt()} $characterUnit" },
                    onCommit = { value ->
                        val stepped = ((value.toInt() + 12) / 25 * 25).coerceIn(50, 350)
                        onSearchWindowChange(stepped)
                    },
                )
            }
            if (audiobook.isMatching) {
                LinearProgressIndicator()
            }
            if (matchData != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        text = stringResource(Res.string.audiobook_match_delete),
                        enabled = !audiobook.isMatching,
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        }
    }
}

@Composable
fun AudiobookAssetCard(
    title: String,
    asset: AudiobookAssetInfo?,
    isImporting: Boolean,
    primaryText: String,
    details: String?,
    parsedText: String? = null,
    onImport: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = asset?.displayName ?: stringResource(Res.string.audiobook_missing),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    details?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    parsedText?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = MiuixTheme.colorScheme.primary,
                        )
                    }
                }
                TextButton(
                    text = if (asset == null) primaryText else stringResource(Res.string.audiobook_reimport),
                    enabled = !isImporting,
                    onClick = onImport,
                )
            }
            if (isImporting) {
                LinearProgressIndicator()
            }
            if (asset != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        text = stringResource(Res.string.audiobook_delete),
                        enabled = !isImporting,
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        }
    }
}

@Composable
fun AudiobookConfirmDialog(
    action: PendingAudiobookAction?,
    onDismiss: () -> Unit,
    onConfirm: (PendingAudiobookAction) -> Unit,
) {
    val current = action ?: return
    val isDelete =
        current == PendingAudiobookAction.DeleteAudio ||
            current == PendingAudiobookAction.DeleteSubtitle ||
            current == PendingAudiobookAction.DeleteMatch
    WindowDialog(
        title =
            when {
                current == PendingAudiobookAction.Rematch -> stringResource(Res.string.audiobook_match_rematch)
                isDelete -> stringResource(Res.string.audiobook_delete)
                else -> stringResource(Res.string.audiobook_reimport)
            },
        show = true,
        onDismissRequest = onDismiss,
    ) {
        Text(
            text =
                when {
                    current == PendingAudiobookAction.Rematch -> stringResource(Res.string.audiobook_rematch_confirm)
                    current == PendingAudiobookAction.DeleteMatch -> stringResource(Res.string.audiobook_delete_match_confirm)
                    isDelete -> stringResource(Res.string.audiobook_delete_asset_confirm)
                    else -> stringResource(Res.string.audiobook_replace_asset_confirm)
                },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(
                text = stringResource(Res.string.btn_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(20.dp))
            TextButton(
                text = if (isDelete) stringResource(Res.string.btn_delete) else stringResource(Res.string.btn_confirm),
                onClick = { onConfirm(current) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}
