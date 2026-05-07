package app.mori.reader.core.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberEpubPicker(onSelected: (List<String>) -> Unit): () -> Unit

@Composable
expect fun rememberDictionaryZipPicker(onSelected: (List<String>) -> Unit): () -> Unit

@Composable
expect fun rememberLocalAudioDatabasePicker(onSelected: (String) -> Unit): () -> Unit

@Composable
expect fun rememberAudiobookAudioPicker(onSelected: (String) -> Unit): () -> Unit

@Composable
expect fun rememberAudiobookSubtitlePicker(onSelected: (String) -> Unit): () -> Unit
