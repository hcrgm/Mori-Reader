package app.mori.reader.features.dictionary.presentation

import app.mori.reader.data.dictionary.DictionaryLookupEntry
import app.mori.reader.features.lookup.presentation.ReaderLookupState
import app.mori.reader.ui.text.UiText

data class DictionaryState(
    val query: String = "",
    val lastQuery: String = "",
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val entries: List<DictionaryLookupEntry> = emptyList(),
    val dictionaryStyles: Map<String, String> = emptyMap(),
    val errorMessage: UiText? = null,
    val popupStack: List<ReaderLookupState> = emptyList(),
)
