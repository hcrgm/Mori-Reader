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
    val importProgress: BookImportUiProgress? = null,
    val importSummary: BookImportSummary? = null,
    val errorMessage: UiText? = null,
)

internal fun BookshelfState.withCatalog(
    books: List<BookInfo>,
    categories: List<BookCategory>,
    isLoading: Boolean = this.isLoading,
    isImporting: Boolean = this.isImporting,
    importProgress: BookImportUiProgress? = this.importProgress,
    importSummary: BookImportSummary? = this.importSummary,
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
        importProgress = importProgress,
        importSummary = importSummary,
        errorMessage = errorMessage,
    )
}

data class BookImportUiProgress(
    val currentIndex: Int,
    val totalCount: Int,
    val currentName: String,
)

data class BookImportSummary(
    val successCount: Int,
    val failureCount: Int,
    val failures: List<BookImportFailureUiItem>,
)

data class BookImportFailureUiItem(
    val fileName: String,
)
