package app.mori.reader.features.bookshelf.presentation

import app.mori.reader.data.settings.BookshelfSortMode

sealed interface BookshelfIntent {
    data class BookshelfImportBooks(
        val uriStrings: List<String>,
    ) : BookshelfIntent

    data class SelectBookCategory(
        val categoryId: String?,
    ) : BookshelfIntent

    data class CreateBookCategory(
        val name: String,
    ) : BookshelfIntent

    data class RenameBookCategory(
        val id: String,
        val name: String,
    ) : BookshelfIntent

    data class ReorderBookCategories(
        val categoryIds: List<String>,
    ) : BookshelfIntent

    data class DeleteBookCategory(
        val id: String,
    ) : BookshelfIntent

    data class UpdateBookCategories(
        val bookId: String,
        val categoryIds: List<String>,
    ) : BookshelfIntent

    data class DeleteBook(
        val id: String,
    ) : BookshelfIntent

    data object DismissHomeError : BookshelfIntent

    data class SetBookshelfSortMode(
        val mode: BookshelfSortMode,
    ) : BookshelfIntent
}
