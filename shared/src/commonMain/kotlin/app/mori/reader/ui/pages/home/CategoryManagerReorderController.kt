package app.mori.reader.ui.pages.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import app.mori.reader.data.book.BookCategory
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

internal class CategoryManagerReorderController(
    val listState: LazyListState,
    val reorderableState: ReorderableLazyListState,
    val localCategories: List<BookCategory>,
)

@Composable
internal fun rememberCategoryManagerReorderController(
    show: Boolean,
    categories: List<BookCategory>,
    onReorder: (List<String>) -> Unit,
): CategoryManagerReorderController {
    val hapticFeedback = LocalHapticFeedback.current
    val onReorderState by rememberUpdatedState(onReorder)
    var localCategories by remember(show) { mutableStateOf(categories) }

    LaunchedEffect(categories) {
        localCategories = categories
    }

    val listState = rememberLazyListState()
    val reorderableState =
        rememberReorderableLazyListState(listState) { from, to ->
            localCategories =
                localCategories.toMutableList().apply {
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

    return CategoryManagerReorderController(
        listState = listState,
        reorderableState = reorderableState,
        localCategories = localCategories,
    )
}
