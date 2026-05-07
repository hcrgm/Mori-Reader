package app.mori.reader.data.dictionary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class DictionaryType(
    val directoryName: String,
    val label: String,
) {
    Term("Term", "Term"),
    Frequency("Frequency", "Frequency"),
    Pitch("Pitch", "Pitch"),
}

@Serializable
data class DictionaryIndex(
    val title: String = "",
    val format: Int = 0,
    val revision: String = "",
    val isUpdatable: Boolean = false,
    val indexUrl: String = "",
    val downloadUrl: String = "",
)

@Serializable
data class DictionaryInfo(
    val id: String,
    val index: DictionaryIndex,
    val path: String,
    val fileName: String,
    val isEnabled: Boolean = true,
    val order: Int = 0,
) {
    val isUpdatable: Boolean
        get() = index.isUpdatable && index.indexUrl.isNotBlank() && index.downloadUrl.isNotBlank()
}

@Serializable
data class DictionaryConfig(
    @SerialName("termDictionaries")
    val termDictionaries: List<DictionaryConfigEntry> = emptyList(),
    @SerialName("frequencyDictionaries")
    val frequencyDictionaries: List<DictionaryConfigEntry> = emptyList(),
    @SerialName("pitchDictionaries")
    val pitchDictionaries: List<DictionaryConfigEntry> = emptyList(),
)

@Serializable
data class DictionaryConfigEntry(
    val fileName: String,
    val isEnabled: Boolean,
    val order: Int,
)

data class DictionaryCatalog(
    val termDictionaries: List<DictionaryInfo> = emptyList(),
    val frequencyDictionaries: List<DictionaryInfo> = emptyList(),
    val pitchDictionaries: List<DictionaryInfo> = emptyList(),
) {
    fun dictionaries(type: DictionaryType): List<DictionaryInfo> =
        when (type) {
            DictionaryType.Term -> termDictionaries
            DictionaryType.Frequency -> frequencyDictionaries
            DictionaryType.Pitch -> pitchDictionaries
        }
}
