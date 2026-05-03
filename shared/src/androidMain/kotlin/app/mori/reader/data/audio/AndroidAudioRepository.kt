package app.mori.reader.data.audio

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun rememberAudioRepository(): AudioRepository {
    val applicationContext = LocalContext.current.applicationContext
    return remember(applicationContext) {
        AndroidAudioRepository(applicationContext)
    }
}

@Composable
actual fun rememberLocalAudioDatabasePicker(onSelected: (String) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
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

private class AndroidAudioRepository(
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
