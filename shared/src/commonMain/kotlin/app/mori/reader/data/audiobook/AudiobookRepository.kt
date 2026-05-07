package app.mori.reader.data.audiobook

import kotlinx.coroutines.flow.Flow

interface AudiobookRepository {
    fun observeAssets(bookId: String): Flow<AudiobookAssetBundle>

    suspend fun loadAssets(bookId: String): AudiobookAssetBundle

    suspend fun importAudio(
        bookId: String,
        uriString: String,
        storageMode: AudiobookStorageMode,
    ): AudiobookAssetBundle

    suspend fun importSubtitle(
        bookId: String,
        uriString: String,
        storageMode: AudiobookStorageMode = AudiobookStorageMode.Copy,
    ): AudiobookAssetBundle

    suspend fun runMatch(
        bookId: String,
        searchWindow: Int,
    ): AudiobookAssetBundle

    suspend fun deleteMatch(bookId: String): AudiobookAssetBundle

    suspend fun deleteAsset(
        bookId: String,
        type: AudiobookAssetType,
    ): AudiobookAssetBundle
}
