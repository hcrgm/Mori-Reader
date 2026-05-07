package app.mori.reader.features.audiobook.presentation

import app.mori.reader.data.audiobook.AudiobookAssetInfo
import app.mori.reader.data.audiobook.AudiobookStorageMode
import app.mori.reader.data.audiobook.AudiobookSubtitleData
import app.mori.reader.data.audiobook.SasayakiMatchData
import app.mori.reader.ui.text.UiText

data class AudiobookState(
    val selectedBookId: String? = null,
    val audioAssetInfo: AudiobookAssetInfo? = null,
    val subtitleAssetInfo: AudiobookAssetInfo? = null,
    val subtitleData: AudiobookSubtitleData? = null,
    val matchData: SasayakiMatchData? = null,
    val isImportingAudio: Boolean = false,
    val isImportingSubtitle: Boolean = false,
    val isMatching: Boolean = false,
    val searchWindow: Int = 200,
    val preferredStorageMode: AudiobookStorageMode = AudiobookStorageMode.Copy,
    val errorMessage: UiText? = null,
) {
    val isOpen: Boolean
        get() = selectedBookId != null
}
