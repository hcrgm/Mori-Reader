package app.mori.reader.features.settings.presentation

import app.mori.reader.data.dictionary.DictionaryImportFailureReason
import app.mori.reader.data.dictionary.DictionaryInfo
import app.mori.reader.data.dictionary.DictionaryType
import app.mori.reader.ui.text.UiText

data class SettingsUiState(
    val isImportingLocalAudio: Boolean = false,
    val dictionaryManagement: DictionaryManagementState = DictionaryManagementState(),
)

data class DictionaryManagementState(
    val selectedType: DictionaryType = DictionaryType.Term,
    val termDictionaries: List<DictionaryInfo> = emptyList(),
    val frequencyDictionaries: List<DictionaryInfo> = emptyList(),
    val pitchDictionaries: List<DictionaryInfo> = emptyList(),
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val isUpdating: Boolean = false,
    val importProgress: DictionaryImportUiProgress? = null,
    val importSummary: DictionaryImportSummary? = null,
    val statusText: UiText? = null,
    val errorMessage: UiText? = null,
) {
    fun dictionaries(type: DictionaryType = selectedType): List<DictionaryInfo> =
        when (type) {
            DictionaryType.Term -> termDictionaries
            DictionaryType.Frequency -> frequencyDictionaries
            DictionaryType.Pitch -> pitchDictionaries
        }
}

data class DictionaryImportUiProgress(
    val currentIndex: Int,
    val totalCount: Int,
)

data class DictionaryImportSummary(
    val successCount: Int,
    val failureCount: Int,
    val failures: List<DictionaryImportFailureItem>,
)

data class DictionaryImportFailureItem(
    val fileName: String,
    val reason: DictionaryImportFailureReason,
)
