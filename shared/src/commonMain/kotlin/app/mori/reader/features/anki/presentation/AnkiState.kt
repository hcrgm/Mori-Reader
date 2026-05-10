package app.mori.reader.features.anki.presentation

import app.mori.reader.data.anki.AnkiDeck
import app.mori.reader.data.anki.AnkiNoteType
import app.mori.reader.data.anki.AnkiSettings
import app.mori.reader.ui.text.UiText

data class AnkiState(
    val settings: AnkiSettings = AnkiSettings(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isFetching: Boolean = false,
    val isAddingNote: Boolean = false,
    val decks: List<AnkiDeck> = emptyList(),
    val noteTypes: List<AnkiNoteType> = emptyList(),
    val duplicateExpression: String? = null,
    val errorMessage: UiText? = null,
)
