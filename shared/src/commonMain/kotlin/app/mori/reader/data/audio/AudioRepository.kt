package app.mori.reader.data.audio

interface AudioRepository {
    suspend fun importLocalAudioDatabase(uriString: String): Long

    suspend fun deleteLocalAudioDatabase(): Long

    suspend fun localAudioDatabaseSizeBytes(): Long
}
