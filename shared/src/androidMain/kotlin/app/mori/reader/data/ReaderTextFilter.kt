package app.mori.reader.data

internal fun String.filteredReaderText(): String {
    var text = this
    Regex("<body\\b[^>]*>.*?</body>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        .find(text)
        ?.let { text = it.value }
    text =
        text
            .replace(Regex("<rt\\b[^>]*>.*?</rt>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)), "")
            .replace(Regex("<rp\\b[^>]*>.*?</rp>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)), "")
            .replace(Regex("<(script|style)\\b[^>]*>.*?</\\1>", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)), "")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    return buildString {
        text.codePoints().forEach { codePoint ->
            if (codePoint.isReaderMatchableCodePoint()) {
                appendCodePoint(codePoint)
            }
        }
    }
}

internal fun String.filteredReaderCharacterCount(): Int {
    val filtered = filteredReaderText()
    return filtered.codePointCount(0, filtered.length)
}

internal fun Int.isReaderMatchableCodePoint(): Boolean =
    when (this) {
        in '0'.code..'9'.code,
        in 'A'.code..'Z'.code,
        in 'a'.code..'z'.code,
        '○'.code,
        '◯'.code,
        in '々'.code..'〇'.code,
        '〻'.code,
        in 'ぁ'.code..'ゖ'.code,
        in 'ゝ'.code..'ゞ'.code,
        in 'ァ'.code..'ヺ'.code,
        'ー'.code,
        in '０'.code..'９'.code,
        in 'Ａ'.code..'Ｚ'.code,
        in 'ａ'.code..'ｚ'.code,
        in 'ｦ'.code..'ﾝ'.code,
        in 0x2E80..0x2FDF,
        in 0x3400..0x4DBF,
        in 0x4E00..0x9FFF,
        in 0xF900..0xFAFF,
        in 0x20000..0x2A6DF,
        in 0x2A700..0x2B73F,
        in 0x2B740..0x2B81F,
        in 0x2B820..0x2CEAF,
        in 0x2CEB0..0x2EBEF,
        in 0x30000..0x3134F,
        in 0x31350..0x323AF,
        -> true

        else -> false
    }
