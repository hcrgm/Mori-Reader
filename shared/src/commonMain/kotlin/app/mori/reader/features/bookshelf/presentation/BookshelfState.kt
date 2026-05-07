package app.mori.reader.features.bookshelf.presentation

import app.mori.reader.data.book.BookCategory
import app.mori.reader.data.book.BookInfo
import app.mori.reader.ui.text.UiText

data class BookshelfState(
    val books: List<BookInfo> = emptyList(),
    val categories: List<BookCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val errorMessage: UiText? = null,
)

internal fun BookshelfState.withCatalog(
    books: List<BookInfo>,
    categories: List<BookCategory>,
    isLoading: Boolean = this.isLoading,
    isImporting: Boolean = this.isImporting,
    errorMessage: UiText? = this.errorMessage,
): BookshelfState {
    val categoryIds = categories.mapTo(mutableSetOf()) { it.id }
    val selectedCategoryId = this.selectedCategoryId.takeIf { it in categoryIds }
    return copy(
        books = books,
        categories = categories,
        selectedCategoryId = selectedCategoryId,
        isLoading = isLoading,
        isImporting = isImporting,
        errorMessage = errorMessage,
    )
}
