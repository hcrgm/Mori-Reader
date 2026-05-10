package app.mori.reader.data.anki

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object AnkiConnectPayloadBuilder {
    private val json = Json { encodeDefaults = true }

    fun version(): String = envelope(action = "version")

    fun deckNames(): String = envelope(action = "deckNames")

    fun modelNames(): String = envelope(action = "modelNames")

    fun modelFieldNames(modelName: String): String =
        envelope(
            action = "modelFieldNames",
            params = buildJsonObject { put("modelName", modelName) },
        )

    fun sync(): String = envelope(action = "sync")

    fun storeMediaFile(media: DictionaryMedia): String =
        envelope(
            action = "storeMediaFile",
            params =
                buildJsonObject {
                    put("filename", media.fileName)
                    put("data", media.dataBase64.orEmpty())
                },
        )

    fun storeMediaFile(media: PreparedAnkiMedia): String =
        envelope(
            action = "storeMediaFile",
            params =
                buildJsonObject {
                    put("filename", media.fileName)
                    put("data", media.dataBase64)
                },
        )

    fun addNote(
        settings: AnkiSettings,
        preparedNote: PreparedAnkiNote,
    ): String =
        envelope(
            action = "addNote",
            params = buildJsonObject { put("note", note(settings, preparedNote)) },
        )

    fun canAddNotesWithErrorDetail(
        settings: AnkiSettings,
        expression: String,
    ): String =
        envelope(
            action = "canAddNotesWithErrorDetail",
            params =
                buildJsonObject {
                    put(
                        "notes",
                        buildJsonArray {
                            add(
                                note(
                                    settings,
                                    PreparedAnkiNote(fields = mapOf(firstMappedField(settings) to expression)),
                                ),
                            )
                        },
                    )
                },
        )

    private fun note(
        settings: AnkiSettings,
        preparedNote: PreparedAnkiNote,
    ): JsonObject {
        val deckName = settings.selectedDeck.requireConfigured("Anki deck")
        val modelName = settings.selectedNoteType.requireConfigured("Anki note type")
        val fields =
            preparedNote.fields
                .filterKeys { it !in preparedNote.mediaFieldNames }
                .mapValues { (_, value) ->
                    value
                        .replaceDictionaryMedia(preparedNote.dictionaryMedia)
                        .normalizeAnkiDictionaryHtml()
                }
        return buildJsonObject {
            put("deckName", deckName)
            put("modelName", modelName)
            put(
                "fields",
                JsonObject(fields.mapValues { JsonPrimitive(it.value) }),
            )
            put("options", duplicateOptions(settings, deckName))
            put("tags", JsonArray(settings.tags.map(::JsonPrimitive)))
            mediaNoteMembers(preparedNote).forEach { (key, value) -> put(key, value) }
        }
    }

    private fun duplicateOptions(
        settings: AnkiSettings,
        deckName: String,
    ): JsonObject =
        buildJsonObject {
            put("allowDuplicate", settings.allowDuplicates)
            put("duplicateScope", settings.duplicateScope.wireName)
            put(
                "duplicateScopeOptions",
                buildJsonObject {
                    put("deckName", deckName)
                    put("checkChildren", true)
                    put("checkAllModels", settings.checkAllModels)
                },
            )
        }

    private fun mediaNoteMembers(preparedNote: PreparedAnkiNote): Map<String, JsonElement> {
        val audio =
            buildList {
                preparedNote.wordAudio
                    ?.takeIf { preparedNote.audioFields.isNotEmpty() }
                    ?.takeIf { it.dataBase64.isNotBlank() }
                    ?.let { media ->
                        add(
                            buildJsonObject {
                                put("filename", media.fileName)
                                put("data", media.dataBase64)
                                put("fields", JsonArray(preparedNote.audioFields.map(::JsonPrimitive)))
                            },
                        )
                    }
                preparedNote.sasayakiAudio
                    ?.takeIf { preparedNote.sasayakiAudioFields.isNotEmpty() }
                    ?.takeIf { it.dataBase64.isNotBlank() }
                    ?.let { media ->
                        add(
                            buildJsonObject {
                                put("filename", media.fileName)
                                put("data", media.dataBase64)
                                put("fields", JsonArray(preparedNote.sasayakiAudioFields.map(::JsonPrimitive)))
                            },
                        )
                    }
            }
        val picture =
            preparedNote.bookCover
                ?.takeIf { preparedNote.pictureFields.isNotEmpty() }
                ?.takeIf { it.dataBase64.isNotBlank() }
                ?.let { media ->
                    listOf(
                        buildJsonObject {
                            put("filename", media.fileName)
                            put("data", media.dataBase64)
                            put("fields", JsonArray(preparedNote.pictureFields.map(::JsonPrimitive)))
                        },
                    )
                }.orEmpty()
        return buildMap {
            if (audio.isNotEmpty()) put("audio", JsonArray(audio))
            if (picture.isNotEmpty()) put("picture", JsonArray(picture))
        }
    }

    private fun String.replaceDictionaryMedia(dictionaryMedia: List<PreparedAnkiMedia>): String =
        dictionaryMedia.fold(this) { value, media ->
            val temporaryName = media.temporaryName
            val finalName = media.fileName
            if (temporaryName.isBlank() || finalName.isBlank()) {
                value
            } else {
                value.replace(temporaryName, finalName)
            }
        }

    private fun envelope(
        action: String,
        params: JsonElement = JsonNull,
    ): String =
        json.encodeToString(
            JsonObject(
                buildMap {
                    put("action", JsonPrimitive(action))
                    put("version", JsonPrimitive(6))
                    if (params !is JsonNull) put("params", params)
                },
            ),
        )

    private fun String?.requireConfigured(label: String): String =
        this?.takeIf(String::isNotBlank) ?: throw IllegalStateException("$label is not selected")

    private fun firstMappedField(settings: AnkiSettings): String =
        settings.fieldMappings.keys.firstOrNull()
            ?: throw IllegalStateException("Anki field mapping is not configured")

    private val PreparedAnkiNote.mediaFieldNames: Set<String>
        get() = (audioFields + sasayakiAudioFields + pictureFields).toSet()

    private val AnkiDuplicateScope.wireName: String
        get() =
            when (this) {
                AnkiDuplicateScope.Collection -> "collection"
                AnkiDuplicateScope.Deck -> "deck"
                AnkiDuplicateScope.NoteType -> "notetype"
            }
}
