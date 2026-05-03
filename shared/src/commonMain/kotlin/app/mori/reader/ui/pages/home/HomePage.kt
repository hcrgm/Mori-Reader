package app.mori.reader.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mori.reader.data.book.BookCategory
import app.mori.reader.data.book.BookInfo
import app.mori.reader.data.book.rememberEpubPicker
import app.mori.reader.data.settings.BookshelfSortMode
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_confirm
import app.mori.reader.shared.generated.resources.btn_delete
import app.mori.reader.shared.generated.resources.cd_add_category
import app.mori.reader.shared.generated.resources.cd_category_manage
import app.mori.reader.shared.generated.resources.cd_close
import app.mori.reader.shared.generated.resources.cd_drag_sort
import app.mori.reader.shared.generated.resources.cd_import_book
import app.mori.reader.shared.generated.resources.cd_rename
import app.mori.reader.shared.generated.resources.cd_sort_by
import app.mori.reader.shared.generated.resources.home_adjust_category
import app.mori.reader.shared.generated.resources.home_category_name_label
import app.mori.reader.shared.generated.resources.home_delete_book
import app.mori.reader.shared.generated.resources.home_delete_book_confirm
import app.mori.reader.shared.generated.resources.home_delete_category
import app.mori.reader.shared.generated.resources.home_empty_bookshelf
import app.mori.reader.shared.generated.resources.home_empty_category
import app.mori.reader.shared.generated.resources.home_importing
import app.mori.reader.shared.generated.resources.home_loading
import app.mori.reader.shared.generated.resources.home_no_categories
import app.mori.reader.shared.generated.resources.home_no_categories_available
import app.mori.reader.shared.generated.resources.home_rename_category
import app.mori.reader.shared.generated.resources.home_tab_all
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.window.WindowListPopup
import androidx.compose.foundation.lazy.grid.items as gridItems

private val HomeHorizontalPadding = 12.dp
private val HomeTabInnerPadding = 5.dp
private val HomeContentTopSpacing = 12.dp
private val HomePageSpacing = 12.dp

@Composable
fun HomePage(
    state: AppState,
    message: String?,
    fixedPadding: PaddingValues,
    onIntent: (AppIntent) -> Unit,
) {
    var categoryManagerOpen by remember { mutableStateOf(false) }
    var createCategoryOpen by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<BookCategory?>(null) }
    var deletingCategory by remember { mutableStateOf<BookCategory?>(null) }
    var contextMenuBook by remember { mutableStateOf<BookInfo?>(null) }
    var contextMenuPage by remember { mutableStateOf<Int?>(null) }
    var categoryEditingBook by remember { mutableStateOf<BookInfo?>(null) }
    var deletingBook by remember { mutableStateOf<BookInfo?>(null) }
    val showSortPopup = remember { mutableStateOf(false) }
    val sortPopupHoldDown = remember { mutableStateOf(false) }
    val epubPicker = rememberEpubPicker { uris ->
        onIntent(AppIntent.ImportBooks(uris))
    }

    val home = state.home
    val categoryIds = listOf<String?>(null) + home.categories.map { it.id }
    val selectedIndex = categoryIds.indexOf(home.selectedCategoryId).takeIf { it >= 0 } ?: 0
    val tabs = listOf(stringResource(Res.string.home_tab_all)) + home.categories.map { it.name }
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { tabs.size },
    )
    val tabCoroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedIndex, tabs.size) {
        if (pagerState.currentPage != selectedIndex && selectedIndex in tabs.indices) {
            pagerState.scrollToPage(selectedIndex)
        }
    }

    LaunchedEffect(pagerState, categoryIds, home.selectedCategoryId) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val targetCategoryId = categoryIds.getOrNull(page)
                if (targetCategoryId != home.selectedCategoryId) {
                    onIntent(AppIntent.SelectBookCategory(targetCategoryId))
                }
            }
    }

    MoriPageScaffold(
        title = AppTab.Home.title,
        subtitle = AppTab.Home.subtitle,
        blurEnabled = state.settings.blurEnabled,
        fixedPadding = fixedPadding,
        message = message,
        navigationIcon = {
            IconButton(
                onClick = {
                    showSortPopup.value = true
                    sortPopupHoldDown.value = true
                },
                holdDownState = sortPopupHoldDown.value,
            ) {
                Icon(
                    imageVector = MiuixIcons.Sort,
                    contentDescription = stringResource(Res.string.cd_sort_by),
                )
            }
            WindowListPopup(
                show = showSortPopup.value,
                popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                alignment = PopupPositionProvider.Align.TopStart,
                onDismissRequest = {
                    showSortPopup.value = false
                },
                onDismissFinished = {
                    sortPopupHoldDown.value = false
                },
            ) {
                val dismiss = LocalDismissState.current
                ListPopupColumn {
                    BookshelfSortMode.entries.forEachIndexed { index, option ->
                        key(option) {
                            DropdownImpl(
                                text = option.label,
                                optionSize = BookshelfSortMode.entries.size,
                                isSelected = state.settings.bookshelfSortMode == option,
                                index = index,
                                onSelectedIndexChange = {
                                    onIntent(AppIntent.SetBookshelfSortMode(option))
                                    dismiss?.invoke()
                                },
                            )
                        }
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = epubPicker) {
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = stringResource(Res.string.cd_import_book),
                )
            }
            IconButton(onClick = { categoryManagerOpen = true }) {
                Icon(
                    imageVector = MiuixIcons.More,
                    contentDescription = stringResource(Res.string.cd_category_manage),
                )
            }
        },
        header = {
            if (home.isLoading) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TabRowDefaults.TabRowWithContourHeight),
                )
            } else {
                TabRowWithContour(
                    tabs = tabs,
                    selectedTabIndex = pagerState.currentPage.coerceAtMost(tabs.lastIndex),
                    onTabSelected = { index ->
                        tabCoroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HomeHorizontalPadding),
                    colors = TabRowDefaults.tabRowColors(
                        backgroundColor = if (state.settings.blurEnabled) Color.Transparent else MiuixTheme.colorScheme.surface,
                    ),
                )
            }
        },
    ) { paddingValues, scrollBehavior ->
        BookshelfContent(
            state = state,
            paddingValues = paddingValues,
            scrollBehavior = scrollBehavior,
            pagerState = pagerState,
            categoryIds = categoryIds,
            contextMenuBookId = contextMenuBook?.id,
            contextMenuPage = contextMenuPage,
            onOpenContextMenu = { page, book ->
                contextMenuPage = page
                contextMenuBook = book
            },
            onDismissContextMenu = {
                contextMenuBook = null
                contextMenuPage = null
            },
            onEditBookCategories = {
                categoryEditingBook = contextMenuBook
                contextMenuBook = null
                contextMenuPage = null
            },
            onDeleteBook = {
                deletingBook = contextMenuBook
                contextMenuBook = null
                contextMenuPage = null
            },
            onIntent = onIntent,
        )
    }

    CategoryManagerSheet(
        show = categoryManagerOpen,
        categories = state.home.categories,
        onDismiss = { categoryManagerOpen = false },
        onCreate = { createCategoryOpen = true },
        onReorder = { categoryIds -> onIntent(AppIntent.ReorderBookCategories(categoryIds)) },
        onRename = { category -> editingCategory = category },
        onDelete = { category -> deletingCategory = category },
    )

    CreateCategoryDialog(
        show = createCategoryOpen,
        onDismiss = { createCategoryOpen = false },
        onConfirm = { name ->
            onIntent(AppIntent.CreateBookCategory(name))
            createCategoryOpen = false
        },
    )

    RenameCategoryDialog(
        category = editingCategory,
        onDismiss = { editingCategory = null },
        onConfirm = { category, name ->
            onIntent(AppIntent.RenameBookCategory(category.id, name))
            editingCategory = null
        },
    )

    DeleteCategoryDialog(
        category = deletingCategory,
        onDismiss = { deletingCategory = null },
        onConfirm = { category ->
            onIntent(AppIntent.DeleteBookCategory(category.id))
            deletingCategory = null
        },
    )

    EditBookCategoriesSheet(
        book = categoryEditingBook,
        categories = state.home.categories,
        onDismiss = { categoryEditingBook = null },
        onConfirm = { book, categoryIds ->
            onIntent(AppIntent.UpdateBookCategories(book.id, categoryIds))
            categoryEditingBook = null
        },
    )

    DeleteBookDialog(
        book = deletingBook,
        onDismiss = { deletingBook = null },
        onConfirm = { book ->
            onIntent(AppIntent.DeleteBook(book.id))
            deletingBook = null
        },
    )
}

@Composable
private fun BookshelfContent(
    state: AppState,
    paddingValues: PaddingValues,
    scrollBehavior: top.yukonga.miuix.kmp.basic.ScrollBehavior,
    pagerState: androidx.compose.foundation.pager.PagerState,
    categoryIds: List<String?>,
    contextMenuBookId: String?,
    contextMenuPage: Int?,
    onOpenContextMenu: (Int, BookInfo) -> Unit,
    onDismissContextMenu: () -> Unit,
    onEditBookCategories: () -> Unit,
    onDeleteBook: () -> Unit,
    onIntent: (AppIntent) -> Unit,
) {
    val home = state.home

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HomeHorizontalPadding + HomeTabInnerPadding),
        beyondViewportPageCount = 1,
        pageSpacing = HomePageSpacing,
        userScrollEnabled = true,
        key = { page -> categoryIds.getOrNull(page) ?: "all" },
    ) { page ->
        val gridState = rememberLazyGridState()
        val visibleBooks = if (page == 0) {
            home.books.sortedFor(state.settings.bookshelfSortMode)
        } else {
            home.books
                .filter { categoryIds.getOrNull(page) in it.categoryIds }
                .sortedFor(state.settings.bookshelfSortMode)
        }

        LaunchedEffect(state.settings.bookshelfSortMode) {
            if (
                !home.isLoading &&
                (visibleBooks.isNotEmpty() || home.errorMessage != null || home.isImporting)
            ) {
                gridState.scrollToItem(0)
            }
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + HomeContentTopSpacing,
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            home.errorMessage?.let { error ->
                item(
                    key = "error",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    ErrorCard(
                        message = error,
                        onDismiss = { onIntent(AppIntent.DismissHomeError) },
                    )
                }
            }

            if (home.isImporting) {
                item(
                    key = "importing",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    StatusCard(text = stringResource(Res.string.home_importing))
                }
            }

            when {
                home.isLoading -> item(
                    key = "loading",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    EmptyBookshelfMessage(text = stringResource(Res.string.home_loading))
                }

                visibleBooks.isEmpty() -> item(
                    key = "empty",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    val emptyText =
                        if (page == 0) stringResource(Res.string.home_empty_bookshelf) else stringResource(
                            Res.string.home_empty_category
                        )
                    EmptyBookshelfMessage(text = emptyText)
                }

                else -> gridItems(
                    items = visibleBooks,
                    key = { it.id },
                ) { book ->
                    BookCard(
                        book = book,
                        showContextMenu = contextMenuBookId == book.id && contextMenuPage == page,
                        onOpenContextMenu = { onOpenContextMenu(page, book) },
                        onDismissContextMenu = onDismissContextMenu,
                        onEditBookCategories = onEditBookCategories,
                        onDeleteBook = onDeleteBook,
                        onClick = { onIntent(AppIntent.OpenBook(book.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BookCard(
    book: BookInfo,
    showContextMenu: Boolean,
    onOpenContextMenu: () -> Unit,
    onDismissContextMenu: () -> Unit,
    onEditBookCategories: () -> Unit,
    onDeleteBook: () -> Unit,
    onClick: () -> Unit,
) {
    val progress = book.progressPercent.coerceIn(0, 100)
    val progressText = "${"%.1f".format(progress.toFloat())}%"
    val cardCornerRadius = 18.dp
    val cardShape = RoundedCornerShape(cardCornerRadius)
    val progressShape = RoundedCornerShape(50)

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = cardCornerRadius,
            colors = CardDefaults.defaultColors(
                color = Color.Transparent,
            ),
            insideMargin = PaddingValues(0.dp),
            pressFeedbackType = PressFeedbackType.Tilt,
            onClick = onClick,
            onLongPress = onOpenContextMenu,
            // holdDownState - TODO: 这个参数，miuix还没有发release，release后补上，交互体验更好
        ) {
            Column(
                modifier = Modifier.clip(cardShape),
            ) {
                BookCoverImage(
                    coverPath = book.coverPath,
                    title = book.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(5f / 7f)
                        .clip(cardShape),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 1.dp, end = 1.dp, top = 6.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(progressShape)
                                .background(MiuixTheme.colorScheme.surfaceVariant),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress / 100f)
                                    .height(3.dp)
                                    .background(MiuixTheme.colorScheme.primary),
                            )
                        }
                        Text(
                            text = progressText,
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                        )
                    }
                    Text(
                        text = book.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        WindowListPopup(
            show = showContextMenu,
            popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
            alignment = PopupPositionProvider.Align.TopStart,
            onDismissRequest = onDismissContextMenu,
            onDismissFinished = onDismissContextMenu,
        ) {
            val dismiss = LocalDismissState.current
            ListPopupColumn {
                DropdownImpl(
                    text = stringResource(Res.string.home_adjust_category),
                    optionSize = 2,
                    isSelected = false,
                    index = 0,
                    onSelectedIndexChange = {
                        onEditBookCategories()
                        dismiss?.invoke()
                    },
                )
                DropdownImpl(
                    text = stringResource(Res.string.home_delete_book),
                    optionSize = 2,
                    isSelected = false,
                    index = 1,
                    onSelectedIndexChange = {
                        onDeleteBook()
                        dismiss?.invoke()
                    },
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MiuixTheme.colorScheme.onErrorContainer,
            )
            IconButton(onClick = onDismiss) {
                Icon(MiuixIcons.Close, contentDescription = stringResource(Res.string.cd_close))
            }
        }
    }
}

@Composable
private fun StatusCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun EmptyBookshelfMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun CategoryManagerSheet(
    show: Boolean,
    categories: List<BookCategory>,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onReorder: (List<String>) -> Unit,
    onRename: (BookCategory) -> Unit,
    onDelete: (BookCategory) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val onReorderState by rememberUpdatedState(onReorder)
    var localCategories by remember(show) { mutableStateOf(categories) }

    LaunchedEffect(categories) {
        localCategories = categories
    }

    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        localCategories = localCategories.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (!reorderableState.isAnyItemDragging) {
            val updatedIds = localCategories.map(BookCategory::id)
            if (updatedIds != categories.map(BookCategory::id)) {
                onReorderState(updatedIds)
            }
        }
    }

    WindowBottomSheet(
        show = show,
        title = stringResource(Res.string.cd_category_manage),

        endAction = {
            IconButton(
                onClick = onCreate,
                backgroundColor = MiuixTheme.colorScheme.primary.copy(0.2f)
            ) {
                Icon(
                    MiuixIcons.Add,
                    contentDescription = stringResource(Res.string.cd_add_category)
                )
            }
        },
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (localCategories.isEmpty()) {
                Text(
                    text = stringResource(Res.string.home_no_categories),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = localCategories,
                        key = { it.id },
                    ) { category ->
                        CategoryManageItem(
                            category = category,
                            reorderableState = reorderableState,
                            onRename = { onRename(category) },
                            onDelete = { onDelete(category) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.CategoryManageItem(
    category: BookCategory,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    ReorderableItem(
        state = reorderableState,
        key = category.id,
    ) { isDragging ->

        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    with(this) {
                        Modifier.longPressDraggableHandle(
                            onDragStarted = {
                                hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.GestureThresholdActivate,
                                )
                            },
                            onDragStopped = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                            },
                        )
                    },
                )
                .animateItem(),
        ) {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = category.name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = MiuixIcons.Sort,
                    contentDescription = stringResource(Res.string.cd_drag_sort),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                IconButton(onClick = onRename) {
                    Icon(MiuixIcons.Edit, contentDescription = stringResource(Res.string.cd_rename))
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        MiuixIcons.Delete,
                        contentDescription = stringResource(Res.string.btn_delete),
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateCategoryDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember(show) { mutableStateOf("") }

    WindowDialog(
        title = stringResource(Res.string.cd_add_category),
        show = show,
        onDismissRequest = onDismiss,
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(Res.string.home_category_name_label),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                onClick = { onConfirm(name) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

@Composable
private fun RenameCategoryDialog(
    category: BookCategory?,
    onDismiss: () -> Unit,
    onConfirm: (BookCategory, String) -> Unit,
) {
    var name by remember(category?.id) { mutableStateOf(category?.name.orEmpty()) }

    WindowDialog(
        title = stringResource(Res.string.home_rename_category),
        show = category != null,
        onDismissRequest = onDismiss,
    ) {
        val current = category ?: return@WindowDialog
        TextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(Res.string.home_category_name_label),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                onClick = { onConfirm(current, name) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

@Composable
private fun DeleteCategoryDialog(
    category: BookCategory?,
    onDismiss: () -> Unit,
    onConfirm: (BookCategory) -> Unit,
) {
    WindowDialog(
        title = stringResource(Res.string.home_delete_category),
        show = category != null,
        onDismissRequest = onDismiss,
    ) {
        val current = category ?: return@WindowDialog
        Text(
            text = "确认删除「${current.name}」？分类下的图书不会删除，但会移出该分类。",
            modifier = Modifier
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
                text = stringResource(Res.string.btn_delete),
                onClick = { onConfirm(current) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

@Composable
private fun EditBookCategoriesSheet(
    book: BookInfo?,
    categories: List<BookCategory>,
    onDismiss: () -> Unit,
    onConfirm: (BookInfo, List<String>) -> Unit,
) {
    var selectedCategoryIds by remember(book?.id, categories) {
        mutableStateOf(book?.categoryIds?.filter { categoryId -> categories.any { it.id == categoryId } }
            ?.toSet().orEmpty())
    }

    WindowBottomSheet(
        show = book != null,
        title = stringResource(Res.string.home_adjust_category),
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (categories.isEmpty()) {
                Text(
                    text = stringResource(Res.string.home_no_categories_available),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    categories.forEach { category ->
                        CheckboxPreference(
                            title = category.name,
                            checked = category.id in selectedCategoryIds,
                            onCheckedChange = { checked ->
                                selectedCategoryIds = if (checked) {
                                    selectedCategoryIds + category.id
                                } else {
                                    selectedCategoryIds - category.id
                                }
                            },
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(
                    text = stringResource(Res.string.btn_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(20.dp))
                TextButton(
                    text = stringResource(Res.string.btn_confirm),
                    onClick = {
                        val current = book ?: return@TextButton
                        onConfirm(current, selectedCategoryIds.toList())
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

@Composable
private fun DeleteBookDialog(
    book: BookInfo?,
    onDismiss: () -> Unit,
    onConfirm: (BookInfo) -> Unit,
) {
    WindowDialog(
        title = stringResource(Res.string.home_delete_book),
        show = book != null,
        onDismissRequest = onDismiss,
    ) {
        Text(
            text = stringResource(Res.string.home_delete_book_confirm, book?.title.orEmpty()),
            modifier = Modifier
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
                text = stringResource(Res.string.btn_delete),
                onClick = {
                    val current = book ?: return@TextButton
                    onConfirm(current)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}

private fun List<BookInfo>.sortedFor(mode: BookshelfSortMode): List<BookInfo> = when (mode) {
    BookshelfSortMode.Recent -> sortedWith(
        compareByDescending<BookInfo> { it.lastOpenedAt ?: it.importedAt }
            .thenBy { it.title.lowercase() },
    )

    BookshelfSortMode.Title -> sortedBy { it.title.lowercase() }
}
