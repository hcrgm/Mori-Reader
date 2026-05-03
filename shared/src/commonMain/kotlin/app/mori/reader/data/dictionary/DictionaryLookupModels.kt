package app.mori.reader.data.dictionary

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryLookupResult(
    val entries: List<DictionaryLookupEntry> = emptyList(),
    val styles: Map<String, String> = emptyMap(),
)

@Serializable
data class DictionaryLookupEntry(
    val expression: String,
    val reading: String,
    val matched: String,
    val deinflectionTrace: List<DictionaryTraceStep> = emptyList(),
    val glossaries: List<DictionaryGlossary> = emptyList(),
    val frequencies: List<DictionaryFrequencyGroup> = emptyList(),
    val pitches: List<DictionaryPitchGroup> = emptyList(),
    val rules: List<String> = emptyList(),
)

@Serializable
data class DictionaryTraceStep(
    val name: String,
    val description: String = "",
)

@Serializable
data class DictionaryGlossary(
    val dictionary: String,
    val content: String,
    val definitionTags: String,
    val termTags: String,
)

@Serializable
data class DictionaryFrequencyGroup(
    val dictionary: String,
    val frequencies: List<DictionaryFrequency> = emptyList(),
)

@Serializable
data class DictionaryFrequency(
    val value: Int,
    val displayValue: String,
)

@Serializable
data class DictionaryPitchGroup(
    val dictionary: String,
    val pitchPositions: List<Int> = emptyList(),
)
