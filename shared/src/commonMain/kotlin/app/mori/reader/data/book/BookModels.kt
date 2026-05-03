package app.mori.reader.data.book

import kotlinx.serialization.Serializable

@Serializable
data class BookInfo(
    val id: String,
    val title: String,
    val author: String? = null,
    val coverPath: String? = null,
    val categoryIds: List<String> = emptyList(),
    val progressPercent: Int = 0,
    val currentChapterName: String? = null,
    val importedAt: Long,
    val lastOpenedAt: Long? = null,
)

@Serializable
data class ReaderBookmark(
    val chapterIndex: Int = 0,
    val chapterProgress: Double = 0.0,
    val characterCount: Int = 0,
    val lastModifiedAt: Long? = null,
)

@Serializable
data class ReaderChapter(
    val id: String,
    val title: String,
    val href: String,
    val sourceUrl: String,
    val index: Int,
    val characterStart: Int,
    val characterCount: Int,
)

@Serializable
data class ReaderTocItem(
    val label: String,
    val chapterIndex: Int,
    val fragment: String? = null,
    val characterCount: Int? = null,
    val indentLevel: Int = 0,
)

@Serializable
data class ReaderBook(
    val info: BookInfo,
    val chapters: List<ReaderChapter>,
    val tableOfContents: List<ReaderTocItem>,
    val totalCharacterCount: Int,
    val bookmark: ReaderBookmark = ReaderBookmark(),
) {
    val currentChapter: ReaderChapter?
        get() = chapters.getOrNull(bookmark.chapterIndex.coerceIn(chapters.indices))
}

@Serializable
data class BookCategory(
    val id: String,
    val name: String,
    val createdAt: Long,
    val order: Int = 0,
)

data class BookCatalog(
    val books: List<BookInfo> = emptyList(),
    val categories: List<BookCategory> = emptyList(),
)
