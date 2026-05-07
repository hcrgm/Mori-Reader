package app.mori.reader.ui.pages.dictionary

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import app.mori.reader.data.audio.AndroidLocalAudioStore
import app.mori.reader.data.settings.AudioPlaybackMode
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import java.io.File

private const val AudioFetchConnectTimeoutMillis = 15_000
private const val AudioFetchReadTimeoutMillis = 45_000
private const val DictionaryAudioLogTag = "MoriDictionaryAudio"
private const val EmptyAudioSourceListJson = """{"type":"audioSourceList","audioSources":[]}"""

private val dictionaryAudioHttpClient = HttpClient(CIO) {
    install(HttpTimeout) {
        connectTimeoutMillis = AudioFetchConnectTimeoutMillis.toLong()
        requestTimeoutMillis = AudioFetchReadTimeoutMillis.toLong()
        socketTimeoutMillis = AudioFetchReadTimeoutMillis.toLong()
    }
    followRedirects = true
    expectSuccess = false
    defaultRequest {
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
    }
}

internal object DictionaryAutoplayTracker {
    private val consumedKeys = linkedSetOf<String>()

    fun consume(key: String): Boolean =
        synchronized(consumedKeys) {
            if (key.isBlank() || key in consumedKeys) {
                false
            } else {
                consumedKeys += key
                if (consumedKeys.size > 128) {
                    consumedKeys.remove(consumedKeys.first())
                }
                true
            }
        }
}

internal class DictionaryAudioSourceResolver {
    fun resolveAudioSourceListJson(
        context: Context?,
        sourceUrl: String,
    ): String =
        if (context != null && sourceUrl.startsWith("local://")) {
            resolveLocalAudioSourceListJson(context, sourceUrl)
        } else {
            resolveRemoteAudioSourceListJson(sourceUrl)
        }

    fun playableAudioUrl(context: Context, url: String): String? =
        if (url.startsWith("local://audio-file")) {
            val bytes = AndroidLocalAudioStore.audioBytes(context, url) ?: return null
            val target = File(context.cacheDir, "word-audio-${System.nanoTime()}.mp3")
            target.writeBytes(bytes)
            target.toURI().toString()
        } else {
            url
        }

    private fun resolveLocalAudioSourceListJson(context: Context, sourceUrl: String): String =
        runCatching {
            AndroidLocalAudioStore.audioSourceListJson(context, sourceUrl)
        }.onFailure {
            Log.e(
                DictionaryAudioLogTag,
                "resolveAudioSourceListJson local failed url=${sourceUrl.take(200)}",
                it,
            )
        }.getOrDefault(EmptyAudioSourceListJson)

    private fun resolveRemoteAudioSourceListJson(sourceUrl: String): String =
        runCatching {
            runBlocking {
                dictionaryAudioHttpClient.get(sourceUrl) {
                    accept(ContentType.Application.Json)
                }.bodyAsText()
            }
        }
            .onFailure {
                Log.e(
                    DictionaryAudioLogTag,
                    "resolveAudioSourceListJson remote failed url=${sourceUrl.take(200)}",
                    it,
                )
            }
            .getOrDefault(EmptyAudioSourceListJson)
}

internal class AndroidWordAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    fun play(context: Context, url: String, mode: AudioPlaybackMode) {
        stop(context)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (mode != AudioPlaybackMode.Mix && !requestFocus(audioManager, mode)) return

        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            setDataSource(context, Uri.parse(url))
            setOnCompletionListener { stop(context) }
            setOnErrorListener { _, _, _ ->
                stop(context)
                true
            }
            prepareAsync()
            setOnPreparedListener { it.start() }
        }
        mediaPlayer = player
    }

    private fun requestFocus(audioManager: AudioManager, mode: AudioPlaybackMode): Boolean {
        val gain = when (mode) {
            AudioPlaybackMode.Interrupt -> AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            AudioPlaybackMode.Duck -> AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            AudioPlaybackMode.Mix -> return true
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(gain)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                gain
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun stop(context: Context) {
        mediaPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) player.stop()
                player.release()
            }
        }
        mediaPlayer = null
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
    }
}
