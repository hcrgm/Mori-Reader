package app.mori.reader.ui.pages.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.mori.reader.data.book.BookCategory
import app.mori.reader.data.book.BookInfo
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_confirm
import app.mori.reader.shared.generated.resources.btn_delete
import app.mori.reader.shared.generated.resources.home_adjust_category
import app.mori.reader.shared.generated.resources.home_delete_book
import app.mori.reader.shared.generated.resources.home_delete_book_confirm
import app.mori.reader.shared.generated.resources.home_no_categories_available
import app.mori.reader.ui.theme.MoriTheme
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun EditBookCategoriesSheet(
    book: BookInfo?,
    categories: List<BookCategory>,
    onDismiss: () -> Unit,
    onConfirm: (BookInfo, List<String>) -> Unit,
) {
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixEditBookCategoriesSheet(
                book = book,
                categories = categories,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
            )
        }

        UiThemeEngine.Material -> {
            MaterialEditBookCategoriesSheet(
                book = book,
                categories = categories,
                onDismiss = onDismiss,
                onConfirm = onConfirm,
            )
        }
    }
}

@Composable
private fun MiuixEditBookCategoriesSheet(
    book: BookInfo?,
    categories: List<BookCategory>,
    onDismiss: () -> Unit,
    onConfirm: (BookInfo, List<String>) -> Unit,
) {
    var selectedCategoryIds by remember(book?.id, categories) {
        mutableStateOf(
            book
                ?.categoryIds
                ?.filter { categoryId -> categories.any { it.id == categoryId } }
                ?.toSet()
                .orEmpty(),
        )
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
                                selectedCategoryIds =
                                    if (checked) {
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
fun DeleteBookDialog(
    book: BookInfo?,
    onDismiss: () -> Unit,
    onConfirm: (BookInfo) -> Unit,
) {
    if (MoriTheme.uiThemeEngine == UiThemeEngine.Material) {
        MaterialDeleteBookDialog(
            book = book,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
        return
    }

    WindowDialog(
        title = stringResource(Res.string.home_delete_book),
        show = book != null,
        onDismissRequest = onDismiss,
    ) {
        Text(
            text = stringResource(Res.string.home_delete_book_confirm, book?.title.orEmpty()),
            modifier =
                Modifier
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

@Composable
private fun MaterialDeleteBookDialog(
    book: BookInfo?,
    onDismiss: () -> Unit,
    onConfirm: (BookInfo) -> Unit,
) {
    val current = book ?: return

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text(text = stringResource(Res.string.home_delete_book)) },
        text = {
            androidx.compose.material3.Text(
                text = stringResource(Res.string.home_delete_book_confirm, current.title),
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            androidx.compose.material3.Button(onClick = { onConfirm(current) }) {
                androidx.compose.material3.Text(text = stringResource(Res.string.btn_delete))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text(text = stringResource(Res.string.btn_cancel))
            }
        },
    )
}
