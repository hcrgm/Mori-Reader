package app.mori.reader.ui.pages.dictionary

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import de.manhhao.hoshi.HoshiDicts
import java.io.ByteArrayInputStream

internal object DictionaryExternalLinkOpener {
    fun open(
        context: Context?,
        url: String,
    ) {
        val appContext = context?.applicationContext ?: return
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            appContext.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }
}

internal class DictionaryWebResourceHandler(
    private val audioSourceResolver: DictionaryAudioSourceResolver,
) {
    fun shouldOverrideUrlLoading(
        context: Context?,
        request: WebResourceRequest,
    ): Boolean {
        val url = request.url?.toString().orEmpty()
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            DictionaryExternalLinkOpener.open(context, url)
            true
        } else {
            false
        }
    }

    fun shouldInterceptRequest(
        request: WebResourceRequest,
        context: Context?,
    ): WebResourceResponse? {
        val uri = request.url ?: return null
        return when (uri.scheme) {
            "image" -> {
                imageResponse(uri)
            }

            "audio" -> {
                audioResponse(
                    context = context,
                    sourceUrl = uri.getQueryParameter("url").orEmpty(),
                )
            }

            else -> {
                null
            }
        }
    }

    private fun imageResponse(uri: Uri): WebResourceResponse? {
        val dictionary = uri.getQueryParameter("dictionary") ?: return null
        val path = uri.getQueryParameter("path") ?: return null
        val bytes =
            HoshiDicts.getMediaFile(
                HoshiDicts.lookupObject,
                dictionary,
                path,
            ) ?: return null
        return WebResourceResponse(
            mimeTypeFor(path),
            null,
            ByteArrayInputStream(bytes),
        )
    }

    private fun audioResponse(
        context: Context?,
        sourceUrl: String,
    ): WebResourceResponse {
        val body = audioSourceResolver.resolveAudioSourceListJson(context, sourceUrl)
        return WebResourceResponse(
            "application/json",
            "UTF-8",
            ByteArrayInputStream(body.toByteArray()),
        )
    }
}

private fun mimeTypeFor(path: String): String =
    when (path.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "svg" -> "image/svg+xml"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }
