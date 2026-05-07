package app.mori.reader.core.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

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
actual fun rememberAudiobookAudioPicker(onSelected: (String) -> Unit): () -> Unit {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { onSelected(it.toString()) }
        }
    return { launcher.launch(arrayOf("audio/*", "application/octet-stream")) }
}

@Composable
actual fun rememberAudiobookSubtitlePicker(onSelected: (String) -> Unit): () -> Unit {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { onSelected(it.toString()) }
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
