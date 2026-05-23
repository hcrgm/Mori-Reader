package app.mori.reader.features.reader.presentation

import app.mori.reader.data.audiobook.AudiobookAssetInfo
import app.mori.reader.data.audiobook.SasayakiCueRange
import app.mori.reader.data.audiobook.SasayakiMatch
import app.mori.reader.data.audiobook.SasayakiPlayerSnapshot
import app.mori.reader.data.book.ReaderBook
import app.mori.reader.data.book.ReaderSavedBookmark
import app.mori.reader.features.lookup.presentation.ReaderLookupState
import app.mori.reader.ui.text.UiText

data class ReaderState(
    val bookId: String? = null,
    val book: ReaderBook? = null,
    val sasayakiAudioAssetInfo: AudiobookAssetInfo? = null,
    val sasayakiMatches: List<SasayakiMatch> = emptyList(),
    val sasayakiPlayer: SasayakiPlayerSnapshot = SasayakiPlayerSnapshot(),
    val resumeSasayakiAfterLookup: Boolean = false,
    val chapterIndex: Int = 0,
    val chapterProgress: Double = 0.0,
    val fragment: String? = null,
    val navigationVersion: Int = 0,
    val verticalWriting: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val lookupStack: List<ReaderLookupState> = emptyList(),
) {
    val currentChapter
        get() = book?.chapters?.getOrNull(chapterIndex)

    val currentChapterSasayakiCues: List<SasayakiCueRange>
        get() =
            sasayakiMatches
                .asSequence()
                .filter { it.chapterIndex == chapterIndex }
                .sortedBy { it.start }
                .map { SasayakiCueRange(id = it.id, start = it.start, length = it.length) }
                .toList()

    val currentCharacter: Int
        get() {
            val chapter = currentChapter ?: return 0
            return chapter.characterStart +
                (
                    chapter.characterCount *
                        chapterProgress.coerceIn(
                            0.0,
                            1.0,
                        )
                ).toInt()
        }

    val progressPercent: Double
        get() {
            val total = book?.totalCharacterCount ?: return 0.0
            if (total <= 0) return 0.0
            return currentCharacter.toDouble() / total.toDouble() * 100.0
        }

    val savedBookmarks: List<ReaderSavedBookmark>
        get() = book?.savedBookmarks.orEmpty()

    val currentSavedBookmark: ReaderSavedBookmark?
        get() =
            savedBookmarks.firstOrNull { bookmark ->
                bookmark.chapterIndex == chapterIndex && bookmark.characterCount == currentCharacter
            }

    val isCurrentPositionBookmarked: Boolean
        get() = currentSavedBookmark != null
}
