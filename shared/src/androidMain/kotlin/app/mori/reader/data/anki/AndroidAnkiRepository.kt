package app.mori.reader.data.anki

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

private const val AnkiDroidPackage = "com.ichi2.anki"
private const val AnkiDroidPermission = "com.ichi2.anki.permission.READ_WRITE_DATABASE"
private const val AnkiDroidPermissionRequestCode = 2001
private const val Authority = "com.ichi2.anki.flashcards"
private const val FieldSeparator = "\u001f"

private val BaseUri: Uri = Uri.parse("content://$Authority")
private val NotesUri: Uri = Uri.withAppendedPath(BaseUri, "notes")
private val NotesV2Uri: Uri = Uri.withAppendedPath(BaseUri, "notes_v2")
private val ModelsUri: Uri = Uri.withAppendedPath(BaseUri, "models")
private val DecksUri: Uri = Uri.withAppendedPath(BaseUri, "decks")

private val AnkiProviderJson = Json { ignoreUnknownKeys = true }

@Composable
actual fun rememberAnkiRepository(): AnkiRepository {
    val context = LocalContext.current
    return remember(context) { AndroidAnkiRepository(context) }
}

private class AndroidAnkiRepository(
    private val context: Context,
) : AnkiRepository {
    override suspend fun ping(endpoint: String): Boolean {
        ensureAnkiDroidReady()
        return true
    }

    override suspend fun fetchDecksAndModels(endpoint: String): AnkiCatalog {
        ensureAnkiDroidReady()
        return withContext(Dispatchers.IO) {
            val decks = deckNames()
            val noteTypes = modelNames().map { model ->
                AnkiNoteType(name = model, fields = modelFieldNames(model))
            }
            AnkiCatalog(decks = decks, noteTypes = noteTypes)
        }
    }

    override suspend fun canAdd(settings: AnkiSettings, card: AnkiCardPayload): AnkiCanAddResult {
        ensureAnkiDroidReady()
        if (settings.allowDuplicates) return AnkiCanAddResult(canAdd = true)
        return withContext(Dispatchers.IO) {
            val deckIds = duplicateDeckIds(settings)
            val noteIds = findNotes(
                expression = card.expression,
                deckIds = deckIds,
            )
            if (noteIds.isEmpty()) {
                AnkiCanAddResult(canAdd = true)
            } else {
                AnkiCanAddResult(canAdd = false, message = "AnkiDroid 已存在重复卡片")
            }
        }
    }

    override suspend fun addNote(settings: AnkiSettings, card: AnkiCardPayload): Long {
        ensureAnkiDroidReady()
        return withContext(Dispatchers.IO) {
            val deckId = findDeckId(settings.selectedDeck)
            val modelId = findModelId(settings.selectedModel)
            val fieldNames = getModelFields(modelId)
            val fields = renderFields(settings, card)
            val values = Array(fieldNames.size) { index -> fields[fieldNames[index]].orEmpty() }
            val contentValues = ContentValues().apply {
                put("mid", modelId)
                put("flds", values.joinToString(FieldSeparator))
                val tags = splitAnkiTags(settings.tags)
                if (tags.isNotEmpty()) put("tags", tags.joinToString(" "))
            }
            val result = context.contentResolver.insert(NotesUri, contentValues)
                ?: throw IllegalStateException("AnkiDroid insert failed")
            val noteId = result.lastPathSegment?.toLongOrNull()
                ?: throw IllegalStateException("Failed to parse AnkiDroid note ID")
            moveCardsToDeck(noteId, deckId)
            noteId
        }
    }

    override suspend fun sync(endpoint: String) {
        // AnkiDroid provider inserts are local. AnkiDroid owns sync scheduling.
    }

    private fun renderFields(settings: AnkiSettings, card: AnkiCardPayload): Map<String, String> =
        settings.selectedFieldMappings.associate { mapping ->
            mapping.fieldName to renderAnkiTemplate(
                template = mapping.template,
                payload = card,
                audioFieldName = mapping.fieldName,
            )
        }

    private suspend fun ensureAnkiDroidReady() {
        if (!isAnkiDroidInstalled()) {
            throw IllegalStateException("未安装 AnkiDroid")
        }
        if (context.checkSelfPermission(AnkiDroidPermission) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        val activity = context.findActivity()
        if (activity != null) {
            activity.requestPermissions(arrayOf(AnkiDroidPermission), AnkiDroidPermissionRequestCode)
            throw IllegalStateException("已请求 AnkiDroid 权限，请同意后再刷新")
        }
        throw IllegalStateException("AnkiDroid 权限未授予")
    }

    private suspend fun isAnkiDroidInstalled(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            context.packageManager.getPackageInfo(AnkiDroidPackage, 0)
        }.isSuccess
    }

    private fun deckNames(): List<String> {
        val names = mutableListOf<String>()
        context.contentResolver.query(DecksUri, arrayOf("deck_name"), null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                cursor.getString(0)?.let(names::add)
            }
        }
        return names.sorted()
    }

    private fun modelNames(): List<String> {
        val names = mutableListOf<String>()
        context.contentResolver.query(ModelsUri, arrayOf("name"), null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                cursor.getString(0)?.let(names::add)
            }
        }
        return names.sorted()
    }

    private fun modelFieldNames(modelName: String): List<String> {
        context.contentResolver.query(
            ModelsUri,
            arrayOf("name", "field_names"),
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(0) == modelName) {
                    return parseFieldNames(cursor.getString(1).orEmpty())
                }
            }
        }
        return emptyList()
    }

    private fun findDeckId(deckName: String): Long {
        context.contentResolver.query(
            DecksUri,
            arrayOf("deck_id", "deck_name"),
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == deckName) return cursor.getLong(0)
            }
        }
        throw IllegalStateException("Deck '$deckName' not found")
    }

    private fun findDeckIdsByRoot(rootDeckName: String): Set<Long> {
        val ids = mutableSetOf<Long>()
        context.contentResolver.query(
            DecksUri,
            arrayOf("deck_id", "deck_name"),
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(1).orEmpty()
                if (name == rootDeckName || name.startsWith("$rootDeckName::")) {
                    ids += cursor.getLong(0)
                }
            }
        }
        return ids
    }

    private fun findModelId(modelName: String): Long {
        context.contentResolver.query(
            ModelsUri,
            arrayOf("_id", "name"),
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(1) == modelName) return cursor.getLong(0)
            }
        }
        throw IllegalStateException("Model '$modelName' not found")
    }

    private fun getModelFields(modelId: Long): List<String> {
        context.contentResolver.query(
            ModelsUri,
            arrayOf("_id", "field_names"),
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getLong(0) == modelId) {
                    return parseFieldNames(cursor.getString(1).orEmpty())
                }
            }
        }
        throw IllegalStateException("Model fields not found")
    }

    private fun findNotes(expression: String, deckIds: Set<Long>?): List<Long> {
        val noteIds = mutableListOf<Long>()
        val checksum = fieldChecksum(expression)
        context.contentResolver.query(
            NotesV2Uri,
            arrayOf("_id"),
            "csum=?",
            arrayOf(checksum.toString()),
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val noteId = cursor.getLong(0)
                if (deckIds == null || isNoteInAnyDeck(noteId, deckIds)) {
                    noteIds += noteId
                }
            }
        }
        return noteIds
    }

    private fun duplicateDeckIds(settings: AnkiSettings): Set<Long>? =
        when (settings.duplicateScope) {
            DuplicateScope.Collection -> null
            DuplicateScope.Deck -> setOf(findDeckId(settings.selectedDeck))
            DuplicateScope.DeckRoot -> findDeckIdsByRoot(settings.selectedDeck.substringBefore("::"))
        }

    private fun isNoteInAnyDeck(noteId: Long, deckIds: Set<Long>): Boolean {
        val cardsUri = Uri.withAppendedPath(Uri.withAppendedPath(NotesUri, noteId.toString()), "cards")
        context.contentResolver.query(cardsUri, arrayOf("deck_id"), null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getLong(0) in deckIds) return true
            }
        }
        return false
    }

    private fun moveCardsToDeck(noteId: Long, deckId: Long) {
        val cardsUri = Uri.withAppendedPath(NotesUri, "$noteId/cards")
        context.contentResolver.query(cardsUri, arrayOf("ord", "deck_id"), null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val ord = cursor.getInt(0)
                if (cursor.getLong(1) != deckId) {
                    val cardUri = Uri.withAppendedPath(cardsUri, ord.toString())
                    context.contentResolver.update(
                        cardUri,
                        ContentValues().apply { put("deck_id", deckId) },
                        null,
                        null,
                    )
                }
            }
        }
    }

    private fun parseFieldNames(rawData: String): List<String> {
        val trimmed = rawData.trim()
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return runCatching {
                AnkiProviderJson.parseToJsonElement(trimmed).jsonArray.map { item ->
                    runCatching { item.jsonObject["name"]?.jsonPrimitive?.content }
                        .getOrNull()
                        ?: item.jsonPrimitive.content
                }.filter { it.isNotBlank() }
            }.getOrDefault(emptyList())
        }
        return rawData.split(FieldSeparator).filter { it.isNotBlank() }
    }

    private fun fieldChecksum(data: String): Long {
        val digest = MessageDigest.getInstance("SHA-1")
            .digest(stripHtmlMedia(data).toByteArray(StandardCharsets.UTF_8))
        val hex = BigInteger(1, digest).toString(16).padStart(40, '0')
        return hex.substring(0, 8).toLong(16)
    }

    private fun stripHtmlMedia(text: String): String =
        text.replace(Regex("(?s)<style.*?>.*?</style>"), "")
            .replace(Regex("(?s)<script.*?>.*?</script>"), "")
            .replace(Regex("<img src=[\"']?([^\"'>]+)[\"']? ?/?>")) { " ${it.groupValues[1]} " }
            .replace(Regex("<.*?>"), "")
            .replace("&nbsp;", " ")
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
