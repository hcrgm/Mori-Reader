package app.mori.reader.ui.pages.home

import androidx.compose.runtime.Composable
import app.mori.reader.data.book.BookCategory
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.ui.theme.MoriTheme

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
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixCategoryManagerSheet(
                show = show,
                categories = categories,
                onDismiss = onDismiss,
                onCreate = onCreate,
                onReorder = onReorder,
                onRename = onRename,
                onDelete = onDelete,
            )
        }

        UiThemeEngine.Material -> {
            MaterialCategoryManagerSheet(
                show = show,
                categories = categories,
                onDismiss = onDismiss,
                onCreate = onCreate,
                onReorder = onReorder,
                onRename = onRename,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
fun CreateCategoryDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixCreateCategoryDialog(
                show = show,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
            )
        }

        UiThemeEngine.Material -> {
            MaterialCreateCategoryDialog(
                show = show,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
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
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixRenameCategoryDialog(
                category = category,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
            )
        }

        UiThemeEngine.Material -> {
            MaterialRenameCategoryDialog(
                category = category,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
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
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixDeleteCategoryDialog(
                category = category,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
            )
        }

        UiThemeEngine.Material -> {
            MaterialDeleteCategoryDialog(
                category = category,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
            )
        }
    }
}
