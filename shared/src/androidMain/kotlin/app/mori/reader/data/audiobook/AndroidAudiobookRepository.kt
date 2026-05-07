package app.mori.reader.data.audiobook

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import app.mori.reader.data.filteredReaderText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI
import java.nio.charset.Charset
import java.util.UUID

internal class AndroidAudiobookRepository(
    private val context: Context,
) : AudiobookRepository {
    private val booksRoot = File(context.filesDir, BOOKS_DIR_NAME)
    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
        }

    private val assetBundles = MutableStateFlow<Map<String, AudiobookAssetBundle>>(emptyMap())

    override fun observeAssets(bookId: String): Flow<AudiobookAssetBundle> = assetBundles.mapNotNull { it[bookId] }

    override suspend fun loadAssets(bookId: String): AudiobookAssetBundle =
        withContext(Dispatchers.IO) {
            val bookDir = findBookDirectory(bookId) ?: throw IllegalArgumentException("图书不存在")
            loadBundle(bookDir).also { publishBundle(bookId, it) }
        }

    override suspend fun importAudio(
        bookId: String,
        uriString: String,
        storageMode: AudiobookStorageMode,
    ): AudiobookAssetBundle =
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(uriString)
            val bookDir = findBookDirectory(bookId) ?: throw IllegalArgumentException("图书不存在")
            val displayName =
                displayName(uri) ?: uri.lastPathSegment
                    .orEmpty()
                    .substringAfterLast('/')
                    .ifBlank { "audio" }
            val format =
                audioFormat(displayName, mimeType(uri))
                    ?: throw IllegalArgumentException("仅支持 MP3 / M4B 音频")
            val sizeBytes = sizeBytes(uri)
            val mimeType = mimeType(uri)
            val assets = loadAssetsStorage(bookDir).copy(audio = null)
            val existing = loadAssetsStorage(bookDir).audio
            deleteLocalAsset(bookDir, existing)

            val asset =
                if (storageMode == AudiobookStorageMode.Reference) {
                    takePersistableReadPermission(uri)
                    AudiobookAssetInfo(
                        bookId = bookId,
                        type = AudiobookAssetType.Audio,
                        format = format.extension,
                        displayName = displayName,
                        storageMode = AudiobookStorageMode.Reference,
                        importedAt = System.currentTimeMillis(),
                        fileSizeBytes = sizeBytes,
                        mimeType = mimeType,
                        sourceUriString = uriString,
                    )
                } else {
                    val target = File(bookDir, "$AUDIOBOOK_DIR/audio.${format.extension}")
                    copyUriToFile(uri, target)
                    AudiobookAssetInfo(
                        bookId = bookId,
                        type = AudiobookAssetType.Audio,
                        format = format.extension,
                        displayName = displayName,
                        storageMode = AudiobookStorageMode.Copy,
                        importedAt = System.currentTimeMillis(),
                        fileSizeBytes = target.length(),
                        mimeType = mimeType,
                        localRelativePath = "$AUDIOBOOK_DIR/${target.name}",
                    )
                }
            saveAssetsStorage(bookDir, assets.copy(audio = asset))
            loadBundle(bookDir).also { publishBundle(bookId, it) }
        }

    override suspend fun importSubtitle(
        bookId: String,
        uriString: String,
        storageMode: AudiobookStorageMode,
    ): AudiobookAssetBundle =
        withContext(Dispatchers.IO) {
            val uri = Uri.parse(uriString)
            val bookDir = findBookDirectory(bookId) ?: throw IllegalArgumentException("图书不存在")
            val displayName =
                displayName(uri) ?: uri.lastPathSegment
                    .orEmpty()
                    .substringAfterLast('/')
                    .ifBlank { "subtitle.srt" }
            val format =
                subtitleFormat(displayName, mimeType(uri))
                    ?: throw IllegalArgumentException("仅支持 SRT 字幕")
            val bytes = readUriBytes(uri)
            val cues = parseSrt(decodeText(bytes))
            val parsedAt = System.currentTimeMillis()
            val target = File(bookDir, "$AUDIOBOOK_DIR/subtitle.${format.extension}")
            target.parentFile?.mkdirs()
            target.writeBytes(bytes)

            val existing = loadAssetsStorage(bookDir).subtitle
            deleteLocalAsset(bookDir, existing)
            val assets = loadAssetsStorage(bookDir).copy(subtitle = null)
            val asset =
                AudiobookAssetInfo(
                    bookId = bookId,
                    type = AudiobookAssetType.Subtitle,
                    format = format.extension,
                    displayName = displayName,
                    storageMode = AudiobookStorageMode.Copy,
                    importedAt = parsedAt,
                    fileSizeBytes = target.length(),
                    mimeType = mimeType(uri),
                    localRelativePath = "$AUDIOBOOK_DIR/${target.name}",
                )
            val subtitleData =
                AudiobookSubtitleData(
                    format = format,
                    cues = cues,
                    sourceAssetDisplayName = displayName,
                    parsedAt = parsedAt,
                )
            saveAssetsStorage(bookDir, assets.copy(subtitle = asset))
            saveSubtitleData(bookDir, subtitleData)
            File(bookDir, MATCH_DATA_FILE).delete()
            loadBundle(bookDir).also { publishBundle(bookId, it) }
        }

    override suspend fun runMatch(
        bookId: String,
        searchWindow: Int,
    ): AudiobookAssetBundle =
        withContext(Dispatchers.IO) {
            val bookDir = findBookDirectory(bookId) ?: throw IllegalArgumentException("图书不存在")
            val subtitleData =
                loadSubtitleData(bookDir) ?: throw IllegalArgumentException("请先导入 SRT 字幕")
            val chapters = loadMatchChapters(bookDir)
            require(chapters.isNotEmpty()) { "没有可匹配章节" }
            val clampedWindow = searchWindow.coerceIn(MIN_SEARCH_WINDOW, MAX_SEARCH_WINDOW)
            val matchData = buildMatchData(subtitleData, chapters, clampedWindow)
            saveMatchData(bookDir, matchData)
            loadBundle(bookDir).also { publishBundle(bookId, it) }
        }

    override suspend fun deleteMatch(bookId: String): AudiobookAssetBundle =
        withContext(Dispatchers.IO) {
            val bookDir = findBookDirectory(bookId) ?: throw IllegalArgumentException("图书不存在")
            File(bookDir, MATCH_DATA_FILE).delete()
            loadBundle(bookDir).also { publishBundle(bookId, it) }
        }

    override suspend fun deleteAsset(
        bookId: String,
        type: AudiobookAssetType,
    ): AudiobookAssetBundle =
        withContext(Dispatchers.IO) {
            val bookDir = findBookDirectory(bookId) ?: throw IllegalArgumentException("图书不存在")
            val assets = loadAssetsStorage(bookDir)
            val next =
                when (type) {
                    AudiobookAssetType.Audio -> {
                        deleteLocalAsset(bookDir, assets.audio)
                        assets.copy(audio = null)
                    }

                    AudiobookAssetType.Subtitle -> {
                        deleteLocalAsset(bookDir, assets.subtitle)
                        File(bookDir, SUBTITLE_DATA_FILE).delete()
                        File(bookDir, MATCH_DATA_FILE).delete()
                        assets.copy(subtitle = null)
                    }
                }
            saveAssetsStorage(bookDir, next)
            loadBundle(bookDir).also { publishBundle(bookId, it) }
        }

    private fun publishBundle(
        bookId: String,
        bundle: AudiobookAssetBundle,
    ) {
        assetBundles.update { it + (bookId to bundle) }
    }

    private fun loadBundle(bookDir: File): AudiobookAssetBundle {
        val assets = loadAssetsStorage(bookDir)
        return AudiobookAssetBundle(
            audioAssetInfo = assets.audio,
            subtitleAssetInfo = assets.subtitle,
            subtitleData = loadSubtitleData(bookDir),
            matchData = loadMatchData(bookDir),
        )
    }

    private fun findBookDirectory(bookId: String): File? =
        booksRoot
            .listFiles()
            ?.filter { it.isDirectory }
            ?.firstOrNull { dir -> loadBookMetadata(dir)?.id == bookId }

    private fun loadBookMetadata(bookDir: File): BookMetadataStorage? =
        runCatching {
            json.decodeFromString(
                BookMetadataStorage.serializer(),
                File(bookDir, METADATA_FILE).readText(),
            )
        }.getOrNull()

    private fun loadAssetsStorage(bookDir: File): AudiobookAssetsStorage =
        runCatching {
            json.decodeFromString(
                AudiobookAssetsStorage.serializer(),
                File(bookDir, ASSETS_FILE).readText(),
            )
        }.getOrDefault(AudiobookAssetsStorage())

    private fun saveAssetsStorage(
        bookDir: File,
        assets: AudiobookAssetsStorage,
    ) {
        File(
            bookDir,
            ASSETS_FILE,
        ).writeText(json.encodeToString(AudiobookAssetsStorage.serializer(), assets))
    }

    private fun loadSubtitleData(bookDir: File): AudiobookSubtitleData? =
        runCatching {
            json.decodeFromString(
                AudiobookSubtitleData.serializer(),
                File(bookDir, SUBTITLE_DATA_FILE).readText(),
            )
        }.getOrNull()

    private fun saveSubtitleData(
        bookDir: File,
        data: AudiobookSubtitleData,
    ) {
        File(
            bookDir,
            SUBTITLE_DATA_FILE,
        ).writeText(json.encodeToString(AudiobookSubtitleData.serializer(), data))
    }

    private fun loadMatchData(bookDir: File): SasayakiMatchData? =
        runCatching {
            json.decodeFromString(
                SasayakiMatchData.serializer(),
                File(bookDir, MATCH_DATA_FILE).readText(),
            )
        }.getOrNull()

    private fun saveMatchData(
        bookDir: File,
        data: SasayakiMatchData,
    ) {
        File(bookDir, MATCH_DATA_FILE).writeText(
            json.encodeToString(
                SasayakiMatchData.serializer(),
                data,
            ),
        )
    }

    private fun loadMatchChapters(bookDir: File): List<MatchChapter> {
        val readerInfoFile = File(bookDir, BOOKINFO_FILE)
        require(readerInfoFile.exists()) { "Reader 信息不存在，请先打开一次图书" }
        val readerInfo =
            json.decodeFromString(ReaderBookInfoStorage.serializer(), readerInfoFile.readText())
        return readerInfo.chapterInfo.entries
            .sortedBy { it.value.spineIndex ?: Int.MAX_VALUE }
            .mapNotNull { (href, info) ->
                val source =
                    info.sourceUrl
                        ?.let { runCatching { File(URI(it)) }.getOrNull() }
                        ?: File(File(bookDir, EXTRACTED_EPUB_DIR), href)
                val text =
                    runCatching { source.readText() }
                        .getOrDefault("")
                        .filteredReaderText()
                if (text.isBlank()) null else MatchChapter(info.spineIndex ?: 0, text)
            }.sortedBy { it.index }
    }

    private fun buildMatchData(
        subtitleData: AudiobookSubtitleData,
        chapters: List<MatchChapter>,
        searchWindow: Int,
    ): SasayakiMatchData {
        val matches = mutableListOf<SasayakiMatch>()
        val effectiveCues = subtitleData.cues.map { cue -> cue to cue.text.filteredReaderText() }
        val firstCue =
            effectiveCues
                .filter { (_, text) -> text.isUsefulMatchCue() }
                .take(15)
                .firstOrNull()
        var chapterCursor = 0
        var offsetCursor = 0
        if (firstCue != null) {
            val firstText = firstCue.second
            val initial =
                chapters.firstNotNullOfOrNull { chapter ->
                    val found = chapter.text.indexOf(firstText)
                    if (found >= 0) chapter to found else null
                }
            if (initial != null) {
                chapterCursor = chapters.indexOf(initial.first)
                offsetCursor = initial.second
            }
        }

        effectiveCues.forEach { (cue, cueText) ->
            if (!cueText.isUsefulMatchCue()) return@forEach
            val found =
                findCue(chapters, chapterCursor, offsetCursor, cueText, searchWindow)
                    ?: return@forEach
            matches +=
                SasayakiMatch(
                    id = cue.id,
                    startTimeMs = cue.startTimeMs,
                    endTimeMs = cue.endTimeMs,
                    text = cue.text,
                    chapterIndex = found.chapterIndex,
                    start = found.start,
                    length = cueText.length,
                )
            chapterCursor = found.chapterListIndex
            offsetCursor = found.start + cueText.length
        }

        return SasayakiMatchData(
            matches = matches,
            unmatched = subtitleData.cues.size - matches.size,
            searchWindow = searchWindow,
            matchedAt = System.currentTimeMillis(),
        )
    }

    private fun findCue(
        chapters: List<MatchChapter>,
        chapterListIndex: Int,
        offset: Int,
        cueText: String,
        searchWindow: Int,
    ): MatchLocation? {
        var currentIndex = chapterListIndex.coerceIn(chapters.indices)
        var currentOffset = offset.coerceAtLeast(0)
        while (currentIndex < chapters.size) {
            val chapter = chapters[currentIndex]
            val from = currentOffset.coerceIn(0, chapter.text.length)
            val to = (from + searchWindow + cueText.length).coerceAtMost(chapter.text.length)
            val found =
                chapter.text.indexOf(cueText, startIndex = from).takeIf { it >= 0 && it < to }
            if (found != null) {
                return MatchLocation(
                    chapterListIndex = currentIndex,
                    chapterIndex = chapter.index,
                    start = found,
                )
            }
            currentIndex += 1
            currentOffset = 0
        }
        return null
    }

    private fun copyUriToFile(
        uri: Uri,
        target: File,
    ) {
        target.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "无法读取文件" }
            target.outputStream().use { output -> input.copyTo(output) }
        }
    }

    private fun readUriBytes(uri: Uri): ByteArray =
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "无法读取字幕文件" }.readBytes()
        }

    private fun displayName(uri: Uri): String? =
        runCatching {
            context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (!cursor.moveToFirst()) return@use null
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                }
        }.getOrNull()

    private fun sizeBytes(uri: Uri): Long =
        runCatching {
            context.contentResolver
                .query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (!cursor.moveToFirst()) return@use null
                    val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (index >= 0) cursor.getLong(index) else null
                }
        }.getOrNull() ?: 0L

    private fun mimeType(uri: Uri): String? = context.contentResolver.getType(uri)

    private fun takePersistableReadPermission(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }

    private fun deleteLocalAsset(
        bookDir: File,
        asset: AudiobookAssetInfo?,
    ) {
        val relativePath = asset?.localRelativePath ?: return
        File(bookDir, relativePath).delete()
    }
}

private fun audioFormat(
    displayName: String,
    mimeType: String?,
): AudiobookAudioFormat? {
    val extension = displayName.substringAfterLast('.', "").lowercase()
    return when {
        extension == "mp3" || mimeType == "audio/mpeg" -> AudiobookAudioFormat.Mp3
        extension == "m4b" || mimeType == "audio/x-m4b" -> AudiobookAudioFormat.M4b
        else -> null
    }
}

private fun subtitleFormat(
    displayName: String,
    mimeType: String?,
): AudiobookSubtitleFormat? {
    val extension = displayName.substringAfterLast('.', "").lowercase()
    return when {
        extension == "srt" || mimeType == "application/x-subrip" -> AudiobookSubtitleFormat.Srt
        else -> null
    }
}

private fun decodeText(bytes: ByteArray): String {
    require(bytes.isNotEmpty()) { "字幕文件为空" }
    val utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    val data =
        if (bytes.size >= 3 && bytes.take(3).toByteArray().contentEquals(utf8Bom)) {
            bytes.copyOfRange(3, bytes.size)
        } else {
            bytes
        }
    return data
        .toString(Charsets.UTF_8)
        .ifBlank { data.toString(Charset.forName("UTF-8")) }
}

private fun parseSrt(rawText: String): List<AudiobookCue> {
    val normalized = rawText.replace("\r\n", "\n").replace('\r', '\n').trim()
    require(normalized.isNotBlank()) { "字幕文件为空" }
    val blocks = normalized.split(Regex("\\n{2,}")).filter { it.isNotBlank() }
    val cues =
        blocks.mapIndexed { blockIndex, block ->
            val lines = block.lines().filter { it.isNotBlank() }
            require(lines.size >= 2) { "SRT 格式无效" }
            val timeLineIndex = lines.indexOfFirst { "-->" in it }
            require(timeLineIndex >= 0) { "SRT 缺少时间轴" }
            val sequence = lines.firstOrNull()?.trim()?.toIntOrNull() ?: (blockIndex + 1)
            val parts = lines[timeLineIndex].split("-->")
            require(parts.size == 2) { "SRT 时间轴无效" }
            val start = parseSrtTime(parts[0].trim())
            val end = parseSrtTime(parts[1].trim().substringBefore(' '))
            require(end > start) { "SRT 时间范围无效" }
            val text = lines.drop(timeLineIndex + 1).joinToString("\n").trim()
            require(text.isNotBlank()) { "SRT 字幕文本为空" }
            AudiobookCue(
                id = UUID.nameUUIDFromBytes("$sequence:$start:$end:$text".toByteArray()).toString(),
                startTimeMs = start,
                endTimeMs = end,
                text = text,
                sequence = sequence,
            )
        }
    require(cues.isNotEmpty()) { "SRT 没有可用字幕" }
    return cues
}

private fun parseSrtTime(value: String): Long {
    val match =
        Regex("""(\d{2}):(\d{2}):(\d{2}),(\d{3})""").matchEntire(value)
            ?: throw IllegalArgumentException("SRT 时间格式无效")
    val (hours, minutes, seconds, millis) = match.destructured
    return hours.toLong() * 3_600_000L +
        minutes.toLong() * 60_000L +
        seconds.toLong() * 1_000L +
        millis.toLong()
}

private fun String.isUsefulMatchCue(): Boolean = isNotBlank() && length >= MIN_CUE_LENGTH && !startsWith("＊")

@Serializable
private data class BookMetadataStorage(
    val id: String,
)

@Serializable
private data class ReaderBookInfoStorage(
    val characterCount: Int,
    val chapterInfo: Map<String, ReaderChapterInfoStorage>,
)

@Serializable
private data class ReaderChapterInfoStorage(
    val spineIndex: Int? = null,
    val currentTotal: Int,
    val characterCount: Int,
    val id: String? = null,
    val title: String? = null,
    val sourceUrl: String? = null,
)

@Serializable
private data class AudiobookAssetsStorage(
    val version: Int = 1,
    val audio: AudiobookAssetInfo? = null,
    val subtitle: AudiobookAssetInfo? = null,
)

private data class MatchChapter(
    val index: Int,
    val text: String,
)

private data class MatchLocation(
    val chapterListIndex: Int,
    val chapterIndex: Int,
    val start: Int,
)

private const val BOOKS_DIR_NAME = "Books"
private const val METADATA_FILE = "metadata.json"
private const val BOOKINFO_FILE = "bookinfo.json"
private const val EXTRACTED_EPUB_DIR = "content"
private const val AUDIOBOOK_DIR = "audiobook"
private const val ASSETS_FILE = "audiobook_assets.json"
private const val SUBTITLE_DATA_FILE = "audiobook_subtitle.json"
private const val MATCH_DATA_FILE = "sasayaki_match.json"
private const val MIN_SEARCH_WINDOW = 50
private const val MAX_SEARCH_WINDOW = 350
private const val MIN_CUE_LENGTH = 2
