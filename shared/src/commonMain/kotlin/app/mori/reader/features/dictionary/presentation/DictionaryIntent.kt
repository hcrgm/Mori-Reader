package app.mori.reader.features.dictionary.presentation

import app.mori.reader.features.lookup.presentation.ReaderSelectionRect

sealed interface DictionaryIntent {
    data class UpdateQuery(
        val query: String,
    ) : DictionaryIntent

    data object ExecuteSearch : DictionaryIntent

    data object ClearQuery : DictionaryIntent

    data class PopupTextSelected(
        val parentIndex: Int? = null,
        val text: String,
        val rect: ReaderSelectionRect? = null,
    ) : DictionaryIntent

    data class DismissPopup(
        val index: Int? = null,
    ) : DictionaryIntent
}
