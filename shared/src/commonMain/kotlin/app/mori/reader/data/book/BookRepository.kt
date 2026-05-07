package app.mori.reader.data.book

import kotlinx.coroutines.flow.StateFlow

interface BookRepository {
    val catalog: StateFlow<BookCatalog?>

    suspend fun loadCatalog(): BookCatalog

    suspend fun loadReaderBook(bookId: String): ReaderBook

    suspend fun importBooks(uriStrings: List<String>): BookCatalog

    suspend fun saveReaderProgress(
        bookId: String,
        chapterIndex: Int,
        chapterProgress: Double,
    ): BookCatalog

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
