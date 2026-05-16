package app.mori.reader.features.lookup.presentation

import app.mori.reader.data.dictionary.DictionaryLookupEntry
import app.mori.reader.ui.text.UiText

internal fun createLookupStackEntry(
    stack: List<ReaderLookupState>,
    parentIndex: Int?,
    lookupId: Int,
    text: String,
    sentence: String,
    rect: ReaderSelectionRect?,
    sasayakiCueId: String? = null,
): List<ReaderLookupState> {
    val baseStack =
        if (parentIndex == null) {
            emptyList()
        } else {
            stack.take(parentIndex + 1)
        }
    return baseStack +
        ReaderLookupState(
            id = lookupId,
            selectedText = text,
            sentence = sentence,
            rect = rect,
            isSearching = true,
            sasayakiCueId = sasayakiCueId,
        )
}

internal fun List<ReaderLookupState>.withLookupResult(
    lookupId: Int,
    entries: List<DictionaryLookupEntry>,
    dictionaryStyles: Map<String, String>,
): List<ReaderLookupState> =
    map { lookup ->
        if (lookup.id != lookupId) return@map lookup
        lookup.copy(
            isSearching = false,
            entries = entries,
            dictionaryStyles = dictionaryStyles,
            highlightLength =
                entries
                    .firstOrNull()
                    ?.matched
                    ?.codePointLength(),
            errorMessage = null,
        )
    }

internal fun List<ReaderLookupState>.withLookupError(
    lookupId: Int,
    errorMessage: UiText,
): List<ReaderLookupState> =
    map { lookup ->
        if (lookup.id != lookupId) return@map lookup
        lookup.copy(
            isSearching = false,
            highlightLength = null,
            errorMessage = errorMessage,
        )
    }

internal fun dismissLookupStack(
    stack: List<ReaderLookupState>,
    index: Int?,
): List<ReaderLookupState> =
    when (index) {
        null -> emptyList()
        else -> stack.take(index)
    }

private fun String.codePointLength(): Int {
    var count = 0
    var index = 0
    while (index < length) {
        val char = this[index]
        index +=
            if (char.isHighSurrogate() && index + 1 < length && this[index + 1].isLowSurrogate()) {
                2
            } else {
                1
            }
        count++
    }
    return count
}
