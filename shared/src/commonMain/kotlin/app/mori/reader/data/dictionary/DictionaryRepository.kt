package app.mori.reader.data.dictionary

interface DictionaryRepository {
    suspend fun loadDictionaries(): DictionaryCatalog

    suspend fun importDictionaries(
        type: DictionaryType,
        uriStrings: List<String>,
        onProgress: ((DictionaryImportProgress) -> Unit)? = null,
    ): DictionaryImportResult

    suspend fun setEnabled(
        type: DictionaryType,
        id: String,
        enabled: Boolean,
    ): DictionaryCatalog

    suspend fun move(
        type: DictionaryType,
        id: String,
        direction: MoveDirection,
    ): DictionaryCatalog

    suspend fun reorder(
        type: DictionaryType,
        ids: List<String>,
    ): DictionaryCatalog

    suspend fun delete(
        type: DictionaryType,
        id: String,
    ): DictionaryCatalog

    suspend fun updateDictionaries(): DictionaryCatalog

    suspend fun rebuildQuery(catalog: DictionaryCatalog)

    suspend fun lookup(
        text: String,
        maxResults: Int,
    ): DictionaryLookupResult
}

enum class MoveDirection {
    Up,
    Down,
}

data class DictionaryImportProgress(
    val currentIndex: Int,
    val totalCount: Int,
)

data class DictionaryImportFailure(
    val fileName: String,
    val reason: DictionaryImportFailureReason,
)

enum class DictionaryImportFailureReason {
    UnsupportedFile,
    CorruptedFile,
    UnreadableFile,
    Unknown,
}

data class DictionaryImportResult(
    val catalog: DictionaryCatalog,
    val successCount: Int,
    val failures: List<DictionaryImportFailure>,
)
