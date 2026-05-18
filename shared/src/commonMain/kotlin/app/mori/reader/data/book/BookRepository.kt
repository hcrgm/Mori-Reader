package app.mori.reader.data.book

import kotlinx.coroutines.flow.StateFlow

interface BookRepository {
    val catalog: StateFlow<BookCatalog?>

    suspend fun loadCatalog(): BookCatalog

    suspend fun loadReaderBook(bookId: String): ReaderBook

    suspend fun importBooks(
        uriStrings: List<String>,
        onProgress: (BookImportProgress) -> Unit = {},
    ): BookImportResult

    suspend fun saveReaderProgress(
        bookId: String,
        bookmark: ReaderBookmark,
    )

    suspend fun createCategory(name: String): BookCatalog

    suspend fun renameCategory(
        id: String,
        name: String,
    ): BookCatalog

    suspend fun reorderCategories(categoryIds: List<String>): BookCatalog

    suspend fun deleteCategory(id: String): BookCatalog

    suspend fun updateBookCategories(
        bookId: String,
        categoryIds: List<String>,
    ): BookCatalog

    suspend fun deleteBook(bookId: String): BookCatalog
}

data class BookImportProgress(
    val currentIndex: Int,
    val totalCount: Int,
    val currentName: String,
)

data class BookImportResult(
    val catalog: BookCatalog,
    val successCount: Int,
    val failures: List<BookImportFailureItem>,
)

data class BookImportFailureItem(
    val fileName: String,
)
