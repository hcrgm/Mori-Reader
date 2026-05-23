package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mori.reader.data.book.ReaderSavedBookmark
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_close
import app.mori.reader.shared.generated.resources.reader_bookmarks_add_current
import app.mori.reader.shared.generated.resources.reader_bookmarks_empty
import app.mori.reader.shared.generated.resources.reader_bookmarks_hint
import app.mori.reader.shared.generated.resources.reader_bookmarks_title
import app.mori.reader.shared.generated.resources.reader_current_position_bookmarked
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReaderBookmarksPanel(
    bookmarks: List<ReaderSavedBookmark>,
    chapterTitleFor: (Int) -> String,
    currentPositionBookmarked: Boolean,
    onToggleCurrentBookmark: () -> Unit,
    onSelectBookmark: (ReaderSavedBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.reader_bookmarks_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(Res.string.btn_close),
                )
            }
        }
        BookmarksContent(
            bookmarks = bookmarks,
            chapterTitleFor = chapterTitleFor,
            currentPositionBookmarked = currentPositionBookmarked,
            onToggleCurrentBookmark = onToggleCurrentBookmark,
            onSelectBookmark = onSelectBookmark,
            onDeleteBookmark = onDeleteBookmark,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun BookmarksContent(
    bookmarks: List<ReaderSavedBookmark>,
    chapterTitleFor: (Int) -> String,
    currentPositionBookmarked: Boolean,
    onToggleCurrentBookmark: () -> Unit,
    onSelectBookmark: (ReaderSavedBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var animatedBookmarks by remember {
        mutableStateOf(
            bookmarks.map { bookmark ->
                AnimatedBookmarkEntry(
                    bookmark = bookmark,
                    shouldAnimateEnter = false,
                )
            },
        )
    }

    LaunchedEffect(bookmarks) {
        val nextBookmarksById = bookmarks.associateBy(ReaderSavedBookmark::id)
        val currentEntriesById = animatedBookmarks.associateBy { it.bookmark.id }

        val retainedEntries =
            animatedBookmarks.map { entry ->
                nextBookmarksById[entry.bookmark.id]?.let { bookmark ->
                    entry.copy(
                        bookmark = bookmark,
                        isRemoving = false,
                        shouldAnimateEnter = false,
                    )
                } ?: entry.copy(isRemoving = true)
            }

        val mergedEntries = retainedEntries.toMutableList()
        bookmarks.forEachIndexed { index, bookmark ->
            if (currentEntriesById.containsKey(bookmark.id)) return@forEachIndexed
            val anchorId =
                bookmarks
                    .drop(index + 1)
                    .firstOrNull { candidate -> currentEntriesById.containsKey(candidate.id) }
                    ?.id
            val insertIndex = anchorId?.let { id -> mergedEntries.indexOfFirst { it.bookmark.id == id } } ?: -1
            mergedEntries.add(
                index = if (insertIndex >= 0) insertIndex else mergedEntries.size,
                element =
                    AnimatedBookmarkEntry(
                        bookmark = bookmark,
                        shouldAnimateEnter = true,
                    ),
            )
        }

        animatedBookmarks = mergedEntries
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            AssistChip(
                onClick = onToggleCurrentBookmark,
                label = {
                    Text(
                        stringResource(
                            if (currentPositionBookmarked) {
                                Res.string.reader_current_position_bookmarked
                            } else {
                                Res.string.reader_bookmarks_add_current
                            },
                        ),
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (currentPositionBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = null,
                    )
                },
            )
        }
        item {
            Text(
                text = stringResource(Res.string.reader_bookmarks_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (bookmarks.isEmpty()) {
            item {
                ReaderPanelNoticeCard(
                    title = stringResource(Res.string.reader_bookmarks_empty),
                )
            }
        } else {
            items(
                items = animatedBookmarks,
                key = { it.bookmark.id },
            ) { entry ->
                AnimatedReaderBookmarkListItem(
                    entry = entry,
                    chapterTitle = chapterTitleFor(entry.bookmark.chapterIndex),
                    onClick = { onSelectBookmark(entry.bookmark) },
                    onDelete = { onDeleteBookmark(entry.bookmark.id) },
                    onFullyRemoved = { bookmarkId ->
                        animatedBookmarks = animatedBookmarks.filterNot { it.bookmark.id == bookmarkId }
                    },
                )
            }
        }
    }
}
