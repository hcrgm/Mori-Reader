package app.mori.reader.core.platform

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ichi2.anki.api.AddContentApi

@Composable
actual fun rememberEpubPicker(onSelected: (List<String>) -> Unit): () -> Unit {
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris ->
            if (uris.isNotEmpty()) {
                onSelected(uris.map { it.toString() })
            }
        }
    return {
        launcher.launch(arrayOf("application/epub+zip", "application/octet-stream"))
    }
}

@Composable
actual fun rememberDictionaryZipPicker(onSelected: (List<String>) -> Unit): () -> Unit {
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenMultipleDocuments(),
        ) { uris ->
            if (uris.isNotEmpty()) {
                onSelected(uris.map { it.toString() })
            }
        }
    return {
        launcher.launch(
            arrayOf(
                "application/zip",
                "application/x-zip-compressed",
                "application/octet-stream",
            ),
        )
    }
}

@Composable
actual fun rememberLocalAudioDatabasePicker(onSelected: (String) -> Unit): () -> Unit {
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                onSelected(uri.toString())
            }
        }
    return {
        launcher.launch(arrayOf("application/octet-stream", "application/vnd.sqlite3", "*/*"))
    }
}

@Composable
actual fun rememberAudiobookAudioPicker(onSelected: (PickedDocument) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                onSelected(
                    PickedDocument(
                        uriString = it.toString(),
                        displayName = context.displayName(it) ?: it.decodedFallbackName("audio"),
                    ),
                )
            }
        }
    return { launcher.launch(arrayOf("audio/*", "application/octet-stream")) }
}

@Composable
actual fun rememberAudiobookSubtitlePicker(onSelected: (PickedDocument) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                onSelected(
                    PickedDocument(
                        uriString = it.toString(),
                        displayName = context.displayName(it) ?: it.decodedFallbackName("subtitle.srt"),
                    ),
                )
            }
        }
    return {
        launcher.launch(
            arrayOf(
                "text/*",
                "application/x-subrip",
                "application/octet-stream",
            ),
        )
    }
}

private fun android.content.Context.displayName(uri: Uri): String? =
    runCatching {
        contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index)?.takeIf(String::isNotBlank) else null
            }
    }.getOrNull()

private fun Uri.decodedFallbackName(defaultName: String): String =
    Uri
        .decode(lastPathSegment.orEmpty())
        .substringAfterLast('/')
        .substringAfterLast(':')
        .substringBefore('?')
        .ifBlank { defaultName }

@Composable
actual fun rememberAnkiDroidPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit {
    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            onResult(granted)
        }
    return {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            onResult(true)
        } else {
            launcher.launch(AddContentApi.READ_WRITE_PERMISSION)
        }
    }
}
