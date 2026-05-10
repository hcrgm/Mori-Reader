package app.mori.reader.data.anki

import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import app.mori.reader.data.audiobook.AudiobookAssetInfo
import app.mori.reader.data.audiobook.AudiobookStorageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

internal object AndroidSasayakiAudioExtractor {
    suspend fun extractCueAudio(
        context: Context,
        assetInfo: AudiobookAssetInfo,
        startMs: Long,
        endMs: Long,
    ): ByteArray? {
        if (endMs <= startMs) return null
        val sourceUri = resolveAudioUri(context, assetInfo) ?: return null
        val outputFile =
            withContext(Dispatchers.IO) {
                File.createTempFile("mori_sasayaki_", ".m4a", context.cacheDir)
            }
        return try {
            exportRange(context, sourceUri, outputFile, startMs, endMs)
                ?.takeIf(File::isFile)
                ?.takeIf { it.length() > 0L }
                ?.readBytes()
        } finally {
            outputFile.delete()
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun exportRange(
        context: Context,
        sourceUri: Uri,
        outputFile: File,
        startMs: Long,
        endMs: Long,
    ): File? =
        withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val mediaItem =
                    MediaItem
                        .Builder()
                        .setUri(sourceUri)
                        .setClippingConfiguration(
                            MediaItem.ClippingConfiguration
                                .Builder()
                                .setStartPositionMs(startMs.coerceAtLeast(0L))
                                .setEndPositionMs(endMs.coerceAtLeast(startMs + 1L))
                                .build(),
                        ).build()
                val editedMediaItem =
                    EditedMediaItem
                        .Builder(mediaItem)
                        .setRemoveVideo(true)
                        .build()
                val composition =
                    Composition
                        .Builder(
                            listOf(EditedMediaItemSequence.withAudioFrom(listOf(editedMediaItem))),
                        ).build()
                val listener =
                    object : Transformer.Listener {
                        override fun onCompleted(
                            composition: Composition,
                            exportResult: ExportResult,
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(outputFile)
                            }
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException,
                        ) {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }
                    }
                val transformer =
                    Transformer
                        .Builder(context)
                        .setLooper(Looper.getMainLooper())
                        .build()
                transformer.addListener(listener)
                continuation.invokeOnCancellation {
                    runCatching { transformer.cancel() }
                }
                runCatching {
                    transformer.start(composition, outputFile.absolutePath)
                }.onFailure {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }

    private fun resolveAudioUri(
        context: Context,
        assetInfo: AudiobookAssetInfo,
    ): Uri? =
        when (assetInfo.storageMode) {
            AudiobookStorageMode.Reference -> {
                assetInfo.sourceUriString?.let(Uri::parse)
            }

            AudiobookStorageMode.Copy -> {
                val relativePath = assetInfo.localRelativePath ?: return null
                val bookDir = findBookDirectory(context, assetInfo.bookId) ?: return null
                File(bookDir, relativePath).takeIf(File::isFile)?.let(Uri::fromFile)
            }
        }

    private fun findBookDirectory(
        context: Context,
        bookId: String,
    ): File? =
        File(context.filesDir, "Books")
            .listFiles()
            ?.firstOrNull { candidate ->
                candidate.isDirectory &&
                    File(candidate, "metadata.json")
                        .takeIf(File::isFile)
                        ?.readText()
                        ?.let { metadata ->
                            Regex(""""id"\s*:\s*"${Regex.escape(bookId)}"""").containsMatchIn(metadata)
                        } == true
            }
}
