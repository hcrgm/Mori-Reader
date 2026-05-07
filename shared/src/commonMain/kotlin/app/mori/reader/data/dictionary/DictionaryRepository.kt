package app.mori.reader.data.dictionary

interface DictionaryRepository {
    suspend fun loadDictionaries(): DictionaryCatalog

    suspend fun importDictionaries(
        type: DictionaryType,
        uriStrings: List<String>,
    ): DictionaryCatalog

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
