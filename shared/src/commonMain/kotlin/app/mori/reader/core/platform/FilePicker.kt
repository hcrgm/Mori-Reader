package app.mori.reader.core.platform

import androidx.compose.runtime.Composable

data class PickedDocument(
    val uriString: String,
    val displayName: String,
)

@Composable
expect fun rememberEpubPicker(onSelected: (List<String>) -> Unit): () -> Unit

@Composable
expect fun rememberDictionaryZipPicker(onSelected: (List<String>) -> Unit): () -> Unit

@Composable
expect fun rememberLocalAudioDatabasePicker(onSelected: (String) -> Unit): () -> Unit

@Composable
expect fun rememberAudiobookAudioPicker(onSelected: (PickedDocument) -> Unit): () -> Unit

@Composable
expect fun rememberAudiobookSubtitlePicker(onSelected: (PickedDocument) -> Unit): () -> Unit

@Composable
expect fun rememberAnkiDroidPermissionRequester(onResult: (Boolean) -> Unit): () -> Unit
