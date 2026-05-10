package app.mori.reader.data.audiobook

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
data class SasayakiPlaybackData(
    val positionMs: Long = 0L,
    val delayMs: Long = 0L,
    val rate: Float = 1.0f,
    val updatedAt: Long = 0L,
)

data class SasayakiPlayerSnapshot(
    val bookId: String? = null,
    val isReady: Boolean = false,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val delayMs: Long = 0L,
    val rate: Float = 1.0f,
    val currentCueId: String? = null,
    val errorMessage: String? = null,
)

data class SasayakiMediaInfo(
    val title: String? = null,
    val coverPath: String? = null,
)

interface AudiobookPlayerRepository {
    val snapshot: StateFlow<SasayakiPlayerSnapshot>

    suspend fun prepare(
        bookId: String,
        audioAssetInfo: AudiobookAssetInfo,
        matches: List<SasayakiMatch>,
        mediaInfo: SasayakiMediaInfo = SasayakiMediaInfo(),
    )

    suspend fun togglePlayPause()

    suspend fun play()

    suspend fun pause()

    suspend fun seekTo(positionMs: Long)

    suspend fun seekToCue(cueId: String)

    suspend fun nextCue()

    suspend fun previousCue()

    suspend fun setDelay(delayMs: Long)

    suspend fun setRate(rate: Float)

    suspend fun stop(bookId: String? = null)

    suspend fun release()
}
