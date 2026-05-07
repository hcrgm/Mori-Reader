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
import app.mori.reader.data.audiobook.AudiobookAssetInfo
import app.mori.reader.data.audiobook.AudiobookAssetType
import app.mori.reader.features.audiobook.presentation.AudiobookState
import app.mori.reader.features.bookshelf.presentation.HomeState
import app.mori.reader.data.audiobook.AudiobookStorageMode
import app.mori.reader.core.platform.rememberAudiobookAudioPicker
import app.mori.reader.core.platform.rememberAudiobookSubtitlePicker
import app.mori.reader.core.platform.rememberEpubPicker
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.BookshelfSortMode
import app.mori.reader.features.audiobook.presentation.AudiobookIntent
import app.mori.reader.features.bookshelf.presentation.BookshelfIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.audiobook_audio_title
import app.mori.reader.shared.generated.resources.audiobook_delete
import app.mori.reader.shared.generated.resources.audiobook_import_audio
import app.mori.reader.shared.generated.resources.audiobook_import_subtitle
import app.mori.reader.shared.generated.resources.audiobook_missing
import app.mori.reader.shared.generated.resources.audiobook_reimport
import app.mori.reader.shared.generated.resources.audiobook_storage_copy
import app.mori.reader.shared.generated.resources.audiobook_storage_reference
import app.mori.reader.shared.generated.resources.audiobook_storage_title
import app.mori.reader.shared.generated.resources.audiobook_subtitle_cue_count
import app.mori.reader.shared.generated.resources.audiobook_subtitle_parsed
import app.mori.reader.shared.generated.resources.audiobook_subtitle_storage_note
import app.mori.reader.shared.generated.resources.audiobook_subtitle_title
import app.mori.reader.shared.generated.resources.audiobook_title
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
import app.mori.reader.shared.generated.resources.home_audiobook
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
import app.mori.reader.shared.generated.resources.home_sort_recent
import app.mori.reader.shared.generated.resources.home_sort_title
import app.mori.reader.shared.generated.resources.home_tab_all
import app.mori.reader.shared.generated.resources.tab_bookshelf
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.settings.SettingSlider
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
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SpinnerEntry
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
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.window.WindowListPopup
import androidx.compose.foundation.lazy.grid.items as gridItems

val HomeHorizontalPadding = 12.dp
val HomeTabInnerPadding = 5.dp
val HomeContentTopSpacing = 12.dp
val HomePageSpacing = 12.dp

@Composable
fun HomePage(
    home: HomeState,
    settings: AppSettings,
    audiobook: AudiobookState,
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
    val epubPicker = rememberEpubPicker { uris ->
        onBookshelfIntent(BookshelfIntent.BookshelfImportBooks(uris))
    }

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
                    onBookshelfIntent(BookshelfIntent.SelectBookCategory(targetCategoryId))
                }
            }
    }

    MoriPageScaffold(
        title = stringResource(Res.string.tab_bookshelf),
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
                                text = when (option) {
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
                        backgroundColor = if (settings.appearance.blurEnabled) Color.Transparent else MiuixTheme.colorScheme.surface,
                    ),
                )
            }
        },
    ) { paddingValues, scrollBehavior ->
        BookshelfContent(
            home = home,
            settings = settings,
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
            onBookshelfIntent = onBookshelfIntent,
            onOpenBook = onOpenBook,
        )
    }

    CategoryManagerSheet(
        show = categoryManagerOpen,
        categories = home.categories,
        onDismiss = { categoryManagerOpen = false },
        onCreate = { createCategoryOpen = true },
        onReorder = { categoryIds ->
            onBookshelfIntent(
                BookshelfIntent.ReorderBookCategories(
                    categoryIds
                )
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
