package app.mori.reader.data.anki

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AnkiConnectTransport(
    context: Context,
    private val httpClient: HttpClient =
        HttpClient(CIO) {
            install(HttpTimeout)
            followRedirects = true
            expectSuccess = false
        },
) : AnkiTransport {
    private val notePreparer = AnkiNotePreparer(context, httpClient)
    private val json = Json { ignoreUnknownKeys = true }
    override val mode: AnkiConnectionMode = AnkiConnectionMode.AnkiConnect

    override suspend fun ping(settings: AnkiSettings): Boolean {
        requireVersion(settings)
        return true
    }

    override suspend fun fetchDecksAndModels(settings: AnkiSettings): AnkiFetchResult {
        requireVersion(settings)
        val decks =
            call(settings, AnkiConnectPayloadBuilder.deckNames())
                .jsonArray
                .map { name ->
                    val deckName = name.jsonPrimitive.content
                    AnkiDeck(id = deckName, name = deckName)
                }
        val noteTypes =
            call(settings, AnkiConnectPayloadBuilder.modelNames())
                .jsonArray
                .map { model ->
                    val modelName = model.jsonPrimitive.content
                    val fields =
                        call(settings, AnkiConnectPayloadBuilder.modelFieldNames(modelName))
                            .jsonArray
                            .map { AnkiField(name = it.jsonPrimitive.content) }
                    AnkiNoteType(id = modelName, name = modelName, fields = fields)
                }
        return AnkiFetchResult(decks = decks, noteTypes = noteTypes)
    }

    override suspend fun addNote(
        settings: AnkiSettings,
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    ) {
        val preparedNote = notePreparer.prepare(settings, content, context)
        uploadEmbeddedMedia(settings, preparedNote)
        call(settings, AnkiConnectPayloadBuilder.addNote(settings, preparedNote))
    }

    override suspend fun checkDuplicate(
        settings: AnkiSettings,
        expression: String,
    ): Boolean {
        val result =
            call(
                settings,
                AnkiConnectPayloadBuilder.canAddNotesWithErrorDetail(settings, expression),
            )
        val first = result.jsonArray.firstOrNull()?.jsonObject ?: return false
        return first["canAdd"]?.jsonPrimitive?.booleanOrNull == false
    }

    override suspend fun sync(settings: AnkiSettings) {
        call(settings, AnkiConnectPayloadBuilder.sync())
    }

    private suspend fun uploadEmbeddedMedia(
        settings: AnkiSettings,
        preparedNote: PreparedAnkiNote,
    ) {
        preparedNote.dictionaryMedia
            .filter { it.dataBase64.isNotBlank() }
            .forEach { media ->
                call(settings, AnkiConnectPayloadBuilder.storeMediaFile(media))
            }
    }

    private suspend fun requireVersion(settings: AnkiSettings) {
        val version = call(settings, AnkiConnectPayloadBuilder.version()).jsonPrimitive.intOrNull
        check(version == ANKI_CONNECT_VERSION) { "AnkiConnect v6 is required" }
    }

    private suspend fun call(
        settings: AnkiSettings,
        payload: String,
    ): JsonElement {
        val responseText =
            httpClient
                .post(settings.ankiConnect.url) {
                    timeout {
                        requestTimeoutMillis = settings.ankiConnect.timeoutMillis.toLong()
                        connectTimeoutMillis = settings.ankiConnect.timeoutMillis.toLong()
                        socketTimeoutMillis = settings.ankiConnect.timeoutMillis.toLong()
                    }
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }.bodyAsText()
        val envelope = json.parseToJsonElement(responseText).jsonObject
        val error = envelope["error"]?.takeUnless { it is JsonNull }
        check(error == null) { error?.jsonPrimitive?.content ?: "AnkiConnect request failed" }
        return envelope["result"] ?: JsonNull
    }

    private companion object {
        const val ANKI_CONNECT_VERSION = 6
    }
}
