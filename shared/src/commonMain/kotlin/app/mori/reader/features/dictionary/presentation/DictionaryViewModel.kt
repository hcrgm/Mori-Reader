package app.mori.reader.features.dictionary.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mori.reader.data.dictionary.DictionaryRepository
import app.mori.reader.data.settings.SettingsRepository
import app.mori.reader.features.dictionary.domain.DictionaryLookupUseCase
import app.mori.reader.features.lookup.presentation.ReaderSelectionRect
import app.mori.reader.features.lookup.presentation.createLookupStackEntry
import app.mori.reader.features.lookup.presentation.dismissLookupStack
import app.mori.reader.features.lookup.presentation.withLookupError
import app.mori.reader.features.lookup.presentation.withLookupResult
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.error_search_failed
import app.mori.reader.ui.text.uiTextOr
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DictionaryViewModel(
    private val dictionaryRepository: DictionaryRepository,
    private val lookupText: DictionaryLookupUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DictionaryState())
    val state = _state.asStateFlow()

    private var dictionarySearchJob: Job? = null
    private var readerLookupNextId = 0

    private var maxResults: Int = 16

    init {
        viewModelScope.launch {
            runCatching { dictionaryRepository.loadDictionaries() }
        }
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                maxResults = settings.dictionary.maxResults
            }
        }
    }

    fun onIntent(intent: DictionaryIntent) {
        when (intent) {
            is DictionaryIntent.UpdateQuery -> {
                if (_state.value.query == intent.query) return
                _state.update {
                    it.copy(
                        query = intent.query,
                        popupStack = emptyList(),
                    )
                }
                scheduleDictionarySearch()
            }

            DictionaryIntent.ClearQuery -> {
                if (_state.value.query.isEmpty()) return
                dictionarySearchJob?.cancel()
                _state.update {
                    it.copy(
                        query = "",
                        lastQuery = "",
                        isSearching = false,
                        hasSearched = false,
                        entries = emptyList(),
                        dictionaryStyles = emptyMap(),
                        errorMessage = null,
                        popupStack = emptyList(),
                    )
                }
            }

            DictionaryIntent.ExecuteSearch -> {
                runDictionarySearch(immediate = true)
            }

            is DictionaryIntent.PopupTextSelected -> {
                lookupDictionaryPopup(
                    text = intent.text,
                    rect = intent.rect,
                    parentIndex = intent.parentIndex,
                )
            }

            is DictionaryIntent.DismissPopup -> {
                dismissDictionaryPopup(intent.index)
            }
        }
    }

    private fun scheduleDictionarySearch() {
        dictionarySearchJob?.cancel()
        val query = _state.value.query.trim()
        if (query.isEmpty()) {
            _state.update {
                it.copy(
                    lastQuery = "",
                    isSearching = false,
                    hasSearched = false,
                    entries = emptyList(),
                    dictionaryStyles = emptyMap(),
                    errorMessage = null,
                )
            }
            return
        }
        dictionarySearchJob =
            viewModelScope.launch {
                delay(250)
                searchDictionary(query)
            }
    }

    private fun runDictionarySearch(immediate: Boolean) {
        dictionarySearchJob?.cancel()
        val query = _state.value.query.trim()
        if (query.isEmpty()) {
            scheduleDictionarySearch()
            return
        }
        dictionarySearchJob =
            viewModelScope.launch {
                if (!immediate) delay(250)
                searchDictionary(query)
            }
    }

    private suspend fun searchDictionary(query: String) {
        _state.update {
            it.copy(
                lastQuery = query,
                isSearching = true,
                hasSearched = true,
                errorMessage = null,
            )
        }
        runCatching { lookupText(query, maxResults) }
            .onSuccess { result ->
                _state.update {
                    if (it.query.trim() != query) return@update it
                    it.copy(
                        isSearching = false,
                        entries = result.entries,
                        dictionaryStyles = result.styles,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _state.update {
                    if (it.query.trim() != query) return@update it
                    it.copy(
                        isSearching = false,
                        entries = emptyList(),
                        dictionaryStyles = emptyMap(),
                        errorMessage = throwable.uiTextOr(Res.string.error_search_failed),
                    )
                }
            }
    }

    private fun lookupDictionaryPopup(
        text: String,
        rect: ReaderSelectionRect?,
        parentIndex: Int? = null,
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            dismissDictionaryPopup(parentIndex)
            return
        }
        val lookupId = ++readerLookupNextId
        _state.update {
            it.copy(
                popupStack =
                    createLookupStackEntry(
                        stack = it.popupStack,
                        parentIndex = parentIndex,
                        lookupId = lookupId,
                        text = trimmed,
                        sentence = trimmed,
                        rect = rect,
                    ),
            )
        }
        viewModelScope.launch {
            runCatching { lookupText(trimmed, maxResults) }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            popupStack =
                                it.popupStack.withLookupResult(
                                    lookupId = lookupId,
                                    entries = result.entries,
                                    dictionaryStyles = result.styles,
                                ),
                        )
                    }
                }.onFailure { throwable ->
                    _state.update {
                        it.copy(
                            popupStack =
                                it.popupStack.withLookupError(
                                    lookupId = lookupId,
                                    errorMessage = throwable.uiTextOr(Res.string.error_search_failed),
                                ),
                        )
                    }
                }
        }
    }

    private fun dismissDictionaryPopup(index: Int?) {
        _state.update { state ->
            state.copy(popupStack = dismissLookupStack(state.popupStack, index))
        }
    }
}
