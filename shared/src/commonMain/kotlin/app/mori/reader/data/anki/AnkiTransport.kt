package app.mori.reader.data.anki

interface AnkiTransport {
    val mode: AnkiConnectionMode

    suspend fun ping(settings: AnkiSettings): Boolean

    suspend fun fetchDecksAndModels(settings: AnkiSettings): AnkiFetchResult

    suspend fun addNote(
        settings: AnkiSettings,
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    )

    suspend fun checkDuplicate(
        settings: AnkiSettings,
        expression: String,
    ): Boolean

    suspend fun sync(settings: AnkiSettings)

    fun recordAddedExpression(expression: String) {}
}

class AnkiLocalApiUnavailableException(
    message: String,
) : IllegalStateException(message)
