package app.mori.reader.data.anki

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AnkiSettingsSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    @Test
    fun defaultsMatchPhaseOneContract() {
        val settings = AnkiSettings()

        assertEquals(AnkiConnectionMode.AnkiDroid, settings.connectionMode)
        assertEquals("http://127.0.0.1:8765", settings.ankiConnect.url)
        assertEquals(10_000, settings.ankiConnect.timeoutMillis)
        assertEquals(AnkiDuplicateScope.Collection, settings.duplicateScope)
        assertFalse(settings.forceSync)
    }

    @Test
    fun decodeIgnoresUnknownFields() {
        val decoded =
            json.decodeFromString<AnkiSettings>(
                """
                {
                  "connectionMode": "AnkiConnect",
                  "ankiConnect": {
                    "url": "http://localhost:8765",
                    "timeoutMillis": 5000,
                    "future": true
                  },
                  "selectedDeck": "Mining",
                  "futureField": "ignored"
                }
                """.trimIndent(),
            )

        assertEquals(AnkiConnectionMode.AnkiConnect, decoded.connectionMode)
        assertEquals("http://localhost:8765", decoded.ankiConnect.url)
        assertEquals(5_000, decoded.ankiConnect.timeoutMillis)
        assertEquals("Mining", decoded.selectedDeck)
    }

    @Test
    fun roundTripKeepsMappingsAndTags() {
        val settings =
            AnkiSettings(
                selectedDeck = "Mining",
                selectedNoteType = "Mori",
                fieldMappings = mapOf("Expression" to "{expression}", "Sentence" to "{sentence}"),
                tags = listOf("mori", "jp"),
                allowDuplicates = true,
            )

        val decoded = json.decodeFromString<AnkiSettings>(json.encodeToString(settings))

        assertEquals(settings, decoded)
    }
}
