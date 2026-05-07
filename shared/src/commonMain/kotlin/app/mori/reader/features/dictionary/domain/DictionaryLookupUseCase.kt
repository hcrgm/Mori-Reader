package app.mori.reader.features.dictionary.domain

import app.mori.reader.data.dictionary.DictionaryLookupResult
import app.mori.reader.data.dictionary.DictionaryRepository

class DictionaryLookupUseCase(
    private val dictionaryRepository: DictionaryRepository,
) {
    suspend operator fun invoke(
        query: String,
        maxResults: Int,
    ): DictionaryLookupResult = dictionaryRepository.lookup(query, maxResults)
}
