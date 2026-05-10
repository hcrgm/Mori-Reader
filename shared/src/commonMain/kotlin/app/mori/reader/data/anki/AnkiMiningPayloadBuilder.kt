package app.mori.reader.data.anki

import app.mori.reader.data.audiobook.AudiobookAssetInfo
import app.mori.reader.data.audiobook.SasayakiMatch
import app.mori.reader.data.book.ReaderBook
import app.mori.reader.data.dictionary.DictionaryFrequencyGroup
import app.mori.reader.data.dictionary.DictionaryGlossary
import app.mori.reader.data.dictionary.DictionaryLookupEntry
import app.mori.reader.data.filteredReaderText
import kotlin.math.max

fun buildAnkiMiningContent(
    entry: DictionaryLookupEntry,
    popupSelectionText: String = "",
    selectedGlossary: String? = null,
    audio: String = "",
): AnkiMiningContent =
    AnkiMiningContent(
        expression = entry.expression,
        reading = entry.reading,
        matched = entry.matched,
        furiganaPlain = constructFuriganaPlain(entry.expression, entry.reading),
        audio = audio,
        glossaries = entry.glossaries.map { AnkiGlossary(it.dictionary, it.toHtml()) },
        selectedGlossary = selectedGlossary,
        popupSelectionText = popupSelectionText,
        frequencies = entry.frequencies.toFrequencyHtml(),
        frequencyHarmonicRank = entry.frequencies.frequencyHarmonicRank(),
        pitchAccentPositions = entry.pitches.flatMap { group -> group.pitchPositions.map(Int::toString) },
        pitchAccentCategories = entry.pitchCategories(),
    )

fun buildReaderAnkiMiningContext(
    book: ReaderBook?,
    sentence: String,
    sasayakiAudioAssetInfo: AudiobookAssetInfo? = null,
    sasayakiMatches: List<SasayakiMatch> = emptyList(),
    sasayakiDelayMs: Long = 0L,
    sasayakiCueId: String? = null,
): AnkiMiningContext {
    val cue = sasayakiCueId?.let { cueId -> sasayakiMatches.firstOrNull { it.id == cueId } }
    val range =
        cue?.let { resolveSasayakiAudioRange(sasayakiMatches, it, sentence, sasayakiDelayMs) }
    return AnkiMiningContext(
        sentence = sentence,
        documentTitle = book?.info?.title.orEmpty(),
        coverUri = book?.info?.coverPath,
        sasayakiAudioFileName = sasayakiCueId?.let { "mori_sasayaki_$it.m4a" },
        sasayakiAudioAssetInfo = sasayakiAudioAssetInfo,
        sasayakiAudioDelayMs = sasayakiDelayMs,
        sasayakiAudioStartMs = range?.first,
        sasayakiAudioEndMs = range?.second,
    )
}

private fun resolveSasayakiAudioRange(
    matches: List<SasayakiMatch>,
    cue: SasayakiMatch,
    sentence: String,
    delayMs: Long,
): Pair<Long, Long> {
    val expanded = expandSasayakiCue(matches = matches, cue = cue, sentence = sentence)
    val start = max(0L, expanded.first + delayMs)
    val end = max(start, expanded.second + delayMs)
    return start to end
}

private fun expandSasayakiCue(
    matches: List<SasayakiMatch>,
    cue: SasayakiMatch,
    sentence: String,
): Pair<Long, Long> {
    val chapterCues = matches.filter { it.chapterIndex == cue.chapterIndex }
    val index = chapterCues.indexOfFirst { it.id == cue.id }
    if (index < 0) return cue.startTimeMs to cue.endTimeMs

    var start = index
    var end = index
    val filteredSentence = sentence.filteredReaderText()
    while (start > chapterCues.indices.first && filteredSentence.contains(chapterCues[start - 1].text.filteredReaderText())) {
        start -= 1
    }
    while (end < chapterCues.indices.last && filteredSentence.contains(chapterCues[end + 1].text.filteredReaderText())) {
        end += 1
    }
    return chapterCues[start].startTimeMs to chapterCues[end].endTimeMs
}

private fun DictionaryGlossary.toHtml(): String {
    val contentHtml = content.structuredText().htmlEscape()
    val tags =
        definitionTags
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it.toIntOrNull() == null && it !in partOfSpeechTags }
    val tagsHtml =
        if (tags.isEmpty()) {
            ""
        } else {
            tags.joinToString(
                prefix = """<div class="tag-row">""",
                postfix = "</div>",
                separator = "",
            ) { tag -> """<span class="expr-tag">${tag.htmlEscape()}</span>""" }
        }
    return """<div class="glossary-content">$tagsHtml$contentHtml</div>"""
}

private fun String.structuredText(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return ""
    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
        return trimmed
            .removePrefix("[")
            .removeSuffix("]")
            .splitTopLevelJsonStrings()
            .joinToString("; ")
    }
    if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
        return trimmed.removeSurrounding("\"").unescapeJsonString()
    }
    return trimmed
}

private fun String.splitTopLevelJsonStrings(): List<String> {
    val values = mutableListOf<String>()
    val current = StringBuilder()
    var inString = false
    var escaping = false
    for (char in this) {
        when {
            escaping -> {
                current.append(char)
                escaping = false
            }

            char == '\\' && inString -> {
                escaping = true
            }

            char == '"' -> {
                if (inString) {
                    values += current.toString().unescapeJsonString()
                    current.clear()
                }
                inString = !inString
            }

            inString -> {
                current.append(char)
            }
        }
    }
    return values
}

private fun String.unescapeJsonString(): String =
    replace("\\\"", "\"")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\\\", "\\")

private fun constructFuriganaPlain(
    expression: String,
    reading: String,
): String {
    if (reading.isBlank() || reading == expression) return expression
    val groups = segmentFurigana(expression, reading)
    return groups
        .joinToString("") { (text, furigana) ->
            if (furigana.isBlank()) "$text " else "$text[$furigana]"
        }.trim()
}

private fun segmentFurigana(
    expression: String,
    reading: String,
): List<Pair<String, String>> {
    if (expression.isBlank() || reading.isBlank() || expression == reading) return listOf(expression to "")
    if (expression.any(::isKanji).not()) return listOf(expression to "")
    return listOf(expression to reading)
}

private fun isKanji(char: Char): Boolean =
    char in '\u4E00'..'\u9FFF' ||
        char in '\u3400'..'\u4DBF' ||
        char in '\uF900'..'\uFAFF' ||
        char == '\u3005'

private fun List<DictionaryFrequencyGroup>.toFrequencyHtml(): List<String> =
    flatMap { group ->
        group.frequencies.map { frequency ->
            val value = frequency.displayValue.ifBlank { frequency.value.toString() }
            """<span class="frequency-group" title="${group.dictionary.htmlEscape()}"><span class="frequency-dict-label">${group.dictionary.htmlEscape()}</span><span class="frequency-values">${value.htmlEscape()}</span></span>"""
        }
    }

private fun List<DictionaryFrequencyGroup>.frequencyHarmonicRank(): String {
    val values =
        asSequence()
            .mapNotNull { group ->
                group.frequencies.firstOrNull()?.let { frequency ->
                    frequency.displayValue.takeWhile(Char::isDigit).toIntOrNull()
                        ?: frequency.value.takeIf { it > 0 }
                }
            }.filter { it > 0 }
            .toList()
    if (values.isEmpty()) return "9999999"
    val reciprocalSum = values.sumOf { 1.0 / it.toDouble() }
    return kotlin.math
        .floor(values.size / reciprocalSum)
        .toInt()
        .toString()
}

private fun DictionaryLookupEntry.pitchCategories(): List<String> {
    val verbOrAdjective =
        glossaries
            .flatMap { it.termTags.split(Regex("\\s+")) }
            .any { it in partOfSpeechTags }
    return pitches
        .asSequence()
        .flatMap { it.pitchPositions.asSequence() }
        .mapNotNull { pitch ->
            pitchCategory(
                reading.ifBlank { expression },
                pitch,
                verbOrAdjective,
            )
        }.distinct()
        .toList()
}

private val partOfSpeechTags =
    setOf("adj-i", "adj-na", "adj-no", "v1", "vk", "vs", "vs-i", "vs-s", "vz", "vi", "vt")

private fun String.htmlEscape(): String =
    buildString(length) {
        this@htmlEscape.forEach { char ->
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(char)
            }
        }
    }

private fun pitchCategory(
    reading: String,
    pitchAccentValue: Int,
    verbOrAdjective: Boolean,
): String? {
    if (pitchAccentValue == 0) return "heiban"
    if (verbOrAdjective) return "kifuku"
    if (pitchAccentValue == 1) return "atamadaka"
    if (pitchAccentValue > 1) {
        val moraCount = reading.length
        return if (pitchAccentValue >= moraCount) "odaka" else "nakadaka"
    }
    return null
}
