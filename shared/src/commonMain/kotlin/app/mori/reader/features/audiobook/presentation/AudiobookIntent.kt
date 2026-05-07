package app.mori.reader.features.audiobook.presentation

import app.mori.reader.data.audiobook.AudiobookAssetType
import app.mori.reader.data.audiobook.AudiobookStorageMode

sealed interface AudiobookIntent {
    data class OpenAudiobookManager(
        val bookId: String,
    ) : AudiobookIntent

    data object CloseAudiobookManager : AudiobookIntent

    data class LoadAudiobookAssets(
        val bookId: String,
    ) : AudiobookIntent

    data class ImportAudiobookAudio(
        val bookId: String,
        val uriString: String,
    ) : AudiobookIntent

    data class ImportAudiobookSubtitle(
        val bookId: String,
        val uriString: String,
    ) : AudiobookIntent

    data class DeleteAudiobookAsset(
        val bookId: String,
        val type: AudiobookAssetType,
    ) : AudiobookIntent

    data class RunAudiobookMatch(
        val bookId: String,
        val searchWindow: Int,
    ) : AudiobookIntent

    data class DeleteAudiobookMatch(
        val bookId: String,
    ) : AudiobookIntent

    data class SetAudiobookSearchWindow(
        val value: Int,
    ) : AudiobookIntent

    data class SetAudiobookStorageMode(
        val mode: AudiobookStorageMode,
    ) : AudiobookIntent

    data object DismissAudiobookError : AudiobookIntent
}
