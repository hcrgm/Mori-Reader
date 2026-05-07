package app.mori.reader.features.lookup.presentation

import app.mori.reader.data.dictionary.DictionaryLookupEntry
import app.mori.reader.ui.text.UiText

data class ReaderLookupState(
    val id: Int = 0,
    val selectedText: String = "",
    val sentence: String = "",
    val rect: ReaderSelectionRect? = null,
    val isSearching: Boolean = false,
    val entries: List<DictionaryLookupEntry> = emptyList(),
    val dictionaryStyles: Map<String, String> = emptyMap(),
    val highlightLength: Int? = null,
    val sasayakiCueId: String? = null,
    val errorMessage: UiText? = null,
) {
    val visible: Boolean
        get() = selectedText.isNotBlank() || isSearching || errorMessage != null
}

data class ReaderSelectionRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val normalizedOffset: Int? = null,
)
