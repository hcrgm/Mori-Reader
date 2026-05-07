package app.mori.reader.data.book

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import app.mori.reader.data.filteredReaderCharacterCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import java.net.URLDecoder
import java.util.UUID
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

internal class AndroidBookRepository(
    private val context: Context,
) : BookRepository {
    private val booksRoot = File(context.filesDir, BOOKS_DIR_NAME)
    private val categoriesFile = File(booksRoot, CATEGORIES_FILE)
    private val bookCategoryMapFile = File(booksRoot, BOOK_CATEGORY_MAP_FILE)
    private val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            encodeDefaults = true
        }

    private val _catalog = MutableStateFlow<BookCatalog?>(null)
    override val catalog: StateFlow<BookCatalog?> = _catalog.asStateFlow()

    override suspend fun loadCatalog(): BookCatalog =
        withContext(Dispatchers.IO) {
            booksRoot.mkdirs()
            buildCatalog().also(::publishCatalog)
        }

    override suspend fun loadReaderBook(bookId: String): ReaderBook =
        withContext(Dispatchers.IO) {
            booksRoot.mkdirs()
            val bookDir =
                findBookDirectory(bookId)
                    ?: throw IllegalArgumentException("图书不存在")
            val metadata =
                loadBookMetadataStorage(bookDir)
                    ?: throw IllegalArgumentException("图书不存在")

            val now = System.currentTimeMillis()
            saveBookMetadataStorage(bookDir, metadata.copy(lastOpenedAt = now))
            publishCatalog(buildCatalog())

            parseReaderBook(
                sourceFile = null,
                bookDir = bookDir,
                book =
                    computeBookInfo(
                        metadata = metadata.copy(lastOpenedAt = now),
                        categoryIds = loadBookCategoryMap()[bookId].orEmpty(),
                        readerInfo = loadReaderBookInfoStorage(bookDir),
                        bookmark = loadBookmark(bookDir),
                    ),
            )
        }

    override suspend fun importBooks(uriStrings: List<String>): BookCatalog =
        withContext(Dispatchers.IO) {
            if (uriStrings.isEmpty()) return@withContext loadCatalog()
            booksRoot.mkdirs()
            val categories = loadCategories()
            val readingCategoryId = categories.firstOrNull { it.name == DEFAULT_READING_CATEGORY_NAME }?.id
            val categoryMap = loadBookCategoryMap().toMutableMap()
            val failures = mutableListOf<String>()

            uriStrings.forEach { uriString ->
                val uri = Uri.parse(uriString)
                val bookId = UUID.randomUUID().toString()
                val tempBookDir = File(booksRoot, UUID.randomUUID().toString())
                val sourceFile = File(tempBookDir, SOURCE_FILE_NAME)
                try {
                    tempBookDir.mkdirs()
                    copyUriToFile(uri, sourceFile)
                    val fallbackTitle = displayName(uri)?.removeSuffix(".epub") ?: "未命名图书"
                    val importedAt = System.currentTimeMillis()
                    val metadata = parseEpub(sourceFile, tempBookDir, fallbackTitle)
                    val safeTitle = sanitizeFileName(metadata.title.ifBlank { fallbackTitle })
                    val bookDir = File(booksRoot, safeTitle)
                    if (bookDir.exists()) {
                        tempBookDir.deleteRecursively()
                        return@forEach
                    }
                    if (!tempBookDir.renameTo(bookDir)) {
                        throw IllegalStateException("无法创建图书目录")
                    }
                    val finalSourceFile = File(bookDir, SOURCE_FILE_NAME)
                    val finalCoverPath =
                        metadata.coverPath
                            ?.let { coverPath ->
                                val coverFile = File(coverPath)
                                File(bookDir, coverFile.name).absolutePath
                            }?.takeIf { File(it).isFile }
                    val bookMetadata =
                        BookMetadataStorage(
                            id = bookId,
                            title = metadata.title.ifBlank { fallbackTitle },
                            author = metadata.author?.takeIf { it.isNotBlank() },
                            coverPath = finalCoverPath,
                            importedAt = importedAt,
                            lastOpenedAt = null,
                        )
                    saveBookMetadataStorage(bookDir, bookMetadata)
                    categoryMap[bookId] = listOfNotNull(readingCategoryId)
                    val processing = processBook(finalSourceFile, bookDir)
                    saveReaderBookInfoStorage(bookDir, processing.info)
                    saveReaderTocStorage(bookDir, processing.tocRows)
                    finalSourceFile.delete()
                } catch (_: Throwable) {
                    tempBookDir.deleteRecursively()
                    failures += displayName(uri) ?: uri.lastPathSegment ?: uriString
                }
            }

            if (failures.isNotEmpty() && buildCatalog().books.isEmpty()) {
                throw IllegalStateException("导入失败，请确认选择的是 EPUB 文件。")
            }

            saveBookCategoryMap(categoryMap)
            buildCatalog().also(::publishCatalog)
        }

    override suspend fun saveReaderProgress(
        bookId: String,
        chapterIndex: Int,
        chapterProgress: Double,
    ): BookCatalog =
        withContext(Dispatchers.IO) {
            val bookDir = findBookDirectory(bookId) ?: return@withContext buildCatalog().also(::publishCatalog)
            val metadata = loadBookMetadataStorage(bookDir) ?: return@withContext buildCatalog().also(::publishCatalog)
            val readerBook =
                parseReaderBook(
                    sourceFile = null,
                    bookDir = bookDir,
                    book =
                        computeBookInfo(
                            metadata,
                            loadBookCategoryMap()[bookId].orEmpty(),
                            loadReaderBookInfoStorage(bookDir),
                            loadBookmark(bookDir),
                        ),
                )
            if (readerBook.chapters.isEmpty()) return@withContext buildCatalog().also(::publishCatalog)

            val clampedIndex = chapterIndex.coerceIn(readerBook.chapters.indices)
            val clampedProgress = chapterProgress.coerceIn(0.0, 1.0)
            val chapter = readerBook.chapters[clampedIndex]
            val characterCount = chapter.characterStart + (chapter.characterCount * clampedProgress).toInt()
            val now = System.currentTimeMillis()
            saveBookmark(
                bookDir,
                ReaderBookmark(
                    chapterIndex = clampedIndex,
                    chapterProgress = clampedProgress,
                    characterCount = characterCount,
                    lastModifiedAt = now,
                ),
            )
            saveBookMetadataStorage(bookDir, metadata.copy(lastOpenedAt = now))

            buildCatalog().also(::publishCatalog)
        }

    override suspend fun createCategory(name: String): BookCatalog =
        withContext(Dispatchers.IO) {
            val trimmed = name.trim()
            require(trimmed.isNotBlank()) { "分类名称不能为空" }
            val categories = loadCategories()
            if (categories.any { it.name == trimmed }) return@withContext buildCatalog().also(::publishCatalog)
            saveCategories(
                categories +
                    BookCategory(
                        id = UUID.randomUUID().toString(),
                        name = trimmed,
                        createdAt = System.currentTimeMillis(),
                        order = (categories.maxOfOrNull(BookCategory::order) ?: -1) + 1,
                    ),
            )
            buildCatalog().also(::publishCatalog)
        }

    override suspend fun renameCategory(
        id: String,
        name: String,
    ): BookCatalog =
        withContext(Dispatchers.IO) {
            val trimmed = name.trim()
            require(trimmed.isNotBlank()) { "分类名称不能为空" }
            saveCategories(loadCategories().map { if (it.id == id) it.copy(name = trimmed) else it })
            buildCatalog().also(::publishCatalog)
        }

    override suspend fun reorderCategories(categoryIds: List<String>): BookCatalog =
        withContext(Dispatchers.IO) {
            val categories = loadCategories()
            val categoriesById = categories.associateBy { it.id }
            val currentIds = categories.map { it.id }
            val requestedIds = categoryIds.distinct()
            require(requestedIds.size == currentIds.size && requestedIds.toSet() == currentIds.toSet()) {
                "分类排序数据无效"
            }
            saveCategories(
                requestedIds.mapIndexedNotNull { index, categoryId ->
                    categoriesById[categoryId]?.copy(order = index)
                },
            )
            buildCatalog().also(::publishCatalog)
        }

    override suspend fun deleteCategory(id: String): BookCatalog =
        withContext(Dispatchers.IO) {
            saveCategories(loadCategories().filterNot { it.id == id })
            saveBookCategoryMap(
                loadBookCategoryMap().mapValues { (_, categoryIds) ->
                    categoryIds.filterNot { it == id }
                },
            )
            buildCatalog().also(::publishCatalog)
        }

    override suspend fun updateBookCategories(
        bookId: String,
        categoryIds: List<String>,
    ): BookCatalog =
        withContext(Dispatchers.IO) {
            val validCategoryIds = loadCategories().map(BookCategory::id).toSet()
            val sanitizedCategoryIds = categoryIds.distinct().filter { it in validCategoryIds }
            val categoryMap = loadBookCategoryMap().toMutableMap()
            categoryMap[bookId] = sanitizedCategoryIds
            saveBookCategoryMap(categoryMap)
            buildCatalog().also(::publishCatalog)
        }

    override suspend fun deleteBook(bookId: String): BookCatalog =
        withContext(Dispatchers.IO) {
            findBookDirectory(bookId)?.deleteRecursively()
            val categoryMap = loadBookCategoryMap().toMutableMap()
            categoryMap.remove(bookId)
            saveBookCategoryMap(categoryMap)
            buildCatalog().also(::publishCatalog)
        }

    private fun publishCatalog(catalog: BookCatalog) {
        _catalog.value = catalog
    }

    private fun buildCatalog(): BookCatalog {
        val categories = loadCategories()
        val validCategoryIds = categories.mapTo(mutableSetOf()) { it.id }
        val categoryMap = loadBookCategoryMap()
        val books =
            listBookDirectories()
                .mapNotNull { bookDir ->
                    val metadata = loadBookMetadataStorage(bookDir) ?: return@mapNotNull null
                    val categoryIds = categoryMap[metadata.id].orEmpty().filter { it in validCategoryIds }.distinct()
                    computeBookInfo(
                        metadata = metadata,
                        categoryIds = categoryIds,
                        readerInfo = loadReaderBookInfoStorage(bookDir),
                        bookmark = loadBookmark(bookDir),
                    )
                }.sortedByDescending { it.importedAt }
        return BookCatalog(books = books, categories = categories.normalizedCategories())
    }

    private fun ensureCategoriesInitialized() {
        if (!categoriesFile.isFile) {
            saveCategories(listOf(defaultReadingCategory()))
        }
    }

    private fun loadCategories(): List<BookCategory> {
        ensureCategoriesInitialized()
        return runCatching {
            json.decodeFromString(BookCategoriesStorage.serializer(), categoriesFile.readText()).categories
        }.getOrDefault(emptyList()).normalizedCategories()
    }

    private fun loadBookCategoryMap(): Map<String, List<String>> =
        runCatching {
            json
                .decodeFromString(BookCategoryMapStorage.serializer(), bookCategoryMapFile.readText())
                .books
                .mapValues { (_, categoryIds) -> categoryIds.distinct() }
        }.getOrDefault(emptyMap())

    private fun saveBookCategoryMap(bookCategoryMap: Map<String, List<String>>) {
        booksRoot.mkdirs()
        val normalized =
            bookCategoryMap
                .mapValues { (_, categoryIds) -> categoryIds.distinct() }
                .filterValues { it.isNotEmpty() }
        bookCategoryMapFile.writeText(
            json.encodeToString(
                BookCategoryMapStorage.serializer(),
                BookCategoryMapStorage(books = normalized),
            ),
        )
    }

    private fun saveCategories(categories: List<BookCategory>) {
        booksRoot.mkdirs()
        val normalized = categories.normalizedCategories()
        categoriesFile.writeText(
            json.encodeToString(
                BookCategoriesStorage.serializer(),
                BookCategoriesStorage(categories = normalized),
            ),
        )
    }

    private fun listBookDirectories(): List<File> =
        booksRoot
            .listFiles()
            ?.filter { it.isDirectory }
            ?.sortedBy { it.name }
            .orEmpty()

    private fun findBookDirectory(bookId: String): File? =
        listBookDirectories().firstOrNull { directory ->
            loadBookMetadataStorage(directory)?.id == bookId
        }

    private fun copyUriToFile(
        uri: Uri,
        target: File,
    ) {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "无法读取 EPUB 文件" }
            target.outputStream().use { output -> input.copyTo(output) }
        }
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

    private fun parseEpub(
        sourceFile: File,
        bookDir: File,
        fallbackTitle: String,
    ): EpubMetadata =
        ZipFile(sourceFile).use { zip ->
            val container =
                zip.readTextEntry("META-INF/container.xml")
                    ?: return@use EpubMetadata(title = fallbackTitle)
            val rootPath =
                parseContainerRootPath(container)
                    ?: return@use EpubMetadata(title = fallbackTitle)
            val opf =
                zip.readTextEntry(rootPath)
                    ?: return@use EpubMetadata(title = fallbackTitle)
            val document =
                runCatching { parseXml(opf) }
                    .getOrNull()
                    ?: return@use EpubMetadata(title = fallbackTitle)
            val title =
                document
                    .elementsByLocalName("title")
                    .firstOrNull { it.textContent.isNotBlank() }
                    ?.textContent
                    ?.trim()
                    ?: fallbackTitle
            val author =
                document
                    .elementsByLocalName("creator")
                    .firstOrNull { it.textContent.isNotBlank() }
                    ?.textContent
                    ?.trim()
            val coverPath =
                runCatching { extractCover(zip, document.documentElement, rootPath, bookDir) }
                    .getOrNull()
            EpubMetadata(title = title, author = author, coverPath = coverPath)
        }

    private fun processBook(
        sourceFile: File,
        bookDir: File,
    ): ProcessedReaderBook =
        ZipFile(sourceFile).use { zip ->
            val container =
                zip.readTextEntry("META-INF/container.xml")
                    ?: throw IllegalStateException("EPUB 缺少 container.xml")
            val rootPath =
                parseContainerRootPath(container)
                    ?: throw IllegalStateException("EPUB 缺少 OPF")
            val opf =
                zip.readTextEntry(rootPath)
                    ?: throw IllegalStateException("无法读取 EPUB OPF")
            val document = parseXml(opf)
            val packageElement = document.documentElement
            val opfDir = rootPath.substringBeforeLast('/', "")
            val manifestItems =
                packageElement
                    .elementsByLocalName("item")
                    .map { item ->
                        ManifestItem(
                            id = item.getAttribute("id"),
                            href = item.getAttribute("href"),
                            mediaType = item.getAttribute("media-type"),
                            properties = item.getAttribute("properties"),
                        )
                    }.filter { it.id.isNotBlank() && it.href.isNotBlank() }
                    .associateBy { it.id }
            val spineElement =
                packageElement.elementsByLocalName("spine").firstOrNull()
                    ?: throw IllegalStateException("EPUB 缺少 spine")
            val spineItems =
                spineElement
                    .elementsByLocalName("itemref")
                    .mapNotNull { itemref ->
                        val idref = itemref.getAttribute("idref")
                        if (idref.isBlank() || itemref.getAttribute("linear") == "no") null else manifestItems[idref]
                    }.filter {
                        it.mediaType.contains("html", ignoreCase = true) ||
                            it.href.endsWith(".xhtml", true) ||
                            it.href.endsWith(".html", true)
                    }
            require(spineItems.isNotEmpty()) { "EPUB 没有可阅读章节" }

            val tocRows = parseTableOfContents(zip, manifestItems, spineElement, opfDir, spineItems)
            val tocTitleByChapter =
                tocRows
                    .groupBy { it.chapterIndex }
                    .mapValues { (_, rows) -> rows.minByOrNull { it.indentLevel }?.label.orEmpty() }

            ensureExtracted(zip, bookDir)

            var characterStart = 0
            val chapters =
                spineItems.mapIndexed { index, item ->
                    val href = resolveZipPath(opfDir, item.href)
                    val html = zip.readTextEntry(href).orEmpty()
                    val count = html.filteredCharacterCount()
                    val title =
                        tocTitleByChapter[index]
                            ?.takeIf { it.isNotBlank() }
                            ?: "第 ${index + 1} 章"
                    val chapter =
                        ReaderChapter(
                            id = item.id.ifBlank { href },
                            title = title,
                            href = href,
                            sourceUrl = File(File(bookDir, EXTRACTED_EPUB_DIR), href).toURI().toString(),
                            index = index,
                            characterStart = characterStart,
                            characterCount = count,
                        )
                    characterStart += count
                    chapter
                }

            ProcessedReaderBook(
                chapters = chapters,
                tocRows = tocRows,
                info =
                    ReaderBookInfoStorage(
                        characterCount = characterStart,
                        chapterInfo =
                            chapters.associate { chapter ->
                                chapter.href to
                                    ReaderChapterInfoStorage(
                                        spineIndex = chapter.index,
                                        currentTotal = chapter.characterStart,
                                        characterCount = chapter.characterCount,
                                        id = chapter.id,
                                        title = chapter.title,
                                        sourceUrl = chapter.sourceUrl,
                                    )
                            },
                    ),
            )
        }

    private fun parseReaderBook(
        sourceFile: File?,
        bookDir: File,
        book: BookInfo,
    ): ReaderBook {
        val storedInfo = loadReaderBookInfoStorage(bookDir)
        val storedToc = loadReaderTocStorage(bookDir)
        val processed =
            if (storedInfo == null) {
                requireNotNull(sourceFile) { "EPUB 源文件不存在" }
                processBook(sourceFile, bookDir).also {
                    saveReaderBookInfoStorage(bookDir, it.info)
                    saveReaderTocStorage(bookDir, it.tocRows)
                }
            } else {
                null
            }
        val readerInfo = storedInfo ?: processed!!.info
        val tableOfContents = storedToc ?: processed?.tocRows.orEmpty()
        val chapters =
            readerInfo.chapterInfo.entries
                .sortedBy { it.value.spineIndex ?: Int.MAX_VALUE }
                .map { (href, info) ->
                    ReaderChapter(
                        id = info.id ?: href,
                        title = info.title ?: "第 ${(info.spineIndex ?: 0) + 1} 章",
                        href = href,
                        sourceUrl = info.sourceUrl ?: File(File(bookDir, EXTRACTED_EPUB_DIR), href).toURI().toString(),
                        index = info.spineIndex ?: 0,
                        characterStart = info.currentTotal,
                        characterCount = info.characterCount,
                    )
                }
        val bookmark =
            loadBookmark(bookDir)
                ?.let {
                    it.copy(
                        chapterIndex = it.chapterIndex.coerceIn(chapters.indices),
                        chapterProgress = it.chapterProgress.coerceIn(0.0, 1.0),
                    )
                }
                ?: ReaderBookmark()
        return ReaderBook(
            info = book,
            chapters = chapters,
            tableOfContents =
                tableOfContents.ifEmpty {
                    chapters.map {
                        ReaderTocItem(
                            label = it.title,
                            chapterIndex = it.index,
                            characterCount = it.characterStart,
                        )
                    }
                },
            totalCharacterCount = readerInfo.characterCount,
            bookmark = bookmark,
        )
    }

    private fun computeBookInfo(
        metadata: BookMetadataStorage,
        categoryIds: List<String>,
        readerInfo: ReaderBookInfoStorage?,
        bookmark: ReaderBookmark?,
    ): BookInfo {
        val chapterIndex = bookmark?.chapterIndex
        val currentChapterName =
            chapterIndex?.let { index ->
                readerInfo
                    ?.chapterInfo
                    ?.values
                    ?.firstOrNull { it.spineIndex == index }
                    ?.title
            }
        val progressPercent =
            if (readerInfo != null && bookmark != null && readerInfo.characterCount > 0) {
                ((bookmark.characterCount.toDouble() / readerInfo.characterCount.toDouble()) * 100.0)
                    .toInt()
                    .coerceIn(0, 100)
            } else {
                0
            }
        return BookInfo(
            id = metadata.id,
            title = metadata.title,
            author = metadata.author,
            coverPath = metadata.coverPath,
            categoryIds = categoryIds,
            progressPercent = progressPercent,
            currentChapterName = currentChapterName,
            importedAt = metadata.importedAt,
            lastOpenedAt = metadata.lastOpenedAt,
        )
    }

    private fun loadBookMetadataStorage(bookDir: File): BookMetadataStorage? =
        runCatching {
            json.decodeFromString(BookMetadataStorage.serializer(), File(bookDir, METADATA_FILE).readText())
        }.getOrNull()

    private fun saveBookMetadataStorage(
        bookDir: File,
        metadata: BookMetadataStorage,
    ) {
        File(bookDir, METADATA_FILE)
            .writeText(json.encodeToString(BookMetadataStorage.serializer(), metadata))
    }

    private fun loadReaderBookInfoStorage(bookDir: File): ReaderBookInfoStorage? =
        runCatching {
            json.decodeFromString(ReaderBookInfoStorage.serializer(), File(bookDir, BOOKINFO_FILE).readText())
        }.getOrNull()

    private fun saveReaderBookInfoStorage(
        bookDir: File,
        info: ReaderBookInfoStorage,
    ) {
        File(bookDir, BOOKINFO_FILE)
            .writeText(json.encodeToString(ReaderBookInfoStorage.serializer(), info))
    }

    private fun loadReaderTocStorage(bookDir: File): List<ReaderTocItem>? =
        runCatching {
            json.decodeFromString(ListSerializer(ReaderTocItem.serializer()), File(bookDir, TOC_FILE).readText())
        }.getOrNull()

    private fun saveReaderTocStorage(
        bookDir: File,
        toc: List<ReaderTocItem>,
    ) {
        File(bookDir, TOC_FILE)
            .writeText(json.encodeToString(ListSerializer(ReaderTocItem.serializer()), toc))
    }

    private fun ensureExtracted(
        zip: ZipFile,
        bookDir: File,
    ) {
        val targetRoot = File(bookDir, EXTRACTED_EPUB_DIR)
        val marker = File(targetRoot, EXTRACTION_MARKER_FILE)
        if (marker.isFile) return
        targetRoot.mkdirs()
        val canonicalRoot = targetRoot.canonicalFile
        zip
            .entries()
            .asSequence()
            .filterNot { it.isDirectory }
            .forEach { entry ->
                val target = File(targetRoot, entry.name).canonicalFile
                if (!target.path.startsWith(canonicalRoot.path + File.separator)) return@forEach
                target.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }
        marker.writeText("ok")
    }

    private fun loadBookmark(bookDir: File): ReaderBookmark? =
        runCatching {
            json.decodeFromString(ReaderBookmark.serializer(), File(bookDir, BOOKMARK_FILE).readText())
        }.getOrNull()

    private fun saveBookmark(
        bookDir: File,
        bookmark: ReaderBookmark,
    ) {
        File(bookDir, BOOKMARK_FILE).writeText(json.encodeToString(ReaderBookmark.serializer(), bookmark))
    }

    private fun parseContainerRootPath(xml: String): String? {
        val document = runCatching { parseXml(xml) }.getOrNull() ?: return null
        return document
            .elementsByLocalName("rootfile")
            .firstOrNull()
            ?.getAttribute("full-path")
            ?.takeIf { it.isNotBlank() }
    }

    private fun extractCover(
        zip: ZipFile,
        packageElement: Element,
        opfPath: String,
        bookDir: File,
    ): String? {
        val manifestItems =
            packageElement
                .elementsByLocalName("item")
                .map { item ->
                    ManifestItem(
                        id = item.getAttribute("id"),
                        href = item.getAttribute("href"),
                        mediaType = item.getAttribute("media-type"),
                        properties = item.getAttribute("properties"),
                    )
                }.filter { it.href.isNotBlank() }

        val coverId =
            packageElement
                .elementsByLocalName("meta")
                .firstOrNull { it.getAttribute("name") == "cover" }
                ?.getAttribute("content")
        val coverItem =
            manifestItems.firstOrNull { "cover-image" in it.properties.split(' ') }
                ?: manifestItems.firstOrNull { it.id == coverId }
                ?: manifestItems.firstOrNull {
                    it.mediaType.startsWith("image/") &&
                        (it.id.contains("cover", ignoreCase = true) || it.href.contains("cover", ignoreCase = true))
                }
                ?: return null

        val entryName = resolveZipPath(opfPath.substringBeforeLast('/', ""), coverItem.href)
        val entry = zip.getEntry(entryName) ?: return null
        val extension =
            coverItem.href
                .substringAfterLast('.', "")
                .takeIf { it.length in 2..5 }
                ?: coverItem.mediaType.substringAfterLast('/', "jpg")
        val coverFile = File(bookDir, "cover.$extension")
        zip.getInputStream(entry).use { input ->
            coverFile.outputStream().use { output -> input.copyTo(output) }
        }
        return coverFile.absolutePath
    }

    private fun parseTableOfContents(
        zip: ZipFile,
        manifestItems: Map<String, ManifestItem>,
        spineElement: Element,
        opfDir: String,
        spineItems: List<ManifestItem>,
    ): List<ReaderTocItem> {
        val navItem = manifestItems.values.firstOrNull { "nav" in it.properties.split(' ') }
        val navRows =
            if (navItem != null) {
                val navPath = resolveZipPath(opfDir, navItem.href)
                zip
                    .readTextEntry(navPath)
                    ?.let { runCatching { parseNavToc(parseXml(it), opfDir, spineItems) }.getOrDefault(emptyList()) }
                    .orEmpty()
            } else {
                emptyList()
            }
        if (navRows.isNotEmpty()) return navRows

        val ncxId = spineElement.getAttribute("toc")
        val ncxItem = manifestItems[ncxId]
        return if (ncxItem != null) {
            val ncxPath = resolveZipPath(opfDir, ncxItem.href)
            zip
                .readTextEntry(ncxPath)
                ?.let { runCatching { parseNcxToc(parseXml(it), opfDir, spineItems) }.getOrDefault(emptyList()) }
                .orEmpty()
        } else {
            emptyList()
        }
    }

    private fun parseNavToc(
        document: Document,
        opfDir: String,
        spineItems: List<ManifestItem>,
    ): List<ReaderTocItem> {
        val nav =
            document
                .elementsByLocalName("nav")
                .firstOrNull { nav ->
                    nav.getAttribute("epub:type").split(' ').contains("toc") ||
                        nav.getAttribute("type").split(' ').contains("toc")
                }
                ?: document.documentElement
        val rows = mutableListOf<ReaderTocItem>()
        nav.directChildElementsByLocalName("ol").forEach { ol ->
            parseNavList(ol, opfDir, spineItems, rows, indentLevel = 0)
        }
        if (rows.isEmpty()) {
            nav.elementsByLocalName("a").forEach { anchor ->
                appendTocRow(rows, anchor.textContent.trim(), anchor.getAttribute("href"), opfDir, spineItems, 0)
            }
        }
        return rows.distinctBy { "${it.chapterIndex}:${it.fragment}:${it.label}" }
    }

    private fun parseNavList(
        parent: Element,
        opfDir: String,
        spineItems: List<ManifestItem>,
        rows: MutableList<ReaderTocItem>,
        indentLevel: Int,
    ) {
        parent.directChildElementsByLocalName("li").forEach { li ->
            val anchor = li.directChildElementsByLocalName("a").firstOrNull()
            val span = li.directChildElementsByLocalName("span").firstOrNull()
            val label = (anchor ?: span)?.textContent?.trim().orEmpty()
            val href = anchor?.getAttribute("href").orEmpty()
            appendTocRow(rows, label, href, opfDir, spineItems, indentLevel)
            li.directChildElementsByLocalName("ol").forEach { nested ->
                parseNavList(nested, opfDir, spineItems, rows, indentLevel + 1)
            }
        }
    }

    private fun parseNcxToc(
        document: Document,
        opfDir: String,
        spineItems: List<ManifestItem>,
    ): List<ReaderTocItem> {
        val rows = mutableListOf<ReaderTocItem>()
        document.documentElement.directChildElementsByLocalName("navMap").forEach { navMap ->
            parseNcxPoints(navMap, opfDir, spineItems, rows, indentLevel = 0)
        }
        return rows
    }

    private fun parseNcxPoints(
        parent: Element,
        opfDir: String,
        spineItems: List<ManifestItem>,
        rows: MutableList<ReaderTocItem>,
        indentLevel: Int,
    ) {
        parent.directChildElementsByLocalName("navPoint").forEach { point ->
            val label =
                point
                    .elementsByLocalName("text")
                    .firstOrNull()
                    ?.textContent
                    ?.trim()
                    .orEmpty()
            val src =
                point
                    .elementsByLocalName("content")
                    .firstOrNull()
                    ?.getAttribute("src")
                    .orEmpty()
            appendTocRow(rows, label, src, opfDir, spineItems, indentLevel)
            parseNcxPoints(point, opfDir, spineItems, rows, indentLevel + 1)
        }
    }

    private fun appendTocRow(
        rows: MutableList<ReaderTocItem>,
        label: String,
        href: String,
        opfDir: String,
        spineItems: List<ManifestItem>,
        indentLevel: Int,
    ) {
        if (label.isBlank() || href.isBlank()) return
        val path = resolveZipPath(opfDir, href.substringBefore('#'))
        val fragment = href.substringAfter('#', "").takeIf { it.isNotBlank() }
        val chapterIndex =
            spineItems.indexOfFirst { item ->
                val spinePath = resolveZipPath(opfDir, item.href)
                spinePath == path || spinePath.endsWith(path) || path.endsWith(spinePath)
            }
        if (chapterIndex < 0) return
        rows +=
            ReaderTocItem(
                label = label,
                chapterIndex = chapterIndex,
                fragment = fragment,
                indentLevel = indentLevel,
            )
    }

    private fun parseXml(xml: String) =
        DocumentBuilderFactory
            .newInstance()
            .apply {
                isNamespaceAware = true
                safeSetFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                safeSetFeature("http://xml.org/sax/features/external-general-entities", false)
                safeSetFeature("http://xml.org/sax/features/external-parameter-entities", false)
            }.newDocumentBuilder()
            .parse(InputSource(StringReader(xml)))

    private fun DocumentBuilderFactory.safeSetFeature(
        name: String,
        value: Boolean,
    ) {
        runCatching { setFeature(name, value) }
    }

    private fun ZipFile.readTextEntry(name: String): String? {
        val entry = getEntry(name) ?: return null
        return getInputStream(entry).bufferedReader().use { it.readText() }
    }

    private fun resolveZipPath(
        baseDir: String,
        href: String,
    ): String {
        val decoded = URLDecoder.decode(href.substringBefore('#'), "UTF-8")
        val parts =
            (if (baseDir.isBlank()) decoded else "$baseDir/$decoded")
                .split('/')
                .filter { it.isNotBlank() && it != "." }
        val normalized = mutableListOf<String>()
        parts.forEach { part ->
            if (part == "..") {
                if (normalized.isNotEmpty()) normalized.removeAt(normalized.lastIndex)
            } else {
                normalized += part
            }
        }
        return normalized.joinToString("/")
    }

    private fun sanitizeFileName(string: String): String =
        string
            .split(Regex("[\\\\/:*?\"<>|\\n\\r\\u0000-\\u001F]+"))
            .joinToString("_")
            .trim()

    private fun defaultReadingCategory() =
        BookCategory(
            id = UUID.randomUUID().toString(),
            name = DEFAULT_READING_CATEGORY_NAME,
            createdAt = System.currentTimeMillis(),
            order = 0,
        )
}

private fun List<BookCategory>.normalizedCategories(): List<BookCategory> =
    distinctBy { it.id }
        .sortedBy { it.order }
        .mapIndexed { index, category ->
            if (category.order == index) category else category.copy(order = index)
        }

private fun Element.elementsByLocalName(name: String): List<Element> {
    val byNamespace = getElementsByTagNameNS("*", name)
    val nodes = if (byNamespace.length > 0) byNamespace else getElementsByTagName(name)
    return buildList {
        repeat(nodes.length) { index ->
            val node = nodes.item(index)
            if (node is Element) add(node)
        }
    }
}

private fun Element.directChildElementsByLocalName(name: String): List<Element> =
    buildList {
        repeat(childNodes.length) { index ->
            val child = childNodes.item(index)
            if (child is Element && (child.localName == name || child.tagName == name)) {
                add(child)
            }
        }
    }

private fun Document.elementsByLocalName(name: String): List<Element> = documentElement.elementsByLocalName(name)

private fun String.filteredCharacterCount(): Int = filteredReaderCharacterCount()

@Serializable
private data class BookCategoriesStorage(
    val version: Int = 1,
    val categories: List<BookCategory> = emptyList(),
)

@Serializable
private data class BookCategoryMapStorage(
    val version: Int = 1,
    val books: Map<String, List<String>> = emptyMap(),
)

@Serializable
private data class BookMetadataStorage(
    val id: String,
    val title: String,
    val author: String? = null,
    val coverPath: String? = null,
    val importedAt: Long,
    val lastOpenedAt: Long? = null,
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

private data class EpubMetadata(
    val title: String,
    val author: String? = null,
    val coverPath: String? = null,
)

private data class ProcessedReaderBook(
    val chapters: List<ReaderChapter>,
    val tocRows: List<ReaderTocItem>,
    val info: ReaderBookInfoStorage,
)

private data class ManifestItem(
    val id: String,
    val href: String,
    val mediaType: String,
    val properties: String,
)

private const val BOOKS_DIR_NAME = "Books"
private const val CATEGORIES_FILE = "categories.json"
private const val BOOK_CATEGORY_MAP_FILE = "book_categories.json"
private const val METADATA_FILE = "metadata.json"
private const val BOOKINFO_FILE = "bookinfo.json"
private const val TOC_FILE = "toc.json"
private const val BOOKMARK_FILE = "bookmark.json"
private const val SOURCE_FILE_NAME = "source.epub"
private const val DEFAULT_READING_CATEGORY_NAME = "在读"
private const val EXTRACTED_EPUB_DIR = "content"
private const val EXTRACTION_MARKER_FILE = ".mori_extracted"
