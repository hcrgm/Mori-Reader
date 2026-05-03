package app.mori.reader.data.anki

import kotlinx.serialization.Serializable

const val DefaultAnkiEndpoint = "http://127.0.0.1:8765"

@Serializable
data class AnkiSettings(
    val enabled: Boolean = false,
    val endpoint: String = DefaultAnkiEndpoint,
    val selectedDeck: String = "",
    val selectedModel: String = "",
    val fieldMappingsByModel: Map<String, List<AnkiFieldMapping>> = emptyMap(),
    val allowDuplicates: Boolean = false,
    val duplicateScope: DuplicateScope = DuplicateScope.Collection,
    val checkAllModels: Boolean = false,
    val forceSync: Boolean = false,
    val tags: String = "mori",
    val compactGlossaries: Boolean = true,
) {
    val selectedFieldMappings: List<AnkiFieldMapping>
        get() = fieldMappingsByModel[selectedModel].orEmpty()
}

@Serializable
data class AnkiNoteType(
    val name: String,
    val fields: List<String> = emptyList(),
)

@Serializable
data class AnkiFieldMapping(
    val fieldName: String,
    val template: String,
)

@Serializable
data class AnkiCardPayload(
    val expression: String,
    val reading: String = "",
    val furiganaPlain: String = "",
    val glossary: String = "",
    val glossaryFirst: String = "",
    val selectedGlossary: String = "",
    val popupSelectionText: String = "",
    val sentence: String = "",
    val frequencies: String = "",
    val frequencyHarmonicRank: String = "",
    val pitchAccentPositions: String = "",
    val pitchAccentCategories: String = "",
    val documentTitle: String = "",
    val audio: List<AnkiAudioFile> = emptyList(),
)

@Serializable
data class AnkiAudioFile(
    val filename: String,
    val dataBase64: String,
    val fields: List<String> = emptyList(),
)

@Serializable
data class AnkiMiningContext(
    val popupSelectionText: String = "",
    val sentence: String = "",
    val documentTitle: String = "",
)

enum class DuplicateScope(val label: String) {
    Collection("整个集合"),
    Deck("当前牌组"),
    DeckRoot("牌组根目录"),
}

enum class AnkiTemplateToken(val token: String, val label: String) {
    Expression("{expression}", "表达"),
    Reading("{reading}", "读音"),
    FuriganaPlain("{furigana-plain}", "纯文本注音"),
    Audio("{audio}", "音频"),
    Glossary("{glossary}", "释义"),
    GlossaryFirst("{glossary-first}", "首条释义"),
    SelectedGlossary("{selected-glossary}", "选中释义"),
    PopupSelectionText("{popup-selection-text}", "弹窗选词"),
    Sentence("{sentence}", "例句"),
    Frequencies("{frequencies}", "词频"),
    FrequencyHarmonicRank("{frequency-harmonic-rank}", "谐波词频"),
    PitchAccentPositions("{pitch-accent-positions}", "音高位置"),
    PitchAccentCategories("{pitch-accent-categories}", "音高分类"),
    DocumentTitle("{document-title}", "文档标题"),
}

data class AnkiCatalog(
    val decks: List<String> = emptyList(),
    val noteTypes: List<AnkiNoteType> = emptyList(),
)

data class AnkiDuplicateOptions(
    val allowDuplicate: Boolean,
    val duplicateScope: DuplicateScope,
    val deckName: String,
    val checkAllModels: Boolean,
) {
    val effectiveDeckName: String
        get() = if (duplicateScope == DuplicateScope.DeckRoot) deckName.substringBefore("::") else deckName
}

fun splitAnkiTags(tags: String): List<String> =
    tags.split(Regex("\\s+")).map { it.trim() }.filter { it.isNotEmpty() }.distinct()

fun renderAnkiTemplate(
    template: String,
    payload: AnkiCardPayload,
    audioFieldName: String,
): String {
    val audioText = payload.audio
        .filter { it.fields.isEmpty() || audioFieldName in it.fields }
        .joinToString(" ") { "[sound:${it.filename}]" }
    return template
        .replace(AnkiTemplateToken.Expression.token, payload.expression)
        .replace(AnkiTemplateToken.Reading.token, payload.reading)
        .replace(AnkiTemplateToken.FuriganaPlain.token, payload.furiganaPlain)
        .replace(AnkiTemplateToken.Audio.token, audioText)
        .replace(AnkiTemplateToken.Glossary.token, payload.glossary)
        .replace(AnkiTemplateToken.GlossaryFirst.token, payload.glossaryFirst)
        .replace(AnkiTemplateToken.SelectedGlossary.token, payload.selectedGlossary)
        .replace(AnkiTemplateToken.PopupSelectionText.token, payload.popupSelectionText)
        .replace(AnkiTemplateToken.Sentence.token, payload.sentence)
        .replace(AnkiTemplateToken.Frequencies.token, payload.frequencies)
        .replace(AnkiTemplateToken.FrequencyHarmonicRank.token, payload.frequencyHarmonicRank)
        .replace(AnkiTemplateToken.PitchAccentPositions.token, payload.pitchAccentPositions)
        .replace(AnkiTemplateToken.PitchAccentCategories.token, payload.pitchAccentCategories)
        .replace(AnkiTemplateToken.DocumentTitle.token, payload.documentTitle)
}

fun defaultAnkiFieldMappings(fields: List<String>): List<AnkiFieldMapping> =
    fields.map { field ->
        val normalized = field.lowercase()
        val template = when {
            normalized.contains("expression") || normalized.contains("word") || field == "表現" -> "{expression}"
            normalized.contains("reading") || normalized.contains("kana") || field == "読み" -> "{reading}"
            normalized.contains("sentence") || field == "例文" -> "{sentence}"
            normalized.contains("audio") || field == "音声" -> "{audio}"
            normalized.contains("gloss") || normalized.contains("meaning") || normalized.contains("definition") ||
                field == "意味" -> "{glossary}"
            normalized.contains("freq") || field == "頻度" -> "{frequencies}"
            normalized.contains("pitch") || field == "アクセント" -> "{pitch-accent-positions}"
            else -> ""
        }
        AnkiFieldMapping(fieldName = field, template = template)
    }
