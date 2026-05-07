package app.mori.reader.ui

import app.mori.reader.ui.text.UiText

sealed interface AppEffect {
    data class ShowMessage(
        val message: UiText,
    ) : AppEffect

    data class OpenReader(
        val bookId: String,
    ) : AppEffect
}
