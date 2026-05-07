package app.mori.reader.ui

sealed interface AppIntent {
    data class OpenBook(
        val id: String,
    ) : AppIntent
}
