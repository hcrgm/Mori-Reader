package app.mori.reader.data.audiobook

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

internal class AndroidAudiobookPlayerRepository(
    private val context: Context,
) : AudiobookPlayerRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val booksRoot = File(context.filesDir, BOOKS_DIR_NAME)
    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
        }
    private val player = ExoPlayer.Builder(context).build()
    private var preparedBookId: String? = null
    private var preparedMatches: List<SasayakiMatch> = emptyList()
    private var tickJob: Job? = null
    private var lastPersistAt = 0L

    private val _snapshot = MutableStateFlow(SasayakiPlayerSnapshot())
    override val snapshot: StateFlow<SasayakiPlayerSnapshot> = _snapshot

    init {
        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    publishSnapshot()
                    if (isPlaying) startTicker() else persistCurrent()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    publishSnapshot()
                    if (playbackState == Player.STATE_READY) persistCurrent()
                }
            },
        )
    }

    override suspend fun prepare(
        bookId: String,
        audioAssetInfo: AudiobookAssetInfo,
        matches: List<SasayakiMatch>,
    ) = withContext(Dispatchers.Main.immediate) {
        val audioUri = resolveAudioUri(bookId, audioAssetInfo)
        val playback = loadPlayback(bookId)
        preparedBookId = bookId
        preparedMatches = matches.sortedBy { it.startTimeMs }
        player.setMediaItem(MediaItem.fromUri(audioUri))
        player.playbackParameters = PlaybackParameters(playback.rate.coerceIn(MIN_RATE, MAX_RATE))
        player.prepare()
        if (playback.positionMs > 0L) {
            player.seekTo(playback.positionMs)
        }
        _snapshot.update {
            it.copy(
                bookId = bookId,
                isReady = true,
                isPlaying = false,
                positionMs = playback.positionMs,
                delayMs = playback.delayMs.coerceIn(MIN_DELAY_MS, MAX_DELAY_MS),
                rate = playback.rate.coerceIn(MIN_RATE, MAX_RATE),
                currentCueId = cueAt(playback.positionMs, playback.delayMs)?.id,
                errorMessage = null,
            )
        }
        publishSnapshot()
    }

    override suspend fun togglePlayPause() =
        withContext(Dispatchers.Main.immediate) {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
                startTicker()
            }
            publishSnapshot()
        }

    override suspend fun play() =
        withContext(Dispatchers.Main.immediate) {
            player.play()
            startTicker()
            publishSnapshot()
        }

    override suspend fun pause() =
        withContext(Dispatchers.Main.immediate) {
            player.pause()
            publishSnapshot()
            persistCurrent()
        }

    override suspend fun seekTo(positionMs: Long) =
        withContext(Dispatchers.Main.immediate) {
            val duration = player.duration.takeIf { it != C.TIME_UNSET } ?: Long.MAX_VALUE
            player.seekTo(positionMs.coerceIn(0L, duration))
            publishSnapshot()
            persistCurrent()
        }

    override suspend fun seekToCue(cueId: String) =
        withContext(Dispatchers.Main.immediate) {
            val cue = preparedMatches.firstOrNull { it.id == cueId } ?: return@withContext
            player.seekTo(cue.startTimeMs)
            publishSnapshot()
            persistCurrent()
        }

    override suspend fun nextCue() =
        withContext(Dispatchers.Main.immediate) {
            val current = player.currentPosition + _snapshot.value.delayMs
            val cue =
                preparedMatches.firstOrNull { it.startTimeMs > current + 250L }
                    ?: preparedMatches.lastOrNull()
                    ?: return@withContext
            player.seekTo(cue.startTimeMs)
            publishSnapshot()
            persistCurrent()
        }

    override suspend fun previousCue() =
        withContext(Dispatchers.Main.immediate) {
            val current = player.currentPosition + _snapshot.value.delayMs
            val cue =
                preparedMatches.lastOrNull { it.startTimeMs < current - 750L }
                    ?: preparedMatches.firstOrNull()
                    ?: return@withContext
            player.seekTo(cue.startTimeMs)
            publishSnapshot()
            persistCurrent()
        }

    override suspend fun setDelay(delayMs: Long) =
        withContext(Dispatchers.Main.immediate) {
            _snapshot.update {
                it.copy(
                    delayMs = delayMs.coerceIn(MIN_DELAY_MS, MAX_DELAY_MS),
                    currentCueId = cueAt(player.currentPosition, delayMs)?.id,
                )
            }
            persistCurrent()
        }

    override suspend fun setRate(rate: Float) =
        withContext(Dispatchers.Main.immediate) {
            val clamped = rate.coerceIn(MIN_RATE, MAX_RATE)
            player.playbackParameters = PlaybackParameters(clamped)
            _snapshot.update { it.copy(rate = clamped) }
            persistCurrent()
        }

    override suspend fun release() =
        withContext(Dispatchers.Main.immediate) {
            close()
        }

    fun close() {
        tickJob?.cancel()
        persistCurrent()
        player.release()
        scope.coroutineContext[Job]?.cancel()
    }

    private fun startTicker() {
        if (tickJob?.isActive == true) return
        tickJob =
            scope.launch {
                while (isActive) {
                    publishSnapshot()
                    val now = System.currentTimeMillis()
                    if (now - lastPersistAt >= 1_000L) {
                        persistCurrent()
                        lastPersistAt = now
                    }
                    delay(250L)
                }
            }
    }

    private fun publishSnapshot() {
        val duration = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L
        val position = player.currentPosition.coerceAtLeast(0L)
        val delay = _snapshot.value.delayMs
        _snapshot.update {
            it.copy(
                bookId = preparedBookId,
                isReady = preparedBookId != null,
                isPlaying = player.isPlaying,
                positionMs = position,
                durationMs = duration.coerceAtLeast(0L),
                currentCueId = cueAt(position, delay)?.id,
                errorMessage = null,
            )
        }
    }

    private fun cueAt(
        positionMs: Long,
        delayMs: Long,
    ): SasayakiMatch? {
        val adjusted = positionMs + delayMs
        return preparedMatches.firstOrNull { cue ->
            adjusted in cue.startTimeMs until cue.endTimeMs
        }
    }

    private fun persistCurrent() {
        val bookId = preparedBookId ?: return
        val bookDir = findBookDirectory(bookId) ?: return
        val data =
            SasayakiPlaybackData(
                positionMs = player.currentPosition.coerceAtLeast(0L),
                delayMs = _snapshot.value.delayMs,
                rate = _snapshot.value.rate,
                updatedAt = System.currentTimeMillis(),
            )
        runCatching {
            File(bookDir, PLAYBACK_FILE).writeText(json.encodeToString(SasayakiPlaybackData.serializer(), data))
        }
    }

    private fun loadPlayback(bookId: String): SasayakiPlaybackData {
        val bookDir = findBookDirectory(bookId) ?: return SasayakiPlaybackData()
        return runCatching {
            json.decodeFromString(SasayakiPlaybackData.serializer(), File(bookDir, PLAYBACK_FILE).readText())
        }.getOrDefault(SasayakiPlaybackData())
    }

    private fun resolveAudioUri(
        bookId: String,
        asset: AudiobookAssetInfo,
    ): Uri {
        asset.sourceUriString?.let { return Uri.parse(it) }
        val bookDir = findBookDirectory(bookId) ?: throw IllegalArgumentException("图书不存在")
        val relative = asset.localRelativePath ?: throw IllegalArgumentException("有声书音频不存在")
        val file = File(bookDir, relative)
        require(file.exists()) { "有声书音频不存在" }
        return Uri.fromFile(file)
    }

    private fun findBookDirectory(bookId: String): File? =
        booksRoot
            .listFiles()
            ?.filter { it.isDirectory }
            ?.firstOrNull { dir -> loadBookMetadata(dir)?.id == bookId }

    private fun loadBookMetadata(bookDir: File): PlayerBookMetadataStorage? =
        runCatching {
            json.decodeFromString(PlayerBookMetadataStorage.serializer(), File(bookDir, METADATA_FILE).readText())
        }.getOrNull()
}

@Serializable
private data class PlayerBookMetadataStorage(
    val id: String,
)

private const val BOOKS_DIR_NAME = "Books"
private const val METADATA_FILE = "metadata.json"
private const val PLAYBACK_FILE = "sasayaki_playback.json"
private const val MIN_DELAY_MS = -2_000L
private const val MAX_DELAY_MS = 2_000L
private const val MIN_RATE = 0.5f
private const val MAX_RATE = 1.5f
