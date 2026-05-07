package app.mori.reader.data.audio

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

object AndroidLocalAudioStore {
    private val defaultSources =
        listOf(
            "nhk16",
            "daijisen",
            "shinmeikai8",
            "jpod",
            "jpod_alternate",
            "taas",
            "ozk5",
            "forvo",
            "forvo_ext",
            "forvo_ext2",
        )

    fun databaseFile(context: Context): File = File(context.filesDir, "Audio/android.db")

    private fun tempDatabaseFile(context: Context): File = File(context.filesDir, "Audio/android.db.importing")

    private fun backupDatabaseFile(context: Context): File = File(context.filesDir, "Audio/android.db.backup")

    fun databaseSizeBytes(context: Context): Long =
        databaseFile(context)
            .also { recoverIfNeeded(context) }
            .takeIf { it.isFile }
            ?.length() ?: 0L

    fun importDatabase(
        context: Context,
        uriString: String,
    ): Long {
        val uri = uriString.toUri()
        val target = databaseFile(context)
        val temp = tempDatabaseFile(context)
        val backup = backupDatabaseFile(context)
        recoverIfNeeded(context)
        target.parentFile?.mkdirs()

        temp.delete()
        try {
            context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "无法读取 android.db" }
                temp.outputStream().use { output -> input.copyTo(output) }
            }
            validateDatabase(temp)

            if (target.isFile) {
                backup.delete()
                require(target.renameTo(backup)) { "无法创建 android.db 备份" }
            }

            require(temp.renameTo(target)) { "无法替换 android.db" }
            backup.delete()
            return target.length()
        } catch (throwable: Throwable) {
            temp.delete()
            if (!target.isFile && backup.isFile) {
                backup.renameTo(target)
            }
            throw throwable
        }
    }

    fun deleteDatabase(context: Context): Long {
        recoverIfNeeded(context)
        databaseFile(context).delete()
        tempDatabaseFile(context).delete()
        backupDatabaseFile(context).delete()
        return 0L
    }

    fun audioSourceListJson(
        context: Context,
        sourceUrl: String,
    ): String {
        recoverIfNeeded(context)
        val uri = sourceUrl.toUri()
        val term = uri.getQueryParameter("term").orEmpty()
        val reading = katakanaToHiragana(uri.getQueryParameter("reading").orEmpty())
        val match = findAudioFile(context, term, reading)
        return if (match == null) {
            EMPTY_AUDIO_RESPONSE
        } else {
            val url =
                Uri
                    .Builder()
                    .scheme("local")
                    .authority("audio-file")
                    .appendQueryParameter("source", match.source)
                    .appendQueryParameter("file", match.file)
                    .build()
                    .toString()
            """{"type":"audioSourceList","audioSources":[{"name":${jsonString(match.source)},"url":${
                jsonString(
                    url,
                )
            }}]}"""
        }
    }

    fun audioBytes(
        context: Context,
        audioUrl: String,
    ): ByteArray? {
        recoverIfNeeded(context)
        val uri = audioUrl.toUri()
        if (uri.scheme != "local" || uri.host != "audio-file") return null
        val source = uri.getQueryParameter("source") ?: return null
        val file = uri.getQueryParameter("file") ?: return null
        val dbFile = databaseFile(context)
        if (!dbFile.isFile) return null

        return SQLiteDatabase
            .openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            .use { db ->
                db
                    .rawQuery(
                        "SELECT data FROM android WHERE source = ? AND file = ? LIMIT 1",
                        arrayOf(source, file),
                    ).use { cursor ->
                        if (cursor.moveToFirst()) cursor.getBlob(0) else null
                    }
            }
    }

    private fun findAudioFile(
        context: Context,
        term: String,
        reading: String,
    ): AudioFile? {
        recoverIfNeeded(context)
        val dbFile = databaseFile(context)
        if (!dbFile.isFile || term.isBlank()) return null

        return runCatching {
            SQLiteDatabase
                .openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
                .use { db ->
                    val sortOrder =
                        "CASE source " +
                            List(defaultSources.size) { index ->
                                "WHEN ? THEN $index "
                            }.joinToString("") + "ELSE 999 END"
                    val args = mutableListOf<String>()
                    val sql =
                        if (reading.isBlank()) {
                            args += term
                            """
                            SELECT source, file FROM entries
                            WHERE expression = ? AND file LIKE '%.mp3'
                            ORDER BY $sortOrder
                            LIMIT 1
                            """.trimIndent()
                        } else {
                            args += term
                            args += reading
                            args += reading
                            """
                            SELECT source, file FROM entries
                            WHERE (expression = ? OR reading = ?) AND file LIKE '%.mp3'
                            ORDER BY CASE WHEN reading = ? THEN 0 ELSE 1 END, $sortOrder
                            LIMIT 1
                            """.trimIndent()
                        }
                    args += defaultSources
                    db.rawQuery(sql, args.toTypedArray()).use { cursor ->
                        if (cursor.moveToFirst()) {
                            AudioFile(cursor.getString(0), cursor.getString(1))
                        } else {
                            null
                        }
                    }
                }
        }.getOrNull()
    }

    private fun katakanaToHiragana(text: String): String =
        buildString {
            text.forEach { char ->
                append(
                    if (char in '\u30A1'..'\u30F6') {
                        (char.code - 0x60).toChar()
                    } else {
                        char
                    },
                )
            }
        }

    private fun jsonString(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    private fun validateDatabase(file: File) {
        require(file.isFile && file.length() > 0L) { "android.db 导入失败" }
        SQLiteDatabase
            .openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            .use { db ->
                db
                    .rawQuery(
                        "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN ('entries', 'android')",
                        emptyArray(),
                    ).use { cursor ->
                        val tables = mutableSetOf<String>()
                        while (cursor.moveToNext()) {
                            tables += cursor.getString(0)
                        }
                        require("entries" in tables && "android" in tables) { "android.db 结构无效" }
                    }
            }
    }

    private fun recoverIfNeeded(context: Context) {
        val target = databaseFile(context)
        val temp = tempDatabaseFile(context)
        val backup = backupDatabaseFile(context)

        if (temp.exists()) {
            temp.delete()
        }

        if (backup.isFile && !target.isFile) {
            require(backup.renameTo(target)) { "无法恢复 android.db 备份" }
        } else if (backup.exists()) {
            backup.delete()
        }
    }

    private data class AudioFile(
        val source: String,
        val file: String,
    )

    private const val EMPTY_AUDIO_RESPONSE = """{"type":"audioSourceList","audioSources":[]}"""
}
