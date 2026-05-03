package app.mori.reader.data.audio

import androidx.compose.runtime.Composable

interface AudioRepository {
    suspend fun importLocalAudioDatabase(uriString: String): Long
    suspend fun deleteLocalAudioDatabase(): Long
    suspend fun localAudioDatabaseSizeBytes(): Long
}

@Composable
expect fun rememberAudioRepository(): AudioRepository

@Composable
expect fun rememberLocalAudioDatabasePicker(onSelected: (String) -> Unit): () -> Unit
