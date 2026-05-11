package app.mori.reader.data.audiobook

import java.util.UUID

// TODO move parser stuff to common

private val SRT_ARROW_REGEX = Regex("""-->|->|–>|—>""")

private val SRT_TIME_TOKEN_REGEX =
    Regex("""(?:\d{1,5}:)?\d{1,2}:\d{1,2}(?:[,.]\d{1,6})?""")

private val BR_TAG_REGEX = Regex("""(?i)<br\s*/?>""")

fun parseSrt(rawText: String): List<AudiobookCue> {
    val normalized =
        rawText
            .replace("\uFEFF", "") // BOM
            .replace("\r\n", "\n")
            .replace('\r', '\n')

    require(normalized.isNotBlank()) { "字幕文件为空" }

    val lines = normalized.lines()
    val cues = ArrayList<AudiobookCue>(maxOf(16, lines.size / 4))

    var pendingSequence: Int? = null
    var i = 0

    while (i < lines.size) {
        val line = lines[i].trim()
        val timing = parseSrtTimingLine(line)

        if (timing == null) {
            when {
                line.isBlank() -> {
                    // ignore
                }

                looksLikeSrtTimingLine(line) -> {
                    // 像时间轴但无效，例如结束时间 <= 开始时间，跳过这条
                    pendingSequence = null
                }

                line.isSrtSequenceLine() -> {
                    pendingSequence = line.toIntOrNull()
                }

                else -> {
                    // 杂散文本，避免错误复用前面的序号
                    pendingSequence = null
                }
            }

            i++
            continue
        }

        val sequence = pendingSequence ?: (cues.size + 1)
        pendingSequence = null

        val start = timing.first
        val end = timing.second

        i++

        val textLines = ArrayList<String>(4)

        while (i < lines.size) {
            val current = lines[i]
            val currentTrimmed = current.trim()

            // 下一条 cue 没有序号，直接遇到时间轴
            if (looksLikeSrtTimingLine(currentTrimmed)) {
                break
            }

            // 下一条 cue 有序号：数字行 + 后面最近的非空行是时间轴
            if (
                currentTrimmed.isSrtSequenceLine() &&
                nextNonBlankLooksLikeTiming(lines, i + 1)
            ) {
                break
            }

            textLines += current
            i++
        }

        val text = normalizeSrtCueText(textLines)

        // 宽松模式：空文本 cue 直接跳过，而不是中断整个文件
        if (text.isBlank()) {
            continue
        }

        cues +=
            AudiobookCue(
                id =
                    UUID
                        .nameUUIDFromBytes(
                            "$sequence:$start:$end:$text".toByteArray(Charsets.UTF_8),
                        ).toString(),
                startTimeMs = start,
                endTimeMs = end,
                text = text,
                sequence = sequence,
            )
    }

    val result =
        cues
            .distinctBy { "${it.startTimeMs}\u0000${it.endTimeMs}\u0000${it.text}" }
            .sortedWith(
                compareBy<AudiobookCue> { it.startTimeMs }
                    .thenBy { it.endTimeMs }
                    .thenBy { it.sequence },
            )

    require(result.isNotEmpty()) { "SRT 没有可用字幕" }

    return result
}

private fun parseSrtTimingLine(line: String): Pair<Long, Long>? {
    val arrow = SRT_ARROW_REGEX.find(line) ?: return null

    val left = line.substring(0, arrow.range.first)
    val right = line.substring(arrow.range.last + 1)

    val startToken = SRT_TIME_TOKEN_REGEX.find(left)?.value ?: return null
    val endToken = SRT_TIME_TOKEN_REGEX.find(right)?.value ?: return null

    val start = parseSrtTimeToken(startToken) ?: return null
    val end = parseSrtTimeToken(endToken) ?: return null

    return if (end > start) {
        start to end
    } else {
        null
    }
}

private fun looksLikeSrtTimingLine(line: String): Boolean {
    val arrow = SRT_ARROW_REGEX.find(line) ?: return false

    val left = line.substring(0, arrow.range.first)
    val right = line.substring(arrow.range.last + 1)

    return SRT_TIME_TOKEN_REGEX.containsMatchIn(left) &&
        SRT_TIME_TOKEN_REGEX.containsMatchIn(right)
}

private fun parseSrtTimeToken(token: String): Long? {
    val parts = token.trim().split(',', '.', limit = 2)

    val hms = parts[0].split(':')
    if (hms.size !in 2..3) return null

    val hours: Long
    val minutes: Long
    val seconds: Long

    if (hms.size == 3) {
        hours = hms[0].toLongOrNull() ?: return null
        minutes = hms[1].toLongOrNull() ?: return null
        seconds = hms[2].toLongOrNull() ?: return null
    } else {
        hours = 0L
        minutes = hms[0].toLongOrNull() ?: return null
        seconds = hms[1].toLongOrNull() ?: return null
    }

    if (minutes !in 0..59) return null
    if (seconds !in 0..59) return null

    val millis =
        parts
            .getOrNull(1)
            ?.take(3)
            ?.padEnd(3, '0')
            ?.toLongOrNull()
            ?: 0L

    return ((hours * 60 + minutes) * 60 + seconds) * 1000 + millis
}

private fun normalizeSrtCueText(lines: List<String>): String =
    lines
        .dropWhile { it.isBlank() }
        .dropLastWhile { it.isBlank() }
        .joinToString("\n") { it.trimEnd() }
        .replace(BR_TAG_REGEX, "\n")
        .trim()

private fun nextNonBlankLooksLikeTiming(
    lines: List<String>,
    fromIndex: Int,
): Boolean {
    var index = fromIndex

    while (index < lines.size && lines[index].isBlank()) {
        index++
    }

    return index < lines.size && looksLikeSrtTimingLine(lines[index].trim())
}

private fun String.isSrtSequenceLine(): Boolean = length in 1..9 && all { it in '0'..'9' }
