package app.mori.reader.data.anki

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.ichi2.anki.api.AddContentApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnkiDroidTransport(
    private val context: Context,
    httpClient: HttpClient =
        HttpClient(CIO) {
            install(HttpTimeout)
            followRedirects = true
            expectSuccess = false
        },
) : AnkiTransport {
    private val appContext = context.applicationContext
    private val api by lazy { AddContentApi(appContext) }
    private val mediaStore = AnkiMediaStore(appContext)
    private val notePreparer = AnkiNotePreparer(appContext, httpClient)
    private val duplicateCache =
        appContext.getSharedPreferences("mori_anki_duplicate_cache", Context.MODE_PRIVATE)

    override val mode: AnkiConnectionMode = AnkiConnectionMode.AnkiDroid

    fun isAvailable(): Boolean = AddContentApi.getAnkiDroidPackageName(appContext) != null

    fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            appContext.checkSelfPermission(READ_WRITE_PERMISSION) == PackageManager.PERMISSION_GRANTED

    override suspend fun ping(settings: AnkiSettings): Boolean {
        requireAvailableAndPermitted()
        requireApiReady()
        return true
    }

    override suspend fun fetchDecksAndModels(settings: AnkiSettings): AnkiFetchResult {
        requireAvailableAndPermitted()
        val decks =
            requireDeckList()
                .map { (id, name) -> AnkiDeck(id = id.toString(), name = name) }
        val noteTypes =
            requireModelList()
                .map { (id, name) ->
                    AnkiNoteType(
                        id = id.toString(),
                        name = name,
                        fields =
                            api
                                .getFieldList(id)
                                ?.map(::AnkiField)
                                .orEmpty(),
                    )
                }
        return AnkiFetchResult(decks = decks, noteTypes = noteTypes)
    }

    override suspend fun addNote(
        settings: AnkiSettings,
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    ) {
        if (!isAvailable() || !hasPermission()) {
            throw AnkiLocalApiUnavailableException("AnkiDroid is unavailable or its database permission is missing")
        }
        val decks = api.getDeckList().orEmpty()
        val models = api.getModelList().orEmpty()
        val deckId = decks.firstKeyForValue(settings.selectedDeck.requireConfigured("Anki deck"))
        val modelId = models.firstKeyForValue(settings.selectedNoteType.requireConfigured("Anki note type"))
        val fieldNames = api.getFieldList(modelId)?.toList().orEmpty()
        val preparedNote = notePreparer.prepare(settings, content, context).withAnkiDroidMedia()
        val fields =
            fieldNames
                .ifEmpty { settings.fieldMappings.keys.toList() }
                .map { field -> preparedNote.fields[field].orEmpty() }
                .toTypedArray()
        val noteId = api.addNote(modelId, deckId, fields, settings.tags.toSet())
        check(noteId != null) { "AnkiDroid refused the note; check API status and model fields" }
        cacheDuplicate(content.expression)
    }

    override suspend fun checkDuplicate(
        settings: AnkiSettings,
        expression: String,
    ): Boolean {
        if (expression.isBlank()) return false
        if (!isAvailable() || !hasPermission()) return isCachedDuplicate(expression)
        return withContext(Dispatchers.IO) {
            val models = api.getModelList().orEmpty()
            val modelId =
                settings.selectedNoteType
                    ?.let { selected -> models.firstKeyForValueOrNull(selected) }
                    ?: return@withContext isCachedDuplicate(expression)
            val duplicates = api.findDuplicateNotes(modelId, expression)
            !duplicates.isNullOrEmpty() || isCachedDuplicate(expression)
        }
    }

    override suspend fun sync(settings: AnkiSettings) {
        // The Instant-Add API does not expose sync. Keep this a no-op for AnkiDroid mode.
    }

    private fun requireAvailableAndPermitted() {
        check(isAvailable()) { "AnkiDroid is not installed or its API is disabled" }
        check(hasPermission()) { "AnkiDroid database permission is not granted" }
    }

    private fun requireApiReady() {
        requireDeckList()
        requireModelList()
    }

    private fun requireDeckList(): Map<Long, String> =
        api.getDeckList()
            ?: throw IllegalStateException("AnkiDroid is not ready. Open AnkiDroid and make sure its API is available")

    private fun requireModelList(): Map<Long, String> =
        api.getModelList()
            ?: throw IllegalStateException("AnkiDroid is not ready. Open AnkiDroid and make sure its API is available")

    private fun cacheDuplicate(expression: String) {
        if (expression.isBlank()) return
        duplicateCache.edit().putBoolean(expression, true).apply()
    }

    private fun isCachedDuplicate(expression: String): Boolean = duplicateCache.getBoolean(expression, false)

    override fun recordAddedExpression(expression: String) {
        cacheDuplicate(expression)
    }

    private fun PreparedAnkiNote.withAnkiDroidMedia(): PreparedAnkiNote {
        var updatedFields = fields
        dictionaryMedia.forEach { media ->
            val fileName = addMedia(media, ANKI_MEDIA_IMAGE)?.ankiMediaFileName() ?: return@forEach
            updatedFields =
                updatedFields.mapValues { (_, value) ->
                    value.replace(media.temporaryName, fileName)
                }
        }
        wordAudio?.let { media ->
            val audioMarkup = addMedia(media, ANKI_MEDIA_AUDIO).orEmpty()
            updatedFields = updatedFields.replaceExactMediaFields(audioFields, audioMarkup)
        }
        sasayakiAudio?.let { media ->
            val audioMarkup = addMedia(media, ANKI_MEDIA_AUDIO).orEmpty()
            updatedFields = updatedFields.replaceExactMediaFields(sasayakiAudioFields, audioMarkup)
        }
        bookCover?.let { media ->
            val pictureMarkup = addMedia(media, ANKI_MEDIA_IMAGE).orEmpty()
            updatedFields = updatedFields.replaceExactMediaFields(pictureFields, pictureMarkup)
        }
        return copy(fields = updatedFields.mapValues { (_, value) -> value.normalizeAnkiDictionaryHtml() })
    }

    private fun addMedia(
        media: PreparedAnkiMedia,
        mediaType: String,
    ): String? {
        val ankiPackage = AddContentApi.getAnkiDroidPackageName(appContext) ?: return null
        val uri = mediaStore.writeMedia(media) ?: return null
        appContext.grantUriPermission(ankiPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return try {
            api.addMediaFromUri(uri, media.fileName.substringBeforeLast('.', media.fileName), mediaType)
        } finally {
            appContext.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun Map<String, String>.replaceExactMediaFields(
        fieldNames: List<String>,
        replacement: String,
    ): Map<String, String> =
        mapValues { (field, value) ->
            if (field in fieldNames) replacement else value
        }

    private fun String.ankiMediaFileName(): String? =
        soundMediaPattern.find(this)?.groupValues?.getOrNull(1)
            ?: imageMediaPattern.find(this)?.groupValues?.getOrNull(1)

    private fun Map<Long, String>.firstKeyForValue(value: String): Long =
        firstKeyForValueOrNull(value) ?: throw IllegalStateException("Anki item not found: $value")

    private fun Map<Long, String>.firstKeyForValueOrNull(value: String): Long? = entries.firstOrNull { it.value == value }?.key

    private fun String?.requireConfigured(label: String): String =
        this?.takeIf(String::isNotBlank) ?: throw IllegalStateException("$label is not selected")

    private companion object {
        const val READ_WRITE_PERMISSION = AddContentApi.READ_WRITE_PERMISSION
        const val ANKI_MEDIA_AUDIO = "audio"
        const val ANKI_MEDIA_IMAGE = "image"
        val soundMediaPattern = Regex("""\[sound:([^]]+)]""")
        val imageMediaPattern = Regex("""<img\s+src="([^"]+)".*?>""")
    }
}
