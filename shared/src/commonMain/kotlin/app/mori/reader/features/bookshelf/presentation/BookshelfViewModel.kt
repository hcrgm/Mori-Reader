package app.mori.reader.features.bookshelf.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mori.reader.data.book.BookCatalog
import app.mori.reader.data.book.BookRepository
import app.mori.reader.data.settings.BookshelfSortMode
import app.mori.reader.data.settings.SettingsRepository
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.error_book_import_failed
import app.mori.reader.shared.generated.resources.error_bookshelf_load_failed
import app.mori.reader.shared.generated.resources.error_category_operation_failed
import app.mori.reader.shared.generated.resources.error_category_sort_failed
import app.mori.reader.ui.text.uiTextOr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookshelfViewModel(
    private val bookRepository: BookRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BookshelfState(isLoading = true))
    val state = _state.asStateFlow()

    private var sortMode: BookshelfSortMode = BookshelfSortMode.Recent

    init {
        viewModelScope.launch {
            runCatching { bookRepository.loadCatalog() }
                .onSuccess(::applyCatalog)
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.uiTextOr(Res.string.error_bookshelf_load_failed),
                        )
                    }
                }
        }
        viewModelScope.launch {
            bookRepository.catalog.filterNotNull().collect(::applyCatalog)
        }
    }

    fun onIntent(intent: BookshelfIntent) {
        when (intent) {
            is BookshelfIntent.BookshelfImportBooks -> {
                importBooks(intent.uriStrings)
            }

            is BookshelfIntent.SelectBookCategory -> {
                _state.update {
                    it.copy(selectedCategoryId = intent.categoryId)
                }
            }

            is BookshelfIntent.CreateBookCategory -> {
                mutateBookCatalog {
                    bookRepository.createCategory(intent.name)
                }
            }

            is BookshelfIntent.RenameBookCategory -> {
                mutateBookCatalog {
                    bookRepository.renameCategory(intent.id, intent.name)
                }
            }

            is BookshelfIntent.ReorderBookCategories -> {
                viewModelScope.launch {
                    runCatching { bookRepository.reorderCategories(intent.categoryIds) }
                        .onSuccess { catalog ->
                            applyCatalog(catalog)
                        }.onFailure { throwable ->
                            _state.update {
                                it.copy(errorMessage = throwable.uiTextOr(Res.string.error_category_sort_failed))
                            }
                        }
                }
            }

            is BookshelfIntent.DeleteBookCategory -> {
                mutateBookCatalog {
                    bookRepository.deleteCategory(intent.id)
                }
            }

            is BookshelfIntent.UpdateBookCategories -> {
                mutateBookCatalog {
                    bookRepository.updateBookCategories(intent.bookId, intent.categoryIds)
                }
            }

            is BookshelfIntent.DeleteBook -> {
                mutateBookCatalog {
                    bookRepository.deleteBook(intent.id)
                }
            }

            is BookshelfIntent.DismissHomeError -> {
                _state.update {
                    it.copy(errorMessage = null)
                }
            }

            is BookshelfIntent.SetBookshelfSortMode -> {
                setSortMode(intent.mode)
            }
        }
    }

    private fun setSortMode(mode: BookshelfSortMode) {
        if (sortMode == mode) return
        sortMode = mode
        viewModelScope.launch {
            settingsRepository.setBookshelfSortMode(mode)
        }
    }

    private fun importBooks(uriStrings: List<String>) {
        if (uriStrings.isEmpty()) return
        _state.update { it.copy(isImporting = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { bookRepository.importBooks(uriStrings) }
                .onSuccess { catalog ->
                    _state.update { state ->
                        state.withCatalog(
                            books = catalog.books,
                            categories = catalog.categories,
                            isImporting = false,
                            errorMessage = null,
                        )
                    }
                }.onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isImporting = false,
                            errorMessage = throwable.uiTextOr(Res.string.error_book_import_failed),
                        )
                    }
                }
        }
    }

    private fun mutateBookCatalog(block: suspend () -> BookCatalog) {
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { catalog ->
                    applyCatalog(catalog)
                }.onFailure { throwable ->
                    _state.update {
                        it.copy(errorMessage = throwable.uiTextOr(Res.string.error_category_operation_failed))
                    }
                }
        }
    }

    private fun applyCatalog(catalog: BookCatalog) {
        _state.update { state ->
            state.withCatalog(
                books = catalog.books,
                categories = catalog.categories,
                isLoading = false,
                errorMessage = null,
            )
        }
    }
}
