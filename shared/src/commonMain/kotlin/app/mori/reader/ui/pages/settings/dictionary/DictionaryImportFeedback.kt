package app.mori.reader.ui.pages.settings.dictionary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mori.reader.data.dictionary.DictionaryImportFailureReason
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.settings.presentation.DictionaryImportFailureItem
import app.mori.reader.features.settings.presentation.DictionaryImportSummary
import app.mori.reader.features.settings.presentation.DictionaryImportUiProgress
import app.mori.reader.features.settings.presentation.DictionaryManagementState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_close
import app.mori.reader.shared.generated.resources.dict_import_failed_all_message
import app.mori.reader.shared.generated.resources.dict_import_failed_all_title
import app.mori.reader.shared.generated.resources.dict_import_failed_partial_message
import app.mori.reader.shared.generated.resources.dict_import_failed_partial_title
import app.mori.reader.shared.generated.resources.dict_import_progress
import app.mori.reader.shared.generated.resources.dict_import_reason_corrupted
import app.mori.reader.shared.generated.resources.dict_import_reason_label
import app.mori.reader.shared.generated.resources.dict_import_reason_unknown
import app.mori.reader.shared.generated.resources.dict_import_reason_unreadable
import app.mori.reader.shared.generated.resources.dict_import_reason_unsupported
import app.mori.reader.shared.generated.resources.toast_dict_importing
import app.mori.reader.ui.components.loading.MoriLoadingDialog
import app.mori.reader.ui.components.loading.MoriLoadingDialogState
import app.mori.reader.ui.theme.MoriTheme
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextButton as MiuixTextButton

@Composable
internal fun DictionaryImportFeedback(
    dictionaryState: DictionaryManagementState,
    onDismissSummary: () -> Unit,
) {
    DictionaryImportLoadingDialog(dictionaryState = dictionaryState)
    DictionaryImportResultDialog(
        summary = dictionaryState.importSummary,
        onDismiss = onDismissSummary,
    )
}

@Composable
private fun DictionaryImportLoadingDialog(dictionaryState: DictionaryManagementState) {
    val progress = dictionaryState.importProgress
    MoriLoadingDialog(
        show = dictionaryState.isImporting,
        state =
            MoriLoadingDialogState(
                title = stringResource(Res.string.toast_dict_importing),
                message =
                    progress?.let {
                        stringResource(
                            Res.string.dict_import_progress,
                            it.currentIndex,
                            it.totalCount,
                        )
                    },
                fraction = progress?.fractionOrNull(),
            ),
        onCancel = null,
    )
}

@Composable
private fun DictionaryImportResultDialog(
    summary: DictionaryImportSummary?,
    onDismiss: () -> Unit,
) {
    val current = summary ?: return
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Material -> MaterialDictionaryImportResultDialog(summary = current, onDismiss = onDismiss)
        UiThemeEngine.Miuix -> MiuixDictionaryImportResultDialog(summary = current, onDismiss = onDismiss)
    }
}

@Composable
private fun MaterialDictionaryImportResultDialog(
    summary: DictionaryImportSummary,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = summary.title()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = summary.message(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(summary.failures) { failure ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = failure.fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = stringResource(Res.string.dict_import_reason_label, failure.reasonText()),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.btn_close))
            }
        },
    )
}

@Composable
private fun MiuixDictionaryImportResultDialog(
    summary: DictionaryImportSummary,
    onDismiss: () -> Unit,
) {
    WindowDialog(
        title = summary.title(),
        summary = summary.message(),
        show = true,
        onDismissRequest = onDismiss,
        onDismissFinished = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            summary.failures.forEach { failure ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiuixText(
                        text = failure.fileName,
                        color = MiuixTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                    )
                    MiuixText(
                        text = stringResource(Res.string.dict_import_reason_label, failure.reasonText()),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
            MiuixTextButton(
                text = stringResource(Res.string.btn_close),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

@Composable
private fun DictionaryImportSummary.title(): String =
    if (successCount > 0) {
        stringResource(Res.string.dict_import_failed_partial_title)
    } else {
        stringResource(Res.string.dict_import_failed_all_title)
    }

@Composable
private fun DictionaryImportSummary.message(): String =
    if (successCount > 0) {
        stringResource(Res.string.dict_import_failed_partial_message)
    } else {
        stringResource(Res.string.dict_import_failed_all_message)
    }

@Composable
private fun DictionaryImportFailureItem.reasonText(): String =
    when (reason) {
        DictionaryImportFailureReason.UnsupportedFile -> stringResource(Res.string.dict_import_reason_unsupported)
        DictionaryImportFailureReason.CorruptedFile -> stringResource(Res.string.dict_import_reason_corrupted)
        DictionaryImportFailureReason.UnreadableFile -> stringResource(Res.string.dict_import_reason_unreadable)
        DictionaryImportFailureReason.Unknown -> stringResource(Res.string.dict_import_reason_unknown)
    }

private fun DictionaryImportUiProgress.fractionOrNull(): Float? =
    if (totalCount > 1) {
        currentIndex.toFloat() / totalCount.toFloat()
    } else {
        null
    }
