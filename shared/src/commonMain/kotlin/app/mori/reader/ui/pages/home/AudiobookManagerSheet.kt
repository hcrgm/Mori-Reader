package app.mori.reader.ui.pages.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.core.platform.PickedDocument
import app.mori.reader.core.platform.rememberAudiobookAudioPicker
import app.mori.reader.core.platform.rememberAudiobookSubtitlePicker
import app.mori.reader.data.audiobook.AudiobookAssetInfo
import app.mori.reader.data.audiobook.AudiobookStorageMode
import app.mori.reader.data.book.BookInfo
import app.mori.reader.features.audiobook.presentation.AudiobookIntent
import app.mori.reader.features.audiobook.presentation.AudiobookUiState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.audiobook_audio_section
import app.mori.reader.shared.generated.resources.audiobook_audio_title
import app.mori.reader.shared.generated.resources.audiobook_change
import app.mori.reader.shared.generated.resources.audiobook_choose
import app.mori.reader.shared.generated.resources.audiobook_import_title
import app.mori.reader.shared.generated.resources.audiobook_importing
import app.mori.reader.shared.generated.resources.audiobook_matched_full
import app.mori.reader.shared.generated.resources.audiobook_no_audio_selected
import app.mori.reader.shared.generated.resources.audiobook_no_subtitle_selected
import app.mori.reader.shared.generated.resources.audiobook_progress_audio
import app.mori.reader.shared.generated.resources.audiobook_progress_subtitle_match
import app.mori.reader.shared.generated.resources.audiobook_remove
import app.mori.reader.shared.generated.resources.audiobook_remove_confirm_message
import app.mori.reader.shared.generated.resources.audiobook_remove_confirm_title
import app.mori.reader.shared.generated.resources.audiobook_search_window
import app.mori.reader.shared.generated.resources.audiobook_storage_copy
import app.mori.reader.shared.generated.resources.audiobook_storage_reference
import app.mori.reader.shared.generated.resources.audiobook_storage_title
import app.mori.reader.shared.generated.resources.audiobook_subtitle_section
import app.mori.reader.shared.generated.resources.audiobook_subtitle_title
import app.mori.reader.shared.generated.resources.audiobook_title
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_close
import app.mori.reader.shared.generated.resources.btn_confirm
import app.mori.reader.shared.generated.resources.btn_import
import app.mori.reader.ui.components.settings.MoriSettingsHorizontalPadding
import app.mori.reader.ui.components.settings.MoriWarningCard
import app.mori.reader.ui.text.asString
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults.SliderHapticEffect
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.math.roundToInt

private enum class PendingAudiobookAction {
    Remove,
}

@Composable
fun AudiobookManagerSheet(
    book: BookInfo?,
    audiobook: AudiobookUiState,
    onAudiobookIntent: (AudiobookIntent) -> Unit,
    onDismiss: () -> Unit,
) {
    val current = book
    val importing = audiobook.isImportingAudio || audiobook.isImportingSubtitle || audiobook.isMatching
    val imported = audiobook.audioAssetInfo != null && audiobook.subtitleAssetInfo != null && audiobook.matchData != null
    var pendingAction by remember(current?.id) { mutableStateOf<PendingAudiobookAction?>(null) }
    var pickedAudio by remember(current?.id) { mutableStateOf<PickedDocument?>(null) }
    var pickedSubtitle by remember(current?.id) { mutableStateOf<PickedDocument?>(null) }
    val audioPicker =
        rememberAudiobookAudioPicker { file ->
            pickedAudio = file
        }
    val subtitlePicker =
        rememberAudiobookSubtitlePicker { file ->
            pickedSubtitle = file
        }

    LaunchedEffect(current?.id, audiobook.isOpen) {
        if (!audiobook.isOpen) {
            pickedAudio = null
            pickedSubtitle = null
        }
    }

    WindowBottomSheet(
        show = current != null && audiobook.isOpen && !audiobook.isLoadingAssets && !imported,
        title = stringResource(Res.string.audiobook_import_title),
        onDismissRequest = {
            if (!importing) onDismiss()
        },
        allowDismiss = !importing,
        dragHandleColor =
            MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(
                alpha = if (importing) 0.05f else 0.2f,
            ),
    ) {
        if (current == null) return@WindowBottomSheet
        ImportAudiobookSheetContent(
            audiobook = audiobook,
            pickedAudio = pickedAudio,
            pickedSubtitle = pickedSubtitle,
            storageMode = audiobook.preferredStorageMode,
            importing = importing,
            onPickAudio = {
                if (!importing) audioPicker()
            },
            onPickSubtitle = {
                if (!importing) subtitlePicker()
            },
            onSelectStorageMode = { mode ->
                if (!importing) {
                    onAudiobookIntent(AudiobookIntent.SetAudiobookStorageMode(mode))
                }
            },
            onSelectSearchWindow = { value ->
                if (!importing) {
                    onAudiobookIntent(AudiobookIntent.SetAudiobookSearchWindow(value))
                }
            },
            onCancel = onDismiss,
            onImport = {
                val audio = pickedAudio
                val subtitle = pickedSubtitle
                if (audio != null && subtitle != null) {
                    onAudiobookIntent(
                        AudiobookIntent.ImportAudiobook(
                            bookId = current.id,
                            audioUriString = audio.uriString,
                            subtitleUriString = subtitle.uriString,
                            searchWindow = audiobook.searchWindow,
                        ),
                    )
                }
            },
        )
    }

    WindowBottomSheet(
        show = current != null && audiobook.isOpen && !audiobook.isLoadingAssets && imported,
        title = stringResource(Res.string.audiobook_title),
        onDismissRequest = onDismiss,
    ) {
        ImportedAudiobookSheetContent(
            audiobook = audiobook,
            onClose = onDismiss,
            onRemove = { pendingAction = PendingAudiobookAction.Remove },
        )
    }

    current?.let { selectedBook ->
        RemoveAudiobookDialog(
            action = pendingAction,
            onDismiss = { pendingAction = null },
            onConfirm = {
                pendingAction = null
                onAudiobookIntent(AudiobookIntent.RemoveAudiobook(selectedBook.id))
            },
        )
    }
}

@Composable
private fun ImportAudiobookSheetContent(
    audiobook: AudiobookUiState,
    pickedAudio: PickedDocument?,
    pickedSubtitle: PickedDocument?,
    storageMode: AudiobookStorageMode,
    importing: Boolean,
    onPickAudio: () -> Unit,
    onPickSubtitle: () -> Unit,
    onSelectStorageMode: (AudiobookStorageMode) -> Unit,
    onSelectSearchWindow: (Int) -> Unit,
    onCancel: () -> Unit,
    onImport: () -> Unit,
) {
    val importEnabled = pickedAudio != null && pickedSubtitle != null && !importing
    val storageModes = remember { AudiobookStorageMode.entries.toList() }
    val storageModeItems =
        listOf(
            SpinnerEntry(title = stringResource(Res.string.audiobook_storage_copy)),
            SpinnerEntry(title = stringResource(Res.string.audiobook_storage_reference)),
        )
    LazyColumn(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        audiobook.errorMessage?.let { error ->
            item {
                MoriWarningCard(text = error.asString())
            }
        }
        item {
            SheetSectionLabel(text = stringResource(Res.string.audiobook_audio_section))
            FilePickerComponent(
                title = pickedAudio?.displayName ?: stringResource(Res.string.audiobook_no_audio_selected),
                summary = pickedAudio?.let { stringResource(Res.string.audiobook_audio_title) },
                actionText =
                    if (pickedAudio == null) {
                        stringResource(Res.string.audiobook_choose)
                    } else {
                        stringResource(Res.string.audiobook_change)
                    },
                icon = MiuixIcons.Music,
                enabled = !importing,
                onClick = onPickAudio,
            )
        }
        item {
            AnimatedVisibility(
                visible = pickedAudio != null,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
            ) {
                WindowSpinnerPreference(
                    items = storageModeItems,
                    selectedIndex = storageModes.indexOf(storageMode).coerceAtLeast(0),
                    title = stringResource(Res.string.audiobook_storage_title),
                    summary =
                        when (storageMode) {
                            AudiobookStorageMode.Copy -> stringResource(Res.string.audiobook_storage_copy)
                            AudiobookStorageMode.Reference -> stringResource(Res.string.audiobook_storage_reference)
                        },
                    enabled = !importing,
                    startAction = {
                        SheetIconBox(icon = MiuixIcons.Folder, tint = MiuixTheme.colorScheme.primary)
                    },
                    onSelectedIndexChange = { index ->
                        onSelectStorageMode(storageModes[index])
                    },
                )
            }
        }
        item {
            SheetSectionLabel(text = stringResource(Res.string.audiobook_subtitle_section))
            FilePickerComponent(
                title = pickedSubtitle?.displayName ?: stringResource(Res.string.audiobook_no_subtitle_selected),
                summary = pickedSubtitle?.let { stringResource(Res.string.audiobook_subtitle_title) },
                actionText =
                    if (pickedSubtitle == null) {
                        stringResource(Res.string.audiobook_choose)
                    } else {
                        stringResource(Res.string.audiobook_change)
                    },
                icon = MiuixIcons.Translate,
                enabled = !importing,
                onClick = onPickSubtitle,
            )
        }
        item {
            AnimatedVisibility(
                visible = pickedSubtitle != null,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
            ) {
                SearchWindowPreference(
                    value = audiobook.searchWindow,
                    enabled = !importing,
                    onValueChange = onSelectSearchWindow,
                )
            }
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                AnimatedVisibility(visible = importing) {
                    ImportProgressIndicator(audiobook = audiobook)
                }
                SheetActionRow(
                    secondaryText = stringResource(Res.string.btn_cancel),
                    primaryText =
                        if (importing) {
                            stringResource(Res.string.audiobook_importing)
                        } else {
                            stringResource(Res.string.btn_import)
                        },
                    primaryEnabled = importEnabled,
                    secondaryEnabled = !importing,
                    onSecondary = onCancel,
                    onPrimary = onImport,
                )
            }
        }
    }
}

@Composable
private fun ImportedAudiobookSheetContent(
    audiobook: AudiobookUiState,
    onClose: () -> Unit,
    onRemove: () -> Unit,
) {
    val audio = audiobook.audioAssetInfo
    val subtitle = audiobook.subtitleAssetInfo
    val matchData = audiobook.matchData
    val matched = matchData?.matches?.size ?: 0
    val total = matched + (matchData?.unmatched ?: 0)
    LazyColumn(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (audio != null) {
                    AssetInfoComponent(
                        asset = audio,
                        summary = audio.audioDetails(),
                        icon = MiuixIcons.Music,
                    )
                }
                if (subtitle != null) {
                    AssetInfoComponent(
                        asset = subtitle,
                        summary = subtitle.subtitleDetails(audiobook.subtitleData?.cues?.size),
                        icon = MiuixIcons.Translate,
                    )
                }
            }
        }
        item {
            MatchInfoCard(matchRate = formatMatchRate(matched, total))
        }
        item {
            SheetActionRow(
                secondaryText = stringResource(Res.string.btn_close),
                primaryText = stringResource(Res.string.audiobook_remove),
                primaryEnabled = true,
                secondaryEnabled = true,
                primaryDanger = true,
                onSecondary = onClose,
                onPrimary = onRemove,
            )
        }
    }
}

@Composable
private fun FilePickerComponent(
    title: String,
    summary: String?,
    actionText: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        insideMargin = PaddingValues(0.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.primary.copy(alpha = 0.08f)),
        onClick = if (enabled) onClick else null,
    ) {
        BasicComponent(
            enabled = enabled,
            startAction = {
                SheetIconBox(icon = icon, tint = MiuixTheme.colorScheme.primary)
            },
            endActions = {
                Text(
                    text = actionText,
                    color =
                        if (enabled) {
                            MiuixTheme.colorScheme.primary
                        } else {
                            MiuixTheme.colorScheme.disabledOnSecondaryVariant
                        },
                    fontWeight = FontWeight.Medium,
                )
            },
        ) {
            Text(
                text = title,
                color =
                    if (enabled) {
                        MiuixTheme.colorScheme.onSurface
                    } else {
                        MiuixTheme.colorScheme.disabledOnSecondaryVariant
                    },
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (summary != null) {
                Text(
                    text = summary,
                    color =
                        if (enabled) {
                            MiuixTheme.colorScheme.onSurfaceVariantSummary
                        } else {
                            MiuixTheme.colorScheme.disabledOnSecondaryVariant
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SearchWindowPreference(
    value: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value.quantizeSearchWindow().toFloat()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        insideMargin = PaddingValues(0.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        ArrowPreference(
            title = stringResource(Res.string.audiobook_search_window),
            summary = sliderValue.roundToInt().toString(),
            enabled = enabled,
            startAction = {
                SheetIconBox(icon = MiuixIcons.Search, tint = MiuixTheme.colorScheme.primary)
            },
            bottomAction = {
                Slider(
                    value = sliderValue,
                    onValueChange = {
                        val quantized = it.roundToInt().quantizeSearchWindow()
                        sliderValue = quantized.toFloat()
                        onValueChange(quantized)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                    enabled = enabled,
                    valueRange = 50f..1000f,
                    steps = 18,
                    hapticEffect = SliderHapticEffect.Step,
                    showKeyPoints = true,
                )
            },
        )
    }
}

@Composable
private fun AssetInfoComponent(
    asset: AudiobookAssetInfo,
    summary: String,
    icon: ImageVector,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        insideMargin = PaddingValues(0.dp),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
    ) {
        BasicComponent(
            startAction = {
                SheetIconBox(icon = icon, tint = MiuixTheme.colorScheme.primary)
            },
        ) {
            Text(
                text = asset.displayName,
                color = MiuixTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = summary,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                maxLines = 1,
                style = MiuixTheme.textStyles.body2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ImportProgressIndicator(audiobook: AudiobookUiState) {
    val label =
        if (audiobook.isImportingAudio) {
            stringResource(Res.string.audiobook_progress_audio)
        } else {
            stringResource(Res.string.audiobook_progress_subtitle_match)
        }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(
            text = label,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            fontWeight = FontWeight.Medium,
        )
        LinearProgressIndicator()
    }
}

@Composable
private fun MatchInfoCard(matchRate: String) {
    val success = Color(0xFF19B36B)
    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        insideMargin = PaddingValues(16.dp),
        colors = CardDefaults.defaultColors(color = Color(0xFFEAF9F1)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SheetIconBox(
                icon = MiuixIcons.Ok,
                tint = success,
                background = success.copy(alpha = 0.12f),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.audiobook_matched_full, matchRate),
                    color = Color(0xFF087846),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SheetIconBox(
    icon: ImageVector,
    tint: Color,
    background: Color = MiuixTheme.colorScheme.surface,
) {
    Box(
        modifier =
            Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun SheetSectionLabel(text: String) {
    Text(
        text = text,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MoriSettingsHorizontalPadding, vertical = 4.dp),
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun SheetActionRow(
    secondaryText: String,
    primaryText: String,
    primaryEnabled: Boolean,
    secondaryEnabled: Boolean,
    onSecondary: () -> Unit,
    onPrimary: () -> Unit,
    primaryDanger: Boolean = false,
) {
    Row(
        modifier = Modifier.padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(
            text = secondaryText,
            onClick = onSecondary,
            modifier = Modifier.weight(1f),
            enabled = secondaryEnabled,
        )
        Spacer(Modifier.width(20.dp))
        TextButton(
            text = primaryText,
            onClick = onPrimary,
            modifier = Modifier.weight(1f),
            enabled = primaryEnabled,
            colors =
                if (primaryDanger) {
                    ButtonDefaults.textButtonColors(
                        color = MiuixTheme.colorScheme.errorContainer,
                        textColor = MiuixTheme.colorScheme.error,
                    )
                } else {
                    ButtonDefaults.textButtonColorsPrimary()
                },
        )
    }
}

@Composable
private fun RemoveAudiobookDialog(
    action: PendingAudiobookAction?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (action != PendingAudiobookAction.Remove) return
    WindowDialog(
        title = stringResource(Res.string.audiobook_remove_confirm_title),
        show = true,
        onDismissRequest = onDismiss,
    ) {
        Text(
            text = stringResource(Res.string.audiobook_remove_confirm_message),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(
                text = stringResource(Res.string.btn_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(20.dp))
            TextButton(
                text = stringResource(Res.string.btn_confirm),
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors =
                    ButtonDefaults.textButtonColors(
                        color = MiuixTheme.colorScheme.errorContainer,
                        textColor = MiuixTheme.colorScheme.error,
                    ),
            )
        }
    }
}

private fun Int.quantizeSearchWindow(): Int =
    (this / 50f)
        .roundToInt()
        .coerceIn(1, 20) * 50
