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
import app.mori.reader.shared.generated.resources.home_delete_category_confirm
import app.mori.reader.shared.generated.resources.home_empty_bookshelf
import app.mori.reader.shared.generated.resources.home_empty_category
import app.mori.reader.shared.generated.resources.home_importing
import app.mori.reader.shared.generated.resources.home_loading
import app.mori.reader.shared.generated.resources.home_no_categories
import app.mori.reader.shared.generated.resources.home_no_categories_available
import app.mori.reader.shared.generated.resources.home_rename_category
import app.mori.reader.shared.generated.resources.home_tab_all
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
@Composable
fun CategoryManagerSheet(
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
fun LazyItemScope.CategoryManageItem(
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
fun CreateCategoryDialog(
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
fun RenameCategoryDialog(
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
fun DeleteCategoryDialog(
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
            text = stringResource(Res.string.home_delete_category_confirm, current.name),
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
