package app.mori.reader.data.anki

import app.mori.reader.data.audiobook.AudiobookAssetInfo
import kotlinx.serialization.Serializable

@Serializable
data class AnkiSettings(
    val connectionMode: AnkiConnectionMode = AnkiConnectionMode.AnkiDroid,
    val ankiConnect: AnkiConnectConfig = AnkiConnectConfig(),
    val duplicateScope: AnkiDuplicateScope = AnkiDuplicateScope.Collection,
    val checkAllModels: Boolean = false,
    val forceSync: Boolean = false,
    val selectedDeck: String? = null,
    val selectedNoteType: String? = null,
    val cachedDecks: List<AnkiDeck> = emptyList(),
    val cachedNoteTypes: List<AnkiNoteType> = emptyList(),
    val showLapisTemplateHint: Boolean = true,
    val fieldMappings: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
    val allowDuplicates: Boolean = false,
    val compactGlossaries: Boolean = true,
    val embedMedia: Boolean = true,
)

@Serializable
enum class AnkiConnectionMode {
    AnkiDroid,
    AnkiConnect,
}

@Serializable
data class AnkiConnectConfig(
    val url: String = "http://127.0.0.1:8765",
    val timeoutMillis: Int = 10_000,
)

@Serializable
enum class AnkiDuplicateScope {
    Collection,
    Deck,
    NoteType,
}

@Serializable
data class AnkiDeck(
    val id: String,
    val name: String,
)

@Serializable
data class AnkiNoteType(
    val id: String,
    val name: String,
    val fields: List<AnkiField> = emptyList(),
)

@Serializable
data class AnkiField(
    val name: String,
)

object AnkiHandlebarTokens {
    const val EXPRESSION = "{expression}"
    const val READING = "{reading}"
    const val FURIGANA_PLAIN = "{furigana-plain}"
    const val AUDIO = "{audio}"
    const val GLOSSARY = "{glossary}"
    const val GLOSSARY_FIRST = "{glossary-first}"
    const val SELECTED_GLOSSARY = "{selected-glossary}"
    const val POPUP_SELECTION_TEXT = "{popup-selection-text}"
    const val SENTENCE = "{sentence}"
    const val FREQUENCIES = "{frequencies}"
    const val FREQUENCY_HARMONIC_RANK = "{frequency-harmonic-rank}"
    const val PITCH_ACCENT_POSITIONS = "{pitch-accent-positions}"
    const val PITCH_ACCENT_CATEGORIES = "{pitch-accent-categories}"
    const val DOCUMENT_TITLE = "{document-title}"
    const val BOOK_COVER = "{book-cover}"
    const val SASAYAKI_AUDIO = "{sasayaki-audio}"
}

const val ANKI_LAPIS_WORD_AND_SENTENCE_CARD_MARKER = "x"

fun defaultAnkiHandlebarTokens(): List<String> =
    listOf(
        AnkiHandlebarTokens.EXPRESSION,
        AnkiHandlebarTokens.READING,
        AnkiHandlebarTokens.FURIGANA_PLAIN,
        AnkiHandlebarTokens.AUDIO,
        AnkiHandlebarTokens.GLOSSARY,
        AnkiHandlebarTokens.GLOSSARY_FIRST,
        AnkiHandlebarTokens.SELECTED_GLOSSARY,
        AnkiHandlebarTokens.POPUP_SELECTION_TEXT,
        AnkiHandlebarTokens.SENTENCE,
        AnkiHandlebarTokens.FREQUENCIES,
        AnkiHandlebarTokens.FREQUENCY_HARMONIC_RANK,
        AnkiHandlebarTokens.PITCH_ACCENT_POSITIONS,
        AnkiHandlebarTokens.PITCH_ACCENT_CATEGORIES,
        AnkiHandlebarTokens.DOCUMENT_TITLE,
        AnkiHandlebarTokens.BOOK_COVER,
        AnkiHandlebarTokens.SASAYAKI_AUDIO,
    )

fun lapisFieldMappings(): Map<String, String> =
    linkedMapOf(
        "Expression" to AnkiHandlebarTokens.EXPRESSION,
        "ExpressionFurigana" to AnkiHandlebarTokens.FURIGANA_PLAIN,
        "ExpressionReading" to AnkiHandlebarTokens.READING,
        "ExpressionAudio" to AnkiHandlebarTokens.AUDIO,
        "SelectionText" to AnkiHandlebarTokens.POPUP_SELECTION_TEXT,
        "MainDefinition" to AnkiHandlebarTokens.GLOSSARY_FIRST,
        "Sentence" to AnkiHandlebarTokens.SENTENCE,
        "SentenceAudio" to AnkiHandlebarTokens.SASAYAKI_AUDIO,
        "Picture" to AnkiHandlebarTokens.BOOK_COVER,
        "Glossary" to AnkiHandlebarTokens.GLOSSARY,
        "PitchPosition" to AnkiHandlebarTokens.PITCH_ACCENT_POSITIONS,
        "PitchCategories" to AnkiHandlebarTokens.PITCH_ACCENT_CATEGORIES,
        "Frequency" to AnkiHandlebarTokens.FREQUENCIES,
        "FreqSort" to AnkiHandlebarTokens.FREQUENCY_HARMONIC_RANK,
        "MiscInfo" to AnkiHandlebarTokens.DOCUMENT_TITLE,
        "IsWordAndSentenceCard" to ANKI_LAPIS_WORD_AND_SENTENCE_CARD_MARKER,
    )

fun AnkiNoteType.isLapis(): Boolean = name.contains("lapis", ignoreCase = true)

@Serializable
data class AnkiFetchResult(
    val decks: List<AnkiDeck> = emptyList(),
    val noteTypes: List<AnkiNoteType> = emptyList(),
)

@Serializable
data class AnkiHandlebar(
    val token: String,
    val label: String = token,
)

@Serializable
data class AnkiMiningContent(
    val expression: String = "",
    val reading: String = "",
    val matched: String = "",
    val furiganaPlain: String = "",
    val audio: String = "",
    val frequenciesHtml: String = "",
    val freqHarmonicRank: String = "",
    val glossary: String = "",
    val glossaryFirst: String = "",
    val singleGlossaries: Map<String, String> = emptyMap(),
    val pitchPositions: String = "",
    val pitchCategories: String = "",
    val dictionaryMedia: List<DictionaryMedia> = emptyList(),
    val glossaries: List<AnkiGlossary> = emptyList(),
    val selectedGlossary: String? = null,
    val selectedDictionary: String = "",
    val popupSelectionText: String = "",
    val frequencies: List<String> = emptyList(),
    val frequencyHarmonicRank: String = "",
    val pitchAccentPositions: List<String> = emptyList(),
    val pitchAccentCategories: List<String> = emptyList(),
)

@Serializable
data class AnkiGlossary(
    val dictionaryTitle: String,
    val text: String,
)

@Serializable
data class AnkiMiningContext(
    val sentence: String = "",
    val documentTitle: String = "",
    val coverUri: String? = null,
    val sasayakiAudioFileName: String? = null,
    val sasayakiAudioAssetInfo: AudiobookAssetInfo? = null,
    val sasayakiAudioDelayMs: Long = 0L,
    val sasayakiAudioStartMs: Long? = null,
    val sasayakiAudioEndMs: Long? = null,
)

@Serializable
data class DictionaryMedia(
    val fileName: String = "",
    val mimeType: String = "",
    val dataBase64: String? = null,
    val dictionary: String = "",
    val path: String = "",
    val filename: String = "",
)

data class PreparedAnkiNote(
    val fields: Map<String, String> = emptyMap(),
    val audioFields: List<String> = emptyList(),
    val sasayakiAudioFields: List<String> = emptyList(),
    val pictureFields: List<String> = emptyList(),
    val dictionaryMedia: List<PreparedAnkiMedia> = emptyList(),
    val wordAudio: PreparedAnkiMedia? = null,
    val sasayakiAudio: PreparedAnkiMedia? = null,
    val bookCover: PreparedAnkiMedia? = null,
)

data class PreparedAnkiMedia(
    val temporaryName: String = "",
    val fileName: String,
    val mimeType: String,
    val dataBase64: String,
)

data class AnkiRenderedNote(
    val fields: Map<String, String>,
)
