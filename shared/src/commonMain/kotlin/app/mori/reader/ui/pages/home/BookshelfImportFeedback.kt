package app.mori.reader.ui.pages.home

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
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.bookshelf.presentation.BookImportSummary
import app.mori.reader.features.bookshelf.presentation.BookImportUiProgress
import app.mori.reader.features.bookshelf.presentation.BookshelfState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.book_import_failed_all_message
import app.mori.reader.shared.generated.resources.book_import_failed_all_title
import app.mori.reader.shared.generated.resources.book_import_failed_partial_message
import app.mori.reader.shared.generated.resources.book_import_failed_partial_title
import app.mori.reader.shared.generated.resources.btn_close
import app.mori.reader.shared.generated.resources.home_import_progress
import app.mori.reader.shared.generated.resources.home_importing
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
fun BookshelfImportFeedback(
    home: BookshelfState,
    onDismissSummary: () -> Unit,
) {
    BookshelfImportLoadingDialog(home = home)
    BookshelfImportResultDialog(
        summary = home.importSummary,
        onDismiss = onDismissSummary,
    )
}

@Composable
private fun BookshelfImportLoadingDialog(home: BookshelfState) {
    val progress = home.importProgress
    MoriLoadingDialog(
        show = home.isImporting,
        state =
            MoriLoadingDialogState(
                title = stringResource(Res.string.home_importing),
                message = progress.message(),
                currentName = progress?.currentName,
                currentIndex = progress?.currentIndex,
                totalCount = progress?.totalCount,
                fraction = progress?.fractionOrNull(),
            ),
        onCancel = null,
    )
}

@Composable
private fun BookshelfImportResultDialog(
    summary: BookImportSummary?,
    onDismiss: () -> Unit,
) {
    val current = summary ?: return
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Material -> MaterialBookshelfImportResultDialog(summary = current, onDismiss = onDismiss)
        UiThemeEngine.Miuix -> MiuixBookshelfImportResultDialog(summary = current, onDismiss = onDismiss)
    }
}

@Composable
private fun MaterialBookshelfImportResultDialog(
    summary: BookImportSummary,
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
private fun MiuixBookshelfImportResultDialog(
    summary: BookImportSummary,
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
                MiuixText(
                    text = failure.fileName,
                    color = MiuixTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
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
private fun BookImportUiProgress?.message(): String =
    if (this == null || totalCount <= 1) {
        stringResource(Res.string.home_importing)
    } else {
        stringResource(Res.string.home_import_progress, currentIndex, totalCount)
    }

private fun BookImportUiProgress.fractionOrNull(): Float? =
    if (totalCount > 1) {
        currentIndex.toFloat() / totalCount.toFloat()
    } else {
        null
    }

@Composable
private fun BookImportSummary.title(): String =
    if (successCount > 0) {
        stringResource(Res.string.book_import_failed_partial_title)
    } else {
        stringResource(Res.string.book_import_failed_all_title)
    }

@Composable
private fun BookImportSummary.message(): String =
    if (successCount > 0) {
        stringResource(Res.string.book_import_failed_partial_message)
    } else {
        stringResource(Res.string.book_import_failed_all_message)
    }
