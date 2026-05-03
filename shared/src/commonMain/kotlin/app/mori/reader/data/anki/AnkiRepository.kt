package app.mori.reader.data.anki

import androidx.compose.runtime.Composable

interface AnkiRepository {
    suspend fun ping(endpoint: String): Boolean
    suspend fun fetchDecksAndModels(endpoint: String): AnkiCatalog
    suspend fun canAdd(settings: AnkiSettings, card: AnkiCardPayload): AnkiCanAddResult
    suspend fun addNote(settings: AnkiSettings, card: AnkiCardPayload): Long
    suspend fun sync(endpoint: String)
}

data class AnkiCanAddResult(
    val canAdd: Boolean,
    val message: String = "",
)

@Composable
expect fun rememberAnkiRepository(): AnkiRepository
