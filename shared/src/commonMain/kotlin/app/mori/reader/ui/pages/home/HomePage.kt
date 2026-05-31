package app.mori.reader.ui.pages.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.mori.reader.core.platform.rememberEpubPicker
import app.mori.reader.data.book.BookCategory
import app.mori.reader.data.book.BookInfo
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.BookshelfSortMode
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.audiobook.presentation.AudiobookIntent
import app.mori.reader.features.audiobook.presentation.AudiobookUiState
import app.mori.reader.features.bookshelf.presentation.BookshelfIntent
import app.mori.reader.features.bookshelf.presentation.BookshelfState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_category_manage
import app.mori.reader.shared.generated.resources.cd_import_book
import app.mori.reader.shared.generated.resources.cd_sort_by
import app.mori.reader.shared.generated.resources.home_sort_recent
import app.mori.reader.shared.generated.resources.home_sort_title
import app.mori.reader.shared.generated.resources.home_tab_all
import app.mori.reader.shared.generated.resources.tab_bookshelf
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.theme.MoriTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.window.WindowListPopup

val HomeHorizontalPadding = 12.dp
val HomeTabInnerPadding = 5.dp
val HomeContentTopSpacing = 12.dp
val HomePageSpacing = 12.dp

@Composable
fun HomePage(
    home: BookshelfState,
    settings: AppSettings,
    audiobook: AudiobookUiState,
    fixedPadding: PaddingValues,
    onBookshelfIntent: (BookshelfIntent) -> Unit,
    onAudiobookIntent: (AudiobookIntent) -> Unit,
    onOpenBook: (String) -> Unit,
) {
    var categoryManagerOpen by remember { mutableStateOf(false) }
    var createCategoryOpen by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<BookCategory?>(null) }
    var deletingCategory by remember { mutableStateOf<BookCategory?>(null) }
    var contextMenuBook by remember { mutableStateOf<BookInfo?>(null) }
    var contextMenuPage by remember { mutableStateOf<Int?>(null) }
    var categoryEditingBook by remember { mutableStateOf<BookInfo?>(null) }
    var deletingBook by remember { mutableStateOf<BookInfo?>(null) }
    var audiobookBook by remember { mutableStateOf<BookInfo?>(null) }
    val showSortPopup = remember { mutableStateOf(false) }
    val sortPopupHoldDown = remember { mutableStateOf(false) }
    val epubPicker =
        rememberEpubPicker { uris ->
            onBookshelfIntent(BookshelfIntent.BookshelfImportBooks(uris))
        }
    val onImportBooks = {
        if (!home.isImporting) {
            epubPicker()
        }
    }

    val categoryIds = listOf<String?>(null) + home.categories.map { it.id }
    val selectedIndex = categoryIds.indexOf(home.selectedCategoryId).takeIf { it >= 0 } ?: 0
    val tabs = listOf(stringResource(Res.string.home_tab_all)) + home.categories.map { it.name }
    val pagerState =
        rememberPagerState(
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
                    onBookshelfIntent(BookshelfIntent.SelectBookCategory(targetCategoryId))
                }
            }
    }

    val title = stringResource(Res.string.tab_bookshelf)
    val reduceMotion =
        MoriTheme.uiThemeEngine == UiThemeEngine.Material &&
            MoriTheme.materialEInkMode
    val onSelectTab: (Int) -> Unit = { index ->
        tabCoroutineScope.launch {
            if (reduceMotion) {
                pagerState.scrollToPage(index)
            } else {
                pagerState.animateScrollToPage(index)
            }
        }
    }
    val bookshelfContent:
        @Composable (PaddingValues) -> Unit =
        { paddingValues ->
            BookshelfContent(
                home = home,
                settings = settings,
                paddingValues = paddingValues,
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
                onOpenAudiobook = {
                    contextMenuBook?.let { book ->
                        audiobookBook = book
                        onAudiobookIntent(AudiobookIntent.OpenAudiobookManager(book.id))
                    }
                    contextMenuBook = null
                    contextMenuPage = null
                },
                onDeleteBook = {
                    deletingBook = contextMenuBook
                    contextMenuBook = null
                    contextMenuPage = null
                },
                onImportBooks = onImportBooks,
                onBookshelfIntent = onBookshelfIntent,
                onOpenBook = onOpenBook,
            )
        }

    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MoriPageScaffold(
                title = title,
                useSmallTopBar = true,
                revealTopBarOnReverseScroll = true,
                blurEnabled = settings.appearance.blurEnabled,
                fixedPadding = fixedPadding,
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
                                        text =
                                            when (option) {
                                                BookshelfSortMode.Recent -> stringResource(Res.string.home_sort_recent)
                                                BookshelfSortMode.Title -> stringResource(Res.string.home_sort_title)
                                            },
                                        optionSize = BookshelfSortMode.entries.size,
                                        isSelected = settings.bookshelf.sortMode == option,
                                        index = index,
                                        onSelectedIndexChange = {
                                            onBookshelfIntent(BookshelfIntent.SetBookshelfSortMode(option))
                                            dismiss?.invoke()
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onImportBooks) {
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
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(TabRowDefaults.TabRowWithContourHeight),
                        )
                    } else {
                        TabRowWithContour(
                            tabs = tabs,
                            selectedTabIndex = pagerState.currentPage.coerceAtMost(tabs.lastIndex),
                            onTabSelected = onSelectTab,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = HomeHorizontalPadding, vertical = 4.dp),
                            colors =
                                TabRowDefaults.tabRowColors(
                                    backgroundColor =
                                        if (settings.appearance.blurEnabled) {
                                            Color.Transparent
                                        } else {
                                            top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface
                                        },
                                ),
                        )
                    }
                },
            ) { paddingValues ->
                bookshelfContent(paddingValues)
            }
        }

        UiThemeEngine.Material -> {
            MaterialHomePageScaffold(
                title = title,
                settings = settings,
                fixedPadding = fixedPadding,
                isLoading = home.isLoading,
                tabs = tabs,
                selectedTabIndex = pagerState.currentPage.coerceAtMost(tabs.lastIndex),
                currentSortMode = settings.bookshelf.sortMode,
                onTabSelected = onSelectTab,
                onImportBook = onImportBooks,
                onManageCategories = { categoryManagerOpen = true },
                onSetSortMode = { option ->
                    onBookshelfIntent(BookshelfIntent.SetBookshelfSortMode(option))
                },
                content = bookshelfContent,
            )
        }
    }

    CategoryManagerSheet(
        show = categoryManagerOpen,
        categories = home.categories,
        onDismiss = { categoryManagerOpen = false },
        onCreate = { createCategoryOpen = true },
        onReorder = { categoryIds ->
            onBookshelfIntent(
                BookshelfIntent.ReorderBookCategories(
                    categoryIds,
                ),
            )
        },
        onRename = { category -> editingCategory = category },
        onDelete = { category -> deletingCategory = category },
    )

    CreateCategoryDialog(
        show = createCategoryOpen,
        onDismiss = { createCategoryOpen = false },
        onConfirm = { name ->
            onBookshelfIntent(BookshelfIntent.CreateBookCategory(name))
            createCategoryOpen = false
        },
    )

    RenameCategoryDialog(
        category = editingCategory,
        onDismiss = { editingCategory = null },
        onConfirm = { category, name ->
            onBookshelfIntent(BookshelfIntent.RenameBookCategory(category.id, name))
            editingCategory = null
        },
    )

    DeleteCategoryDialog(
        category = deletingCategory,
        onDismiss = { deletingCategory = null },
        onConfirm = { category ->
            onBookshelfIntent(BookshelfIntent.DeleteBookCategory(category.id))
            deletingCategory = null
        },
    )

    EditBookCategoriesSheet(
        book = categoryEditingBook,
        categories = home.categories,
        onDismiss = { categoryEditingBook = null },
        onCreate = { createCategoryOpen = true },
        onConfirm = { book, categoryIds ->
            onBookshelfIntent(BookshelfIntent.UpdateBookCategories(book.id, categoryIds))
            categoryEditingBook = null
        },
    )

    DeleteBookDialog(
        book = deletingBook,
        onDismiss = { deletingBook = null },
        onConfirm = { book ->
            onBookshelfIntent(BookshelfIntent.DeleteBook(book.id))
            deletingBook = null
        },
    )

    AudiobookManagerSheet(
        book = audiobookBook,
        audiobook = audiobook,
        onAudiobookIntent = onAudiobookIntent,
        onDismiss = {
            audiobookBook = null
            onAudiobookIntent(AudiobookIntent.CloseAudiobookManager)
        },
    )
}
