package app.mori.reader.data.anki

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AnkiConnectPayloadBuilderTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun addNotePayloadIncludesVersionFieldsTagsAndDuplicateOptions() {
        val request =
            AnkiConnectPayloadBuilder.addNote(
                settings =
                    AnkiSettings(
                        selectedDeck = "Mining",
                        selectedNoteType = "Mori",
                        fieldMappings =
                            mapOf(
                                "Expression" to "{expression}",
                                "Sentence" to "{sentence}",
                            ),
                        tags = listOf("mori", "jp"),
                        allowDuplicates = false,
                        duplicateScope = AnkiDuplicateScope.Deck,
                        checkAllModels = true,
                    ),
                content = AnkiMiningContent(expression = "猫"),
                context = AnkiMiningContext(sentence = "黒い猫がいる"),
            )
        val root = json.parseToJsonElement(request).jsonObject

        assertEquals("addNote", root.string("action"))
        assertEquals(6, root.int("version"))
        val note = root.obj("params").obj("note")
        assertEquals("Mining", note.string("deckName"))
        assertEquals("Mori", note.string("modelName"))
        assertEquals("猫", note.obj("fields").string("Expression"))
        assertEquals("黒い<b>猫</b>がいる", note.obj("fields").string("Sentence"))
        assertEquals(listOf("mori", "jp"), note.array("tags").map { it.jsonPrimitive.content })
        val options = note.obj("options")
        assertFalse(options.boolean("allowDuplicate"))
        assertEquals("deck", options.string("duplicateScope"))
        assertEquals("Mining", options.obj("duplicateScopeOptions").string("deckName"))
        assertEquals(true, options.obj("duplicateScopeOptions").boolean("checkAllModels"))
    }

    @Test
    fun mediaPayloadUsesBase64StoreMediaFileEnvelope() {
        val request =
            AnkiConnectPayloadBuilder.storeMediaFile(
                DictionaryMedia(
                    fileName = "neko.mp3",
                    mimeType = "audio/mpeg",
                    dataBase64 = "YWJj",
                ),
            )
        val root = json.parseToJsonElement(request).jsonObject

        assertEquals("storeMediaFile", root.string("action"))
        assertEquals(6, root.int("version"))
        assertEquals("neko.mp3", root.obj("params").string("filename"))
        assertEquals("YWJj", root.obj("params").string("data"))
    }

    @Test
    fun addNotePayloadRoutesExactMediaHandlebarsToAnkiConnectMediaMembers() {
        val request =
            AnkiConnectPayloadBuilder.addNote(
                settings =
                    AnkiSettings(
                        selectedDeck = "Mining",
                        selectedNoteType = "Mori",
                        fieldMappings =
                            mapOf(
                                "Expression" to "{expression}",
                                "Audio" to "{audio}",
                                "Cover" to "{book-cover}",
                            ),
                    ),
                content =
                    AnkiMiningContent(
                        expression = "猫",
                        audio = listOf(DictionaryMedia(fileName = "neko.mp3", mimeType = "audio/mpeg", dataBase64 = "YWJj")),
                    ),
                context = AnkiMiningContext(coverUri = "content://cover.jpg"),
            )
        val note =
            json
                .parseToJsonElement(request)
                .jsonObject
                .obj("params")
                .obj("note")

        assertEquals("猫", note.obj("fields").string("Expression"))
        assertNull(note.obj("fields")["Audio"])
        assertNull(note.obj("fields")["Cover"])
        assertEquals(
            "neko.mp3",
            note
                .array("audio")
                .first()
                .jsonObject
                .string("filename"),
        )
        assertEquals(
            listOf("Audio"),
            note
                .array("audio")
                .first()
                .jsonObject
                .array("fields")
                .map { it.jsonPrimitive.content },
        )
        assertEquals(
            "cover.jpg",
            note
                .array("picture")
                .first()
                .jsonObject
                .string("filename"),
        )
        assertEquals(
            listOf("Cover"),
            note
                .array("picture")
                .first()
                .jsonObject
                .array("fields")
                .map { it.jsonPrimitive.content },
        )
    }

    @Test
    fun duplicateCheckPayloadMatchesAnkiConnectCanAddNotesShape() {
        val request =
            AnkiConnectPayloadBuilder.canAddNotesWithErrorDetail(
                settings =
                    AnkiSettings(
                        selectedDeck = "Mining",
                        selectedNoteType = "Mori",
                        fieldMappings = mapOf("Expression" to "{expression}"),
                        duplicateScope = AnkiDuplicateScope.Collection,
                        allowDuplicates = false,
                    ),
                expression = "猫",
            )
        val root = json.parseToJsonElement(request).jsonObject
        val note =
            root
                .obj("params")
                .array("notes")
                .first()
                .jsonObject

        assertEquals("canAddNotesWithErrorDetail", root.string("action"))
        assertEquals("猫", note.obj("fields").string("Expression"))
        assertEquals("collection", note.obj("options").string("duplicateScope"))
    }

    private fun JsonObject.obj(name: String): JsonObject = getValue(name).jsonObject

    private fun JsonObject.array(name: String): JsonArray = getValue(name) as JsonArray

    private fun JsonObject.string(name: String): String = getValue(name).jsonPrimitive.content

    private fun JsonObject.int(name: String): Int = getValue(name).jsonPrimitive.int

    private fun JsonObject.boolean(name: String): Boolean = getValue(name).jsonPrimitive.boolean
}
