package app.mori.reader.ui.pages.reader

internal fun Double.formatPercent(): String {
    val rounded = (this * 10.0).toInt() / 10.0
    return rounded.toString()
}

internal fun formatPlaybackTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

internal fun parseHexColor(value: String): Long =
    value.removePrefix("#").toLongOrNull(16)?.let {
        if (value.length == 9) it else 0xFFC0485C
    } ?: 0xFFC0485C
