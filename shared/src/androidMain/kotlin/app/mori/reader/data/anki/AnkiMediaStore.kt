package app.mori.reader.data.anki

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.File

class AnkiMediaStore(
    private val context: Context,
) {
    private val appContext = context.applicationContext

    fun writeMedia(media: PreparedAnkiMedia): Uri? {
        val data = media.dataBase64.takeIf(String::isNotBlank) ?: return null
        val fileName = media.fileName.takeIf(String::isNotBlank) ?: return null
        val directory = File(context.cacheDir, "anki-media").also { it.mkdirs() }
        val file = File(directory, fileName)
        file.writeBytes(Base64.decode(data, Base64.DEFAULT))
        return FileProvider.getUriForFile(appContext, "${appContext.packageName}.anki.media", file)
    }
}
