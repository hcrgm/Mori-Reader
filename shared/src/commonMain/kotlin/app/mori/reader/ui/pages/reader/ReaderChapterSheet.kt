package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.book.ReaderTocItem
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_confirm
import app.mori.reader.shared.generated.resources.cd_table_of_contents
import app.mori.reader.shared.generated.resources.reader_chapter_jump
import app.mori.reader.shared.generated.resources.reader_chapter_jump_invalid_message
import app.mori.reader.shared.generated.resources.reader_chapter_jump_invalid_title
import app.mori.reader.shared.generated.resources.reader_chapter_jump_label
import app.mori.reader.shared.generated.resources.reader_chapter_jump_summary
import app.mori.reader.shared.generated.resources.reader_chapter_jump_title
import app.mori.reader.shared.generated.resources.reader_chapter_sheet_empty
import app.mori.reader.ui.components.settings.MoriInfoCard
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun ReaderChapterSheet(
    show: Boolean,
    isDark: Boolean,
    materialEInkMode: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    title: String,
    currentCharacter: Int,
    totalCharacters: Int,
    rows: List<ReaderTocItem>,
    currentChapterIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (ReaderTocItem) -> Unit,
    onJumpToCharacter: (Int) -> Unit,
) {
    var showJumpDialog by remember(show) { mutableStateOf(false) }
    var showInvalidInputDialog by remember(show) { mutableStateOf(false) }
    var jumpToInput by remember(show) { mutableStateOf("") }

    ReaderSheetTheme(
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        WindowBottomSheet(
            show = show,
            title = stringResource(Res.string.cd_table_of_contents),
            onDismissRequest = onDismiss,
        ) {
            Column(
                modifier = Modifier.padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReaderChapterHeader(
                    title = title,
                    currentCharacter = currentCharacter,
                    totalCharacters = totalCharacters,
                    onJumpTo = {
                        jumpToInput = ""
                        showJumpDialog = true
                    },
                )
                if (rows.isEmpty()) {
                    MoriInfoCard(
                        text = stringResource(Res.string.reader_chapter_sheet_empty),
                    )
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(420.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(rows) { row ->
                            ReaderChapterRow(
                                row = row,
                                isCurrent = row.chapterIndex == currentChapterIndex,
                                onClick = { onSelect(row) },
                            )
                        }
                    }
                }
            }
        }

        WindowDialog(
            title = stringResource(Res.string.reader_chapter_jump_title),
            summary = stringResource(Res.string.reader_chapter_jump_summary, currentCharacter, totalCharacters),
            show = showJumpDialog,
            onDismissRequest = { showJumpDialog = false },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = jumpToInput,
                    onValueChange = { value ->
                        if (value.all(Char::isDigit)) {
                            jumpToInput = value
                        }
                    },
                    label = stringResource(Res.string.reader_chapter_jump_label),
                    useLabelAsPlaceholder = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextButton(
                        text = stringResource(Res.string.btn_cancel),
                        onClick = { showJumpDialog = false },
                    )
                    TextButton(
                        text = stringResource(Res.string.reader_chapter_jump),
                        onClick = {
                            val target = jumpToInput.toIntOrNull()
                            if (target == null) {
                                showInvalidInputDialog = true
                            } else {
                                onJumpToCharacter(target)
                                showJumpDialog = false
                            }
                        },
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        }

        WindowDialog(
            title = stringResource(Res.string.reader_chapter_jump_invalid_title),
            summary = stringResource(Res.string.reader_chapter_jump_invalid_message),
            show = showInvalidInputDialog,
            onDismissRequest = { showInvalidInputDialog = false },
        ) {
            TextButton(
                text = stringResource(Res.string.btn_confirm),
                onClick = { showInvalidInputDialog = false },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

@Composable
private fun ReaderChapterHeader(
    title: String,
    currentCharacter: Int,
    totalCharacters: Int,
    onJumpTo: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(16.dp),
        colors =
            CardDefaults.defaultColors(
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                contentColor = MiuixTheme.colorScheme.onSurface,
            ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                color = MiuixTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$currentCharacter / $totalCharacters (${progressPercent(currentCharacter, totalCharacters).formatPercent()}%)",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                TextButton(
                    text = stringResource(Res.string.reader_chapter_jump),
                    onClick = onJumpTo,
                    colors = ButtonDefaults.textButtonColors(),
                )
            }
        }
    }
}

@Composable
private fun ReaderChapterRow(
    row: ReaderTocItem,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    if (isCurrent) {
                        MiuixTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.68f)
                    } else {
                        Color.Transparent
                    },
                ).clickable(onClick = onClick)
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 10.dp,
                    bottom = 10.dp,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(start = (16.dp * row.indentLevel))
                    .size(width = 3.dp, height = 18.dp)
                    .background(
                        if (isCurrent) MiuixTheme.colorScheme.primary else Color.Transparent,
                    ),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = row.label,
            modifier = Modifier.weight(1f),
            color = MiuixTheme.colorScheme.onSurface,
            fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        row.characterCount?.let {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = it.toString(),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

private fun progressPercent(
    currentCharacter: Int,
    totalCharacters: Int,
): Double {
    if (totalCharacters <= 0) return 0.0
    return currentCharacter.toDouble() / totalCharacters.toDouble() * 100.0
}
