package app.mori.reader.ui.pages.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.book.ReaderSavedBookmark
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_delete
import app.mori.reader.shared.generated.resources.reader_bookmark_position
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReaderBookmarkListItem(
    bookmark: ReaderSavedBookmark,
    chapterTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = chapterTitle,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                onDelete?.let { handleDelete ->
                    IconButton(
                        onClick = handleDelete,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(Res.string.cd_delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                text = bookmark.snippet.ifBlank { bookmark.chapterProgress.asBookmarkPositionLabel() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun AnimatedReaderBookmarkListItem(
    entry: AnimatedBookmarkEntry,
    chapterTitle: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onFullyRemoved: (String) -> Unit,
) {
    val visibleState =
        remember(entry.bookmark.id) {
            MutableTransitionState(!entry.shouldAnimateEnter && !entry.isRemoving)
        }

    LaunchedEffect(entry.isRemoving) {
        visibleState.targetState = !entry.isRemoving
    }
    LaunchedEffect(visibleState.isIdle, visibleState.currentState, entry.isRemoving) {
        if (entry.isRemoving && visibleState.isIdle && !visibleState.currentState) {
            onFullyRemoved(entry.bookmark.id)
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        ReaderBookmarkListItem(
            bookmark = entry.bookmark,
            chapterTitle = chapterTitle,
            onClick = onClick,
            onDelete = onDelete,
        )
    }
}

@Composable
internal fun ReaderPanelNoticeCard(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            body?.takeIf { it.isNotBlank() }?.let {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun Double.asBookmarkPositionLabel(): String {
    val percent = (coerceIn(0.0, 1.0) * 100).toInt().coerceIn(0, 100)
    return stringResource(Res.string.reader_bookmark_position, percent)
}

internal fun List<ReaderSavedBookmark>.filterByQuery(
    query: String,
    chapterTitleFor: (Int) -> String,
): List<ReaderSavedBookmark> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return this
    return filter { bookmark ->
        chapterTitleFor(bookmark.chapterIndex).contains(normalizedQuery, ignoreCase = true) ||
            bookmark.snippet.contains(normalizedQuery, ignoreCase = true)
    }
}

internal data class AnimatedBookmarkEntry(
    val bookmark: ReaderSavedBookmark,
    val isRemoving: Boolean = false,
    val shouldAnimateEnter: Boolean = false,
)
