package app.mori.reader.data.audiobook

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
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
    private var preparedBookId: String? = null
    private var preparedAudioAssetInfo: AudiobookAssetInfo? = null
    private var preparedMediaInfo: SasayakiMediaInfo = SasayakiMediaInfo()
    private var preparedMatches: List<SasayakiMatch> = emptyList()
    private var publishedCueId: String? = null
    private var tickJob: Job? = null
    private var lastPersistAt = 0L
    private val player = ExoPlayer.Builder(context).build()
    private val sessionPlayer =
        SasayakiSessionPlayer(
            player = player,
            hasCues = { preparedMatches.isNotEmpty() },
            onNextCue = ::seekToNextCueFromSession,
            onPreviousCue = ::seekToPreviousCueFromSession,
        )
    private val mediaSession = createMediaSession(context, sessionPlayer)

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
        mediaInfo: SasayakiMediaInfo,
    ) = withContext(Dispatchers.Main.immediate) {
        val audioUri = resolveAudioUri(bookId, audioAssetInfo)
        val playback = loadPlayback(bookId)
        preparedBookId = bookId
        preparedAudioAssetInfo = audioAssetInfo
        preparedMediaInfo = mediaInfo
        preparedMatches = matches.sortedBy { it.startTimeMs }
        publishedCueId = cueAtOrBefore(playback.positionMs, playback.delayMs)?.id
        player.setMediaItem(
            MediaItem
                .Builder()
                .setUri(audioUri)
                .setMediaMetadata(buildMediaMetadata(currentCue()))
                .build(),
        )
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
                currentCueId = publishedCueId,
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
                startMediaSessionService()
                player.play()
                startTicker()
            }
            publishSnapshot()
        }

    override suspend fun play() =
        withContext(Dispatchers.Main.immediate) {
            startMediaSessionService()
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
            seekToNextCue()
        }

    override suspend fun previousCue() =
        withContext(Dispatchers.Main.immediate) {
            seekToPreviousCue()
        }

    override suspend fun setDelay(delayMs: Long) =
        withContext(Dispatchers.Main.immediate) {
            _snapshot.update {
                it.copy(
                    delayMs = delayMs.coerceIn(MIN_DELAY_MS, MAX_DELAY_MS),
                    currentCueId = cueAtOrBefore(player.currentPosition, delayMs)?.id,
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

    override suspend fun stop(bookId: String?) =
        withContext(Dispatchers.Main.immediate) {
            if (bookId != null && preparedBookId != bookId) return@withContext
            stopInternal()
        }

    override suspend fun release() =
        withContext(Dispatchers.Main.immediate) {
            close()
        }

    fun close() {
        stopInternal()
        mediaSession.release()
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
        val cue = cueAtOrBefore(position, delay)
        updateMediaMetadataIfNeeded(cue)
        _snapshot.update {
            it.copy(
                bookId = preparedBookId,
                isReady = preparedBookId != null,
                isPlaying = player.isPlaying,
                positionMs = position,
                durationMs = duration.coerceAtLeast(0L),
                currentCueId = cue?.id,
                errorMessage = null,
            )
        }
    }

    private fun cueAtOrBefore(
        positionMs: Long,
        delayMs: Long,
    ): SasayakiMatch? {
        val adjusted = positionMs + delayMs
        return preparedMatches.lastOrNull { cue -> adjusted >= cue.startTimeMs }
    }

    private fun seekToNextCue() {
        val current = player.currentPosition + _snapshot.value.delayMs
        val cue =
            preparedMatches.firstOrNull { it.startTimeMs > current + CUE_NAVIGATION_TOLERANCE_MS }
                ?: preparedMatches.lastOrNull()
                ?: return
        player.seekTo(cue.startTimeMs)
        publishSnapshot()
        persistCurrent()
    }

    private fun seekToPreviousCue() {
        val current = player.currentPosition + _snapshot.value.delayMs
        val currentIndex = preparedMatches.indexOfLast { it.startTimeMs <= current + CUE_NAVIGATION_TOLERANCE_MS }
        val targetIndex = (currentIndex - 1).coerceAtLeast(0)
        val cue = preparedMatches.getOrNull(targetIndex) ?: return
        player.seekTo(cue.startTimeMs)
        publishSnapshot()
        persistCurrent()
    }

    private fun seekToNextCueFromSession() {
        runOnPlayerLooper { seekToNextCue() }
    }

    private fun seekToPreviousCueFromSession() {
        runOnPlayerLooper { seekToPreviousCue() }
    }

    private fun runOnPlayerLooper(action: () -> Unit) {
        if (Looper.myLooper() == player.applicationLooper) {
            action()
        } else {
            scope.launch { action() }
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
            File(
                bookDir,
                PLAYBACK_FILE,
            ).writeText(json.encodeToString(SasayakiPlaybackData.serializer(), data))
        }
    }

    private fun loadPlayback(bookId: String): SasayakiPlaybackData {
        val bookDir = findBookDirectory(bookId) ?: return SasayakiPlaybackData()
        return runCatching {
            json.decodeFromString(
                SasayakiPlaybackData.serializer(),
                File(bookDir, PLAYBACK_FILE).readText(),
            )
        }.getOrDefault(SasayakiPlaybackData())
    }

    private fun stopInternal() {
        tickJob?.cancel()
        tickJob = null
        persistCurrent()
        player.pause()
        player.stop()
        player.clearMediaItems()
        preparedBookId = null
        preparedAudioAssetInfo = null
        preparedMediaInfo = SasayakiMediaInfo()
        preparedMatches = emptyList()
        publishedCueId = null
        _snapshot.value =
            SasayakiPlayerSnapshot(
                delayMs = _snapshot.value.delayMs,
                rate = _snapshot.value.rate,
            )
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
            json.decodeFromString(
                PlayerBookMetadataStorage.serializer(),
                File(bookDir, METADATA_FILE).readText(),
            )
        }.getOrNull()

    private fun currentCue(): SasayakiMatch? = publishedCueId?.let { cueId -> preparedMatches.firstOrNull { it.id == cueId } }

    private fun updateMediaMetadataIfNeeded(cue: SasayakiMatch?) {
        if (publishedCueId == cue?.id) return
        publishedCueId = cue?.id
        if (player.mediaItemCount == 0) return
        val current = player.currentMediaItem ?: return
        player.replaceMediaItem(
            player.currentMediaItemIndex.coerceAtLeast(0),
            current
                .buildUpon()
                .setMediaMetadata(buildMediaMetadata(cue))
                .build(),
        )
    }

    private fun buildMediaMetadata(cue: SasayakiMatch?): MediaMetadata {
        val audio = preparedAudioAssetInfo
        val sentence = cue?.text?.trim()?.takeIf { it.isNotBlank() }
        val bookTitle = preparedMediaInfo.title?.trim()?.takeIf { it.isNotBlank() }
        val audioTitle = audio?.displayName?.trim()?.takeIf { it.isNotBlank() }
        val coverUri =
            preparedMediaInfo.coverPath
                ?.takeIf { it.isNotBlank() }
                ?.let(::File)
                ?.takeIf { it.exists() }
                ?.let(Uri::fromFile)
        return MediaMetadata
            .Builder()
            .setTitle(bookTitle ?: audioTitle)
            .setDisplayTitle(bookTitle ?: audioTitle)
            .setArtist(sentence)
            .setSubtitle(sentence)
            .setDescription(sentence)
            .setAlbumTitle(audioTitle ?: "Sasayaki")
            .setArtworkUri(coverUri)
            .build()
    }

    private fun startMediaSessionService() {
        val intent = Intent(context, AndroidAudiobookMediaSessionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    internal fun mediaSession(): MediaSession = mediaSession
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
private const val CUE_NAVIGATION_TOLERANCE_MS = 250L

@OptIn(UnstableApi::class)
private class SasayakiSessionPlayer(
    player: Player,
    private val hasCues: () -> Boolean,
    private val onNextCue: () -> Unit,
    private val onPreviousCue: () -> Unit,
) : ForwardingPlayer(player) {
    override fun isCommandAvailable(command: Int): Boolean =
        if (command.isCueNavigationCommand()) {
            hasCues()
        } else {
            super.isCommandAvailable(command)
        }

    override fun getAvailableCommands(): Player.Commands {
        val builder = Player.Commands.Builder().addAll(super.getAvailableCommands())
        if (hasCues()) {
            builder.addAll(
                Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_PREVIOUS,
                Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                Player.COMMAND_SEEK_TO_NEXT,
            )
        }
        return builder.build()
    }

    override fun hasPreviousMediaItem(): Boolean = hasCues()

    override fun hasNextMediaItem(): Boolean = hasCues()

    override fun seekToPreviousMediaItem() {
        onPreviousCue()
    }

    override fun seekToPrevious() {
        onPreviousCue()
    }

    override fun seekToNextMediaItem() {
        onNextCue()
    }

    override fun seekToNext() {
        onNextCue()
    }

    private fun Int.isCueNavigationCommand(): Boolean =
        this == Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM ||
            this == Player.COMMAND_SEEK_TO_PREVIOUS ||
            this == Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM ||
            this == Player.COMMAND_SEEK_TO_NEXT
}

private fun createMediaSession(
    context: Context,
    player: Player,
): MediaSession {
    val builder = MediaSession.Builder(context, player)
    context.createSessionActivity()?.let(builder::setSessionActivity)
    return builder.build()
}

private fun Context.createSessionActivity(): PendingIntent? {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
    return PendingIntent.getActivity(
        this,
        0,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
