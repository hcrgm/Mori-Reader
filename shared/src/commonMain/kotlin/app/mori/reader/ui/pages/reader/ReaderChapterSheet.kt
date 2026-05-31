package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.book.ReaderSavedBookmark
import app.mori.reader.data.book.ReaderTocItem
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_confirm
import app.mori.reader.shared.generated.resources.cd_table_of_contents
import app.mori.reader.shared.generated.resources.reader_bookmarks_empty
import app.mori.reader.shared.generated.resources.reader_chapter_jump
import app.mori.reader.shared.generated.resources.reader_chapter_jump_invalid_message
import app.mori.reader.shared.generated.resources.reader_chapter_jump_invalid_title
import app.mori.reader.shared.generated.resources.reader_chapter_jump_label
import app.mori.reader.shared.generated.resources.reader_chapter_jump_summary
import app.mori.reader.shared.generated.resources.reader_chapter_jump_title
import app.mori.reader.shared.generated.resources.reader_chapter_search_bookmarks_placeholder
import app.mori.reader.shared.generated.resources.reader_chapter_search_placeholder
import app.mori.reader.shared.generated.resources.reader_chapter_sheet_empty
import app.mori.reader.shared.generated.resources.reader_chapter_tab_bookmarks
import app.mori.reader.shared.generated.resources.reader_chapter_tab_chapters
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderChapterSheet(
    isOpen: Boolean,
    isDark: Boolean,
    materialEInkMode: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    bookTitle: String,
    bookAuthor: String?,
    currentCharacter: Int,
    totalCharacters: Int,
    rows: List<ReaderTocItem>,
    bookmarks: List<ReaderSavedBookmark>,
    currentChapterIndex: Int,
    chapterTitleForBookmark: (Int) -> String,
    onDismiss: () -> Unit,
    onSelect: (ReaderTocItem) -> Unit,
    onSelectBookmark: (ReaderSavedBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onJumpToCharacter: (Int) -> Unit,
) {
    var showJumpDialog by remember { mutableStateOf(false) }
    var showInvalidInputDialog by remember { mutableStateOf(false) }
    var jumpToInput by remember { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    val normalizedQuery = remember(query) { query.trim() }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val uiRows = remember(rows) { rows.toReaderTocUiRows() }
    var expandedKeys by rememberSaveable(rows) { mutableStateOf(uiRows.initialExpandedKeys().toList()) }
    var searchActive by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val currentTopLevelKey =
        remember(uiRows, currentChapterIndex) {
            uiRows.currentTopLevelKey(currentChapterIndex)
        }
    val visibleUiRows =
        remember(uiRows, expandedKeys, normalizedQuery) {
            if (normalizedQuery.isBlank()) {
                uiRows.visibleWith(expandedKeys.toSet())
            } else {
                uiRows.filter { it.row.label.contains(normalizedQuery, ignoreCase = true) }
            }
        }
    val visibleBookmarks =
        remember(bookmarks, normalizedQuery, chapterTitleForBookmark) {
            bookmarks.filterByQuery(normalizedQuery, chapterTitleForBookmark)
        }
    val listState = rememberLazyListState()
    var didAutoPosition by remember(currentChapterIndex, rows) { mutableStateOf(false) }

    LaunchedEffect(isOpen, currentTopLevelKey, visibleUiRows, normalizedQuery, selectedTab) {
        if (!isOpen) return@LaunchedEffect
        if (!didAutoPosition) {
            if (selectedTab == 0 && normalizedQuery.isBlank()) {
                val currentIndex = visibleUiRows.indexOfFirst { it.key == currentTopLevelKey }
                if (currentIndex >= 0) {
                    listState.scrollToItem((currentIndex - 2).coerceAtLeast(0))
                }
            }
            didAutoPosition = true
        }
    }

    LaunchedEffect(isOpen) {
        if (!isOpen) {
            didAutoPosition = false
            searchActive = false
            focusManager.clearFocus(force = true)
            return@LaunchedEffect
        }
        focusManager.clearFocus(force = true)
    }

    LaunchedEffect(selectedTab) {
        listState.scrollToItem(0)
    }

    ReaderMaterialTheme(
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        BoxWithConstraints {
            val drawerWidth =
                if (maxWidth < 424.dp) {
                    maxWidth * 0.85f
                } else {
                    360.dp
                }

            ModalDrawerSheet(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(drawerWidth),
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                drawerTonalElevation = 3.dp,
                windowInsets = WindowInsets.navigationBars,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()),
                ) {
                    ReaderChapterDrawerHeader(
                        bookTitle = bookTitle,
                        bookAuthor = bookAuthor,
                    )
                    ReaderChapterTab(
                        selectedTab = selectedTab,
                        onSelectTab = { selectedTab = it },
                    )
                    ReaderChapterSearch(
                        query = query,
                        active = searchActive,
                        selectedTab = selectedTab,
                        onActiveChange = { searchActive = it },
                        onQueryChange = { query = it },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    when (selectedTab) {
                        0 -> {
                            if (visibleUiRows.isEmpty()) {
                                ReaderChapterEmpty(
                                    text = stringResource(Res.string.reader_chapter_sheet_empty),
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    state = listState,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    items(
                                        items = visibleUiRows,
                                        key = { it.key },
                                    ) { uiRow ->
                                        ReaderChapterRow(
                                            row = uiRow.row,
                                            hasChildren = uiRow.hasChildren,
                                            expanded = uiRow.key in expandedKeys,
                                            isCurrent = uiRow.key == currentTopLevelKey,
                                            onToggleExpanded = {
                                                expandedKeys =
                                                    if (uiRow.key in expandedKeys) {
                                                        expandedKeys - uiRow.key
                                                    } else {
                                                        expandedKeys + uiRow.key
                                                    }
                                            },
                                            onClick = { onSelect(uiRow.row) },
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            if (visibleBookmarks.isEmpty()) {
                                ReaderChapterEmpty(
                                    text = stringResource(Res.string.reader_bookmarks_empty),
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    state = listState,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    items(
                                        items = visibleBookmarks,
                                        key = { it.id },
                                    ) { bookmark ->
                                        ReaderBookmarkListItem(
                                            bookmark = bookmark,
                                            chapterTitle = chapterTitleForBookmark(bookmark.chapterIndex),
                                            onClick = { onSelectBookmark(bookmark) },
                                            onDelete = { onDeleteBookmark(bookmark.id) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showJumpDialog) {
            AlertDialog(
                onDismissRequest = { showJumpDialog = false },
                title = {
                    Text(text = stringResource(Res.string.reader_chapter_jump_title))
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(Res.string.reader_chapter_jump_summary, currentCharacter, totalCharacters),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            value = jumpToInput,
                            onValueChange = { value ->
                                if (value.all(Char::isDigit)) {
                                    jumpToInput = value
                                }
                            },
                            label = {
                                Text(text = stringResource(Res.string.reader_chapter_jump_label))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val target = jumpToInput.toIntOrNull()
                            if (target == null) {
                                showInvalidInputDialog = true
                            } else {
                                onJumpToCharacter(target)
                                showJumpDialog = false
                            }
                        },
                    ) {
                        Text(text = stringResource(Res.string.reader_chapter_jump))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJumpDialog = false }) {
                        Text(text = stringResource(Res.string.btn_cancel))
                    }
                },
            )
        }

        if (showInvalidInputDialog) {
            AlertDialog(
                onDismissRequest = { showInvalidInputDialog = false },
                title = {
                    Text(text = stringResource(Res.string.reader_chapter_jump_invalid_title))
                },
                text = {
                    Text(text = stringResource(Res.string.reader_chapter_jump_invalid_message))
                },
                confirmButton = {
                    TextButton(onClick = { showInvalidInputDialog = false }) {
                        Text(text = stringResource(Res.string.btn_confirm))
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReaderChapterDrawerHeader(
    bookTitle: String,
    bookAuthor: String?,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 12.dp, end = 6.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.7f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = bookTitle.ifBlank { stringResource(Res.string.cd_table_of_contents) },
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            bookAuthor
                ?.takeIf { it.isNotBlank() }
                ?.let { author ->
                    Text(
                        text = author,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
        }
    }
}

@Composable
private fun ReaderChapterSearch(
    query: String,
    active: Boolean,
    selectedTab: Int,
    onActiveChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val editing = active || query.isNotEmpty()

    LaunchedEffect(active) {
        if (active) {
            focusRequester.requestFocus()
        }
    }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable {
                    onActiveChange(true)
                },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        border =
            androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 38.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (editing) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier =
                        Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle =
                        MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (query.isEmpty()) {
                                Text(
                                    text =
                                        stringResource(
                                            if (selectedTab == 0) {
                                                Res.string.reader_chapter_search_placeholder
                                            } else {
                                                Res.string.reader_chapter_search_bookmarks_placeholder
                                            },
                                        ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            } else {
                Text(
                    text =
                        stringResource(
                            if (selectedTab == 0) {
                                Res.string.reader_chapter_search_placeholder
                            } else {
                                Res.string.reader_chapter_search_bookmarks_placeholder
                            },
                        ),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReaderChapterTab(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
) {
    TabRow(
        selectedTabIndex = selectedTab,
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onSelectTab(0) },
            text = {
                Text(
                    text = stringResource(Res.string.reader_chapter_tab_chapters),
                    style = MaterialTheme.typography.labelLarge,
                )
            },
            modifier = Modifier.heightIn(min = 36.dp),
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onSelectTab(1) },
            text = {
                Text(
                    text = stringResource(Res.string.reader_chapter_tab_bookmarks),
                    style = MaterialTheme.typography.labelLarge,
                )
            },
            modifier = Modifier.heightIn(min = 36.dp),
        )
    }
}

@Composable
private fun ReaderChapterEmpty(text: String) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ReaderChapterRow(
    row: ReaderTocItem,
    hasChildren: Boolean,
    expanded: Boolean,
    isCurrent: Boolean,
    onToggleExpanded: () -> Unit,
    onClick: () -> Unit,
) {
    val indent = (16.dp * row.indentLevel).coerceAtMost(64.dp)
    val headlineColor =
        if (isCurrent) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    val supportingColor =
        if (isCurrent) {
            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    val containerColor =
        if (isCurrent) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            Color.Transparent
        }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isCurrent) containerColor else Color.Transparent,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .padding(start = 10.dp, top = 6.dp, end = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(indent))
            if (row.indentLevel > 0) {
                Box(
                    modifier =
                        Modifier
                            .size(6.dp)
                            .background(
                                if (isCurrent) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                CircleShape,
                            ),
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = row.label,
                        modifier = Modifier.weight(1f),
                        color = headlineColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (row.indentLevel == 0 && row.characterCount != null) {
                        Text(
                            text = row.characterCount.toString(),
                            color = supportingColor,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                        )
                    }
                }
            }
            if (hasChildren) {
                IconButton(
                    onClick = onToggleExpanded,
                    modifier = Modifier.size(34.dp),
                ) {
                    Icon(
                        imageVector =
                            if (expanded) {
                                Icons.Rounded.KeyboardArrowDown
                            } else {
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight
                            },
                        contentDescription = null,
                        tint = supportingColor,
                    )
                }
            }
        }
    }
}

private data class ReaderTocUiRow(
    val key: String,
    val row: ReaderTocItem,
    val hasChildren: Boolean,
)

private fun List<ReaderTocItem>.toReaderTocUiRows(): List<ReaderTocUiRow> =
    mapIndexed { index, row ->
        ReaderTocUiRow(
            key = "$index:${row.chapterIndex}:${row.fragment.orEmpty()}:${row.label}",
            row = row,
            hasChildren = getOrNull(index + 1)?.indentLevel?.let { it > row.indentLevel } == true,
        )
    }

private fun List<ReaderTocUiRow>.visibleWith(expandedKeys: Set<String>): List<ReaderTocUiRow> {
    val hiddenIndentStack = mutableListOf<Int>()
    return filter { uiRow ->
        hiddenIndentStack.removeAll { it >= uiRow.row.indentLevel }
        val hidden = hiddenIndentStack.any { uiRow.row.indentLevel > it }
        if (!hidden && uiRow.hasChildren && uiRow.key !in expandedKeys) {
            hiddenIndentStack += uiRow.row.indentLevel
        }
        !hidden
    }
}

private fun List<ReaderTocUiRow>.initialExpandedKeys(): Set<String> =
    filter { it.hasChildren && it.row.indentLevel == 0 }.mapTo(mutableSetOf()) { it.key }

private fun List<ReaderTocUiRow>.currentTopLevelKey(currentChapterIndex: Int): String? {
    val currentIndex = indexOfFirst { it.row.chapterIndex == currentChapterIndex }
    if (currentIndex < 0) return null
    val parentIndex =
        (currentIndex downTo 0).firstOrNull { index ->
            this[index].row.indentLevel == 0
        } ?: currentIndex
    return this[parentIndex].key
}
