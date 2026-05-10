package app.mori.reader.data.anki

import android.content.Context
import android.net.Uri
import android.util.Base64
import app.mori.reader.data.audio.AndroidLocalAudioStore
import de.manhhao.hoshi.HoshiDicts
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.isSuccess
import java.io.File
import java.security.MessageDigest

class AnkiNotePreparer(
    context: Context,
    private val httpClient: HttpClient,
) {
    private val appContext = context.applicationContext

    suspend fun prepare(
        settings: AnkiSettings,
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    ): PreparedAnkiNote {
        val audioFields = settings.exactFields("{audio}")
        val sasayakiAudioFields = settings.exactFields("{sasayaki-audio}")
        val pictureFields = settings.exactFields("{book-cover}")
        return PreparedAnkiNote(
            fields = AnkiTemplateRenderer.render(settings.fieldMappings, content, context).fields,
            audioFields = audioFields,
            sasayakiAudioFields = sasayakiAudioFields,
            pictureFields = pictureFields,
            dictionaryMedia = content.dictionaryMedia.mapNotNull(::prepareDictionaryMedia),
            wordAudio = if (audioFields.isNotEmpty()) prepareWordAudio(content.audio) else null,
            sasayakiAudio = if (sasayakiAudioFields.isNotEmpty()) prepareSasayakiAudio(context) else null,
            bookCover = if (pictureFields.isNotEmpty()) prepareBookCover(context.coverUri) else null,
        )
    }

    private fun prepareDictionaryMedia(media: DictionaryMedia): PreparedAnkiMedia? {
        val dictionary = media.dictionary.takeIf(String::isNotBlank) ?: return null
        val path = media.path.takeIf(String::isNotBlank) ?: return null
        val bytes =
            HoshiDicts.getMediaFile(
                HoshiDicts.lookupObject,
                dictionary,
                path,
            ) ?: return null
        val extension = path.extensionOr("bin")
        return PreparedAnkiMedia(
            temporaryName = media.filename,
            fileName = "mori_dict_${bytes.sha1()}.$extension",
            mimeType = mimeTypeFor(path),
            dataBase64 = bytes.base64(),
        )
    }

    private suspend fun prepareWordAudio(audioUrl: String): PreparedAnkiMedia? {
        if (audioUrl.isBlank()) return null
        val bytes =
            if (audioUrl.startsWith("local://audio-file")) {
                AndroidLocalAudioStore.audioBytes(appContext, audioUrl)
            } else {
                val response = httpClient.get(audioUrl)
                if (response.status.isSuccess()) response.bodyAsBytes() else null
            } ?: return null
        return PreparedAnkiMedia(
            fileName = "mori_audio_${bytes.sha1()}.mp3",
            mimeType = "audio/mpeg",
            dataBase64 = bytes.base64(),
        )
    }

    private suspend fun prepareSasayakiAudio(context: AnkiMiningContext): PreparedAnkiMedia? {
        val assetInfo = context.sasayakiAudioAssetInfo ?: return null
        val startMs = context.sasayakiAudioStartMs ?: return null
        val endMs = context.sasayakiAudioEndMs ?: return null
        val bytes =
            AndroidSasayakiAudioExtractor.extractCueAudio(
                context = appContext,
                assetInfo = assetInfo,
                startMs = startMs,
                endMs = endMs,
            ) ?: return null
        val fileName =
            context.sasayakiAudioFileName
                ?.takeIf { it.endsWith(".m4a", ignoreCase = true) }
                ?: "mori_sasayaki_${bytes.sha1()}.m4a"
        return PreparedAnkiMedia(
            fileName = fileName,
            mimeType = "audio/mp4",
            dataBase64 = bytes.base64(),
        )
    }

    private fun prepareBookCover(coverUri: String?): PreparedAnkiMedia? {
        val uri = coverUri?.takeIf(String::isNotBlank) ?: return null
        val bytes =
            runCatching {
                when {
                    uri.startsWith("content://") -> {
                        appContext.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
                    }

                    uri.startsWith("file://") -> {
                        File(Uri.parse(uri).path.orEmpty()).takeIf(File::isFile)?.readBytes()
                    }

                    else -> {
                        File(uri).takeIf(File::isFile)?.readBytes()
                    }
                }
            }.getOrNull() ?: return null
        val extension = uri.substringBefore('?').extensionOr("jpg")
        return PreparedAnkiMedia(
            fileName = "mori_cover_${bytes.sha1()}.$extension",
            mimeType = mimeTypeFor(uri),
            dataBase64 = bytes.base64(),
        )
    }
}

fun AnkiSettings.exactFields(handlebar: String): List<String> = fieldMappings.filterValues { it == handlebar }.keys.toList()

fun ByteArray.base64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

fun ByteArray.sha1(): String {
    val digest = MessageDigest.getInstance("SHA-1").digest(this)
    return digest.joinToString("") { byte -> "%02x".format(byte) }
}

fun String.extensionOr(defaultValue: String): String =
    substringAfterLast('/', this)
        .substringBefore('?')
        .substringAfterLast('.', "")
        .lowercase()
        .ifBlank { defaultValue }

fun mimeTypeFor(path: String): String =
    when (path.extensionOr("")) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "svg" -> "image/svg+xml"
        "webp" -> "image/webp"
        "avif" -> "image/avif"
        "heic" -> "image/heic"
        "mp3" -> "audio/mpeg"
        "m4a" -> "audio/mp4"
        "ogg" -> "audio/ogg"
        "wav" -> "audio/wav"
        else -> "application/octet-stream"
    }
