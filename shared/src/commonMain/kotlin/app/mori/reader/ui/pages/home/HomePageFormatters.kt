package app.mori.reader.ui.pages.home
import androidx.compose.runtime.Composable
import app.mori.reader.data.audiobook.AudiobookAssetInfo
import app.mori.reader.data.audiobook.AudiobookAssetType
import app.mori.reader.data.audiobook.AudiobookStorageMode
import app.mori.reader.data.book.BookCategory
import app.mori.reader.data.book.BookInfo
import app.mori.reader.data.settings.BookshelfSortMode
import app.mori.reader.features.audiobook.presentation.AudiobookState
import app.mori.reader.features.bookshelf.presentation.HomeState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.audiobook_asset_copied
import app.mori.reader.shared.generated.resources.audiobook_asset_referenced
import app.mori.reader.shared.generated.resources.audiobook_subtitle_saved
import org.jetbrains.compose.resources.stringResource

@Composable
fun AudiobookAssetInfo.audioDetails(): String =
    listOf(
        format.uppercase(),
        formatBytes(fileSizeBytes),
        if (storageMode == AudiobookStorageMode.Reference) {
            stringResource(Res.string.audiobook_asset_referenced)
        } else {
            stringResource(Res.string.audiobook_asset_copied)
        },
    ).joinToString(" · ")

@Composable
fun AudiobookAssetInfo.subtitleDetails(cueCount: Int?): String =
    listOfNotNull(
        format.uppercase(),
        formatBytes(fileSizeBytes),
        cueCount?.let { "$it cues" },
        stringResource(Res.string.audiobook_subtitle_saved),
    ).joinToString(" · ")

fun formatMatchRate(
    matched: Int,
    total: Int,
): String {
    if (total <= 0) return "0/0 (0.0%)"
    val percent = matched * 100.0 / total
    return "$matched/$total (${percent.formatOneDecimal()}%)"
}

fun Double.formatOneDecimal(): String {
    val rounded = kotlin.math.round(this * 10.0) / 10.0
    return rounded.toString()
}

fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = listOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex += 1
    }
    return if (unitIndex == 0) {
        "${value.toLong()} ${units[unitIndex]}"
    } else {
        "%.1f %s".format(value, units[unitIndex])
    }
}

fun List<BookInfo>.sortedFor(mode: BookshelfSortMode): List<BookInfo> =
    when (mode) {
        BookshelfSortMode.Recent -> {
            sortedWith(
                compareByDescending<BookInfo> { it.lastOpenedAt ?: it.importedAt }
                    .thenBy { it.title.lowercase() },
            )
        }

        BookshelfSortMode.Title -> {
            sortedBy { it.title.lowercase() }
        }
    }
