package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.book.ReaderTocItem
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_table_of_contents
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@Composable
internal fun ReaderChapterSheet(
    show: Boolean,
    isDark: Boolean,
    title: String,
    currentCharacter: Int,
    totalCharacters: Int,
    rows: List<ReaderTocItem>,
    currentChapterIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (ReaderTocItem) -> Unit,
) {
    ReaderSheetTheme(isDark = isDark) {
        WindowBottomSheet(
            show = show,
            title = stringResource(Res.string.cd_table_of_contents),
            onDismissRequest = onDismiss,
        ) {
            Column(
                modifier = Modifier.padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = title,
                    color = MiuixTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$currentCharacter / $totalCharacters",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(rows) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(row) }
                                .background(
                                    if (row.chapterIndex == currentChapterIndex) {
                                        MiuixTheme.colorScheme.surfaceContainerHighest
                                    } else {
                                        MiuixTheme.colorScheme.surface
                                    },
                                )
                                .padding(
                                    start = 12.dp + 16.dp * row.indentLevel,
                                    end = 12.dp,
                                    top = 10.dp,
                                    bottom = 10.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = row.label,
                                modifier = Modifier.weight(1f),
                                color = MiuixTheme.colorScheme.onSurface,
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
                }
            }
        }
    }
}
