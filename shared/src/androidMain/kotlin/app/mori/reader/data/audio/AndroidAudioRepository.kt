package app.mori.reader.data.audio

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidAudioRepository(
    private val context: Context,
) : AudioRepository {
    override suspend fun importLocalAudioDatabase(uriString: String): Long =
        withContext(Dispatchers.IO) {
            AndroidLocalAudioStore.importDatabase(context, uriString)
        }

    override suspend fun deleteLocalAudioDatabase(): Long =
        withContext(Dispatchers.IO) {
            AndroidLocalAudioStore.deleteDatabase(context)
        }

    override suspend fun localAudioDatabaseSizeBytes(): Long =
        withContext(Dispatchers.IO) {
            AndroidLocalAudioStore.databaseSizeBytes(context)
        }
}
