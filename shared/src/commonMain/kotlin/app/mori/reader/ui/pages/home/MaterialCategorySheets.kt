package app.mori.reader.ui.pages.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.book.BookCategory
import app.mori.reader.ui.components.material.MaterialModalBottomSheet
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_confirm
import app.mori.reader.shared.generated.resources.btn_delete
import app.mori.reader.shared.generated.resources.cd_add_category
import app.mori.reader.shared.generated.resources.cd_category_manage
import app.mori.reader.shared.generated.resources.cd_drag_sort
import app.mori.reader.shared.generated.resources.cd_rename
import app.mori.reader.shared.generated.resources.home_category_name_label
import app.mori.reader.shared.generated.resources.home_delete_category
import app.mori.reader.shared.generated.resources.home_delete_category_confirm
import app.mori.reader.shared.generated.resources.home_no_categories
import app.mori.reader.shared.generated.resources.home_rename_category
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialCategoryManagerSheet(
    show: Boolean,
    categories: List<BookCategory>,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    onReorder: (List<String>) -> Unit,
    onRename: (BookCategory) -> Unit,
    onDelete: (BookCategory) -> Unit,
) {
    if (!show) return

    val reorderController =
        rememberCategoryManagerReorderController(
            show = show,
            categories = categories,
            onReorder = onReorder,
        )

    MaterialModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.cd_category_manage),
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(onClick = onCreate) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(Res.string.cd_add_category),
                    )
                }
            }

            if (reorderController.localCategories.isEmpty()) {
                Text(
                    text = stringResource(Res.string.home_no_categories),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    state = reorderController.listState,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = reorderController.localCategories,
                        key = { it.id },
                    ) { category ->
                        MaterialCategoryManageItem(
                            category = category,
                            reorderableState = reorderController.reorderableState,
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
private fun LazyItemScope.MaterialCategoryManageItem(
    category: BookCategory,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    ReorderableItem(
        state = reorderableState,
        key = category.id,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(18.dp),
            modifier =
                Modifier
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
                    ).animateItem(),
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = category.name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = stringResource(Res.string.cd_drag_sort),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(onClick = onRename) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(Res.string.cd_rename),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(Res.string.btn_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MaterialCreateCategoryDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    if (!show) return

    var name by remember(show) { mutableStateOf("") }

    MaterialCategoryNameDialog(
        title = stringResource(Res.string.cd_add_category),
        name = name,
        onNameChange = { name = it },
        onDismiss = onDismiss,
        onConfirm = { onConfirm(name) },
    )
}

@Composable
internal fun MaterialRenameCategoryDialog(
    category: BookCategory?,
    onDismiss: () -> Unit,
    onConfirm: (BookCategory, String) -> Unit,
) {
    val current = category ?: return
    var name by remember(current.id) { mutableStateOf(current.name) }

    MaterialCategoryNameDialog(
        title = stringResource(Res.string.home_rename_category),
        name = name,
        onNameChange = { name = it },
        onDismiss = onDismiss,
        onConfirm = { onConfirm(current, name) },
    )
}

@Composable
private fun MaterialCategoryNameDialog(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(text = stringResource(Res.string.home_category_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(Res.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.btn_cancel))
            }
        },
    )
}

@Composable
internal fun MaterialDeleteCategoryDialog(
    category: BookCategory?,
    onDismiss: () -> Unit,
    onConfirm: (BookCategory) -> Unit,
) {
    val current = category ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.home_delete_category)) },
        text = {
            Text(
                text = stringResource(Res.string.home_delete_category_confirm, current.name),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(current) },
            ) {
                Text(text = stringResource(Res.string.btn_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.btn_cancel))
            }
        },
    )
}
