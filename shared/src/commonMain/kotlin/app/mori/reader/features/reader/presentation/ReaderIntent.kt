package app.mori.reader.features.reader.presentation

import app.mori.reader.features.lookup.presentation.ReaderSelectionRect

sealed interface ReaderIntent {
    data class LoadBook(
        val bookId: String,
    ) : ReaderIntent

    data class OpenChapter(
        val index: Int,
        val fragment: String? = null,
    ) : ReaderIntent

    data object OpenNextChapter : ReaderIntent

    data object OpenPreviousChapter : ReaderIntent

    data class JumpToCharacter(
        val characterCount: Int,
    ) : ReaderIntent

    data class TextSelected(
        val text: String,
        val sentence: String,
        val rect: ReaderSelectionRect? = null,
    ) : ReaderIntent

    data class PopupTextSelected(
        val parentIndex: Int?,
        val text: String,
        val rect: ReaderSelectionRect? = null,
    ) : ReaderIntent

    data class JumpToLink(
        val href: String,
    ) : ReaderIntent

    data class DismissLookup(
        val index: Int? = null,
    ) : ReaderIntent

    data class UpdateProgress(
        val progress: Double,
    ) : ReaderIntent

    data class SaveProgress(
        val progress: Double,
    ) : ReaderIntent

    data class ToggleCurrentBookmark(
        val snippet: String = "",
    ) : ReaderIntent

    data class DeleteBookmark(
        val bookmarkId: String,
    ) : ReaderIntent

    data class SetBookReaderScheme(
        val schemeId: String?,
    ) : ReaderIntent

    data object CloseBook : ReaderIntent

    data object TogglePlayback : ReaderIntent

    data object PausePlayback : ReaderIntent

    data object NextCue : ReaderIntent

    data object PreviousCue : ReaderIntent

    data class SeekTo(
        val positionMs: Long,
    ) : ReaderIntent

    data class SetDelay(
        val delayMs: Long,
    ) : ReaderIntent

    data class SetRate(
        val rate: Float,
    ) : ReaderIntent

    data class ReplayCue(
        val cueId: String,
    ) : ReaderIntent

    data class ContinueFromCue(
        val cueId: String,
    ) : ReaderIntent
}
