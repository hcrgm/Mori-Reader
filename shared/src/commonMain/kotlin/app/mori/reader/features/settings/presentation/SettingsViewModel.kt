package app.mori.reader.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mori.reader.data.audio.AudioRepository
import app.mori.reader.data.dictionary.DictionaryCatalog
import app.mori.reader.data.dictionary.DictionaryRepository
import app.mori.reader.data.dictionary.DictionaryType
import app.mori.reader.data.dictionary.MoveDirection
import app.mori.reader.data.settings.AudioSource
import app.mori.reader.data.settings.SettingsRepository
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.error_dictionary_load_failed
import app.mori.reader.shared.generated.resources.error_dictionary_operation_failed
import app.mori.reader.shared.generated.resources.toast_audio_local_delete_failed
import app.mori.reader.shared.generated.resources.toast_audio_local_deleted
import app.mori.reader.shared.generated.resources.toast_audio_local_import_failed
import app.mori.reader.shared.generated.resources.toast_audio_local_imported
import app.mori.reader.shared.generated.resources.toast_audio_url_exists
import app.mori.reader.shared.generated.resources.toast_audio_url_requires_term_or_reading
import app.mori.reader.shared.generated.resources.toast_dict_checking_updates
import app.mori.reader.shared.generated.resources.toast_dict_import_failed
import app.mori.reader.shared.generated.resources.toast_dict_imported
import app.mori.reader.shared.generated.resources.toast_dict_importing
import app.mori.reader.shared.generated.resources.toast_dict_update_failed
import app.mori.reader.shared.generated.resources.toast_dict_updates_complete
import app.mori.reader.ui.AppEffect
import app.mori.reader.ui.text.uiText
import app.mori.reader.ui.text.uiTextOr
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val audioRepository: AudioRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    private val _effects = Channel<AppEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var cachedSources: List<AudioSource> =
        listOf(AudioSource.Local.copy(isEnabled = false), AudioSource.Default)

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { cachedSources = it.audio.sources }
        }
        loadDictionaryCatalog()
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetBookshelfSortMode -> {
                viewModelScope.launch { settingsRepository.setBookshelfSortMode(intent.mode) }
            }

            is SettingsIntent.SetReaderFontSize -> {
                val value = intent.value.coerceIn(16, 40)
                viewModelScope.launch { settingsRepository.setReaderFontSize(value) }
            }

            is SettingsIntent.SetReaderVerticalWriting -> {
                viewModelScope.launch { settingsRepository.setReaderVerticalWriting(intent.enabled) }
            }

            is SettingsIntent.SetReaderLineHeight -> {
                val value = intent.value.coerceIn(1.0, 2.5)
                viewModelScope.launch { settingsRepository.setReaderLineHeight(value) }
            }

            is SettingsIntent.SetReaderHorizontalPadding -> {
                val value = intent.value.coerceIn(0, 50)
                viewModelScope.launch { settingsRepository.setReaderHorizontalPadding(value) }
            }

            is SettingsIntent.SetReaderVerticalPadding -> {
                val value = intent.value.coerceIn(0, 50)
                viewModelScope.launch { settingsRepository.setReaderVerticalPadding(value) }
            }

            is SettingsIntent.SetReaderAvoidPageBreak -> {
                viewModelScope.launch { settingsRepository.setReaderAvoidPageBreak(intent.enabled) }
            }

            is SettingsIntent.SetReaderJustifyText -> {
                viewModelScope.launch { settingsRepository.setReaderJustifyText(intent.enabled) }
            }

            is SettingsIntent.SetReaderLayoutAdvanced -> {
                viewModelScope.launch { settingsRepository.setReaderLayoutAdvanced(intent.enabled) }
            }

            is SettingsIntent.SetReaderCharacterSpacing -> {
                val value = intent.value.coerceIn(-10.0, 10.0)
                viewModelScope.launch { settingsRepository.setReaderCharacterSpacing(value) }
            }

            is SettingsIntent.SetReaderContinuousMode -> {
                viewModelScope.launch { settingsRepository.setReaderContinuousMode(intent.enabled) }
            }

            is SettingsIntent.SetReaderHideFurigana -> {
                viewModelScope.launch { settingsRepository.setReaderHideFurigana(intent.enabled) }
            }

            is SettingsIntent.SetReaderFullscreen -> {
                viewModelScope.launch { settingsRepository.setReaderFullscreen(intent.enabled) }
            }

            is SettingsIntent.SetPopupWidth -> {
                val value = intent.value.coerceIn(100, 700)
                viewModelScope.launch { settingsRepository.setPopupWidth(value) }
            }

            is SettingsIntent.SetPopupHeight -> {
                val value = intent.value.coerceIn(100, 500)
                viewModelScope.launch { settingsRepository.setPopupHeight(value) }
            }

            is SettingsIntent.SetPopupFullWidth -> {
                viewModelScope.launch { settingsRepository.setPopupFullWidth(intent.enabled) }
            }

            is SettingsIntent.SetPopupSwipeToDismiss -> {
                viewModelScope.launch { settingsRepository.setPopupSwipeToDismiss(intent.enabled) }
            }

            is SettingsIntent.SetPopupSwipeThreshold -> {
                val value = intent.value.coerceIn(20, 80)
                viewModelScope.launch { settingsRepository.setPopupSwipeThreshold(value) }
            }

            is SettingsIntent.SetThemeMode -> {
                viewModelScope.launch { settingsRepository.setThemeMode(intent.mode) }
            }

            is SettingsIntent.SetUiThemeEngine -> {
                viewModelScope.launch { settingsRepository.setUiThemeEngine(intent.engine) }
            }

            is SettingsIntent.SetUiScalePercent -> {
                val value = normalizeUiScalePercent(intent.value)
                viewModelScope.launch { settingsRepository.setUiScalePercent(value) }
            }

            is SettingsIntent.SetLanguageMode -> {
                viewModelScope.launch { settingsRepository.setLanguageMode(intent.mode) }
            }

            is SettingsIntent.SetReaderThemeMode -> {
                viewModelScope.launch { settingsRepository.setReaderThemeMode(intent.mode) }
            }

            is SettingsIntent.SetBlurEnabled -> {
                viewModelScope.launch { settingsRepository.setBlurEnabled(intent.enabled) }
            }

            is SettingsIntent.SetMonetEnabled -> {
                viewModelScope.launch { settingsRepository.setMonetEnabled(intent.enabled) }
            }

            is SettingsIntent.SetMonetKeyColor -> {
                viewModelScope.launch { settingsRepository.setMonetKeyColor(intent.color) }
            }

            is SettingsIntent.SetMaxResults -> {
                val value = intent.value.coerceIn(1, 50)
                viewModelScope.launch { settingsRepository.setMaxResults(value) }
            }

            is SettingsIntent.SetScanLength -> {
                val value = intent.value.coerceIn(1, 64)
                viewModelScope.launch { settingsRepository.setScanLength(value) }
            }

            is SettingsIntent.SetCollapseDictionaries -> {
                viewModelScope.launch { settingsRepository.setCollapseDictionaries(intent.enabled) }
            }

            is SettingsIntent.SetCompactGlossaries -> {
                viewModelScope.launch { settingsRepository.setCompactGlossaries(intent.enabled) }
            }

            is SettingsIntent.SetShowExpressionTags -> {
                viewModelScope.launch { settingsRepository.setShowExpressionTags(intent.enabled) }
            }

            is SettingsIntent.SetHarmonicFrequency -> {
                viewModelScope.launch { settingsRepository.setHarmonicFrequency(intent.enabled) }
            }

            is SettingsIntent.SetDeduplicatePitchAccents -> {
                viewModelScope.launch { settingsRepository.setDeduplicatePitchAccents(intent.enabled) }
            }

            is SettingsIntent.SetAudioSourceEnabled -> {
                if (intent.url == AudioSource.Local.url) {
                    viewModelScope.launch { settingsRepository.setEnableLocalAudio(intent.enabled) }
                } else {
                    updateAudioSources { sources ->
                        sources.map { if (it.url == intent.url) it.copy(isEnabled = intent.enabled) else it }
                    }
                }
            }

            is SettingsIntent.AddAudioSource -> {
                addAudioSource(intent.name, intent.url)
            }

            is SettingsIntent.UpdateAudioSource -> {
                updateAudioSource(intent.originalUrl, intent.name, intent.url)
            }

            is SettingsIntent.MoveAudioSource -> {
                updateAudioSources { sources ->
                    val currentIndex = sources.indexOfFirst { it.url == intent.url }
                    if (currentIndex == -1) return@updateAudioSources sources
                    val targetIndex =
                        when (intent.direction) {
                            MoveDirection.Up -> currentIndex - 1
                            MoveDirection.Down -> currentIndex + 1
                        }
                    if (targetIndex !in sources.indices) return@updateAudioSources sources
                    sources.toMutableList().also {
                        val moved = it.removeAt(currentIndex)
                        it.add(targetIndex, moved)
                    }
                }
            }

            is SettingsIntent.ReorderAudioSources -> {
                updateAudioSources { sources ->
                    val urls = intent.urls
                    if (urls.size != sources.size) return@updateAudioSources sources
                    val sourceByUrl = sources.associateBy(AudioSource::url)
                    val reordered = urls.mapNotNull(sourceByUrl::get)
                    if (reordered.size == sources.size) reordered else sources
                }
            }

            is SettingsIntent.DeleteAudioSource -> {
                updateAudioSources { sources ->
                    sources.filterNot { it.url == intent.url && !it.isDefault && !it.isLocal }
                }
            }

            is SettingsIntent.SetEnableLocalAudio -> {
                viewModelScope.launch { settingsRepository.setEnableLocalAudio(intent.enabled) }
            }

            is SettingsIntent.SetAudioEnableAutoplay -> {
                viewModelScope.launch { settingsRepository.setAudioEnableAutoplay(intent.enabled) }
            }

            is SettingsIntent.SetAudioPlaybackMode -> {
                viewModelScope.launch { settingsRepository.setAudioPlaybackMode(intent.mode) }
            }

            is SettingsIntent.ImportLocalAudioDatabase -> {
                importLocalAudioDatabase(intent.uriString)
            }

            SettingsIntent.DeleteLocalAudioDatabase -> {
                deleteLocalAudioDatabase()
            }

            is SettingsIntent.SetAudiobookStorageMode -> {
                viewModelScope.launch { settingsRepository.setPreferredAudiobookStorageMode(intent.mode) }
            }

            is SettingsIntent.SetSasayakiSyncEnabled -> {
                viewModelScope.launch { settingsRepository.setSasayakiSyncEnabled(intent.enabled) }
            }

            is SettingsIntent.SetSasayakiAutoScroll -> {
                viewModelScope.launch { settingsRepository.setSasayakiAutoScroll(intent.enabled) }
            }

            is SettingsIntent.SetSasayakiAutoPauseOnLookup -> {
                viewModelScope.launch { settingsRepository.setSasayakiAutoPauseOnLookup(intent.enabled) }
            }

            is SettingsIntent.SetSasayakiHighlightEnabled -> {
                viewModelScope.launch { settingsRepository.setSasayakiHighlightEnabled(intent.enabled) }
            }

            is SettingsIntent.SetSasayakiHighlightColor -> {
                viewModelScope.launch { settingsRepository.setSasayakiHighlightColor(intent.color) }
            }

            is SettingsIntent.SelectDictionaryType -> {
                updateDictionaryManagement {
                    it.copy(selectedType = intent.type)
                }
            }

            is SettingsIntent.ImportDictionaries -> {
                importDictionaries(intent.type, intent.uriStrings)
            }

            is SettingsIntent.SetDictionaryEnabled -> {
                mutateDictionaries {
                    dictionaryRepository.setEnabled(intent.type, intent.id, intent.enabled)
                }
            }

            is SettingsIntent.MoveDictionary -> {
                mutateDictionaries {
                    dictionaryRepository.move(intent.type, intent.id, intent.direction)
                }
            }

            is SettingsIntent.ReorderDictionaries -> {
                mutateDictionaries {
                    dictionaryRepository.reorder(intent.type, intent.ids)
                }
            }

            is SettingsIntent.DeleteDictionary -> {
                mutateDictionaries {
                    dictionaryRepository.delete(intent.type, intent.id)
                }
            }

            SettingsIntent.UpdateDictionaries -> {
                updateDictionaries()
            }

            SettingsIntent.DismissDictionaryError -> {
                updateDictionaryManagement {
                    it.copy(errorMessage = null)
                }
            }
        }
    }

    private fun addAudioSource(
        name: String,
        url: String,
    ) {
        val trimmedName = name.trim()
        val trimmedUrl = url.trim()
        if (trimmedName.isBlank() || trimmedUrl.isBlank()) return
        if (!trimmedUrl.contains("{term}") && !trimmedUrl.contains("{reading}")) {
            _effects.trySend(AppEffect.ShowMessage(uiText(Res.string.toast_audio_url_requires_term_or_reading)))
            return
        }
        updateAudioSources { sources ->
            if (sources.any { it.url == trimmedUrl }) {
                sources
            } else {
                sources +
                    AudioSource(
                        trimmedName,
                        trimmedUrl,
                    )
            }
        }
    }

    private fun updateAudioSource(
        originalUrl: String,
        name: String,
        url: String,
    ) {
        val trimmedName = name.trim()
        val trimmedUrl = url.trim()
        if (trimmedName.isBlank() || trimmedUrl.isBlank()) return
        if (!trimmedUrl.contains("{term}") && !trimmedUrl.contains("{reading}")) {
            _effects.trySend(AppEffect.ShowMessage(uiText(Res.string.toast_audio_url_requires_term_or_reading)))
            return
        }
        updateAudioSources { sources ->
            val currentIndex = sources.indexOfFirst { it.url == originalUrl }
            if (currentIndex == -1) return@updateAudioSources sources
            val current = sources[currentIndex]
            if (current.isLocal) return@updateAudioSources sources
            if (trimmedUrl != originalUrl && sources.any { it.url == trimmedUrl }) {
                _effects.trySend(AppEffect.ShowMessage(uiText(Res.string.toast_audio_url_exists)))
                return@updateAudioSources sources
            }
            sources.toMutableList().apply {
                this[currentIndex] =
                    current.copy(
                        name = trimmedName,
                        url = trimmedUrl,
                    )
            }
        }
    }

    private fun updateAudioSources(block: (List<AudioSource>) -> List<AudioSource>) {
        val updated = block(cachedSources)
        cachedSources = updated
        viewModelScope.launch { settingsRepository.setAudioSources(updated) }
    }

    private fun importLocalAudioDatabase(uriString: String) {
        _state.update { it.copy(isImportingLocalAudio = true) }
        viewModelScope.launch {
            runCatching { audioRepository.importLocalAudioDatabase(uriString) }
                .onSuccess { sizeBytes ->
                    val updatedSources =
                        cachedSources.map { source ->
                            if (source.isLocal) source.copy(isEnabled = true) else source
                        }
                    cachedSources = updatedSources
                    _state.update { it.copy(isImportingLocalAudio = false) }
                    settingsRepository.setAudioSources(updatedSources)
                    settingsRepository.setLocalAudioDatabaseSizeBytes(sizeBytes)
                    settingsRepository.setEnableLocalAudio(true)
                    _effects.trySend(AppEffect.ShowMessage(uiText(Res.string.toast_audio_local_imported)))
                }.onFailure { throwable ->
                    _state.update { it.copy(isImportingLocalAudio = false) }
                    _effects.trySend(AppEffect.ShowMessage(throwable.uiTextOr(Res.string.toast_audio_local_import_failed)))
                }
        }
    }

    private fun deleteLocalAudioDatabase() {
        viewModelScope.launch {
            runCatching { audioRepository.deleteLocalAudioDatabase() }
                .onSuccess {
                    val updatedSources =
                        cachedSources.map { source ->
                            if (source.isLocal) source.copy(isEnabled = false) else source
                        }
                    cachedSources = updatedSources
                    settingsRepository.setAudioSources(updatedSources)
                    settingsRepository.setLocalAudioDatabaseSizeBytes(0L)
                    settingsRepository.setEnableLocalAudio(false)
                    _effects.trySend(AppEffect.ShowMessage(uiText(Res.string.toast_audio_local_deleted)))
                }.onFailure { throwable ->
                    _effects.trySend(AppEffect.ShowMessage(throwable.uiTextOr(Res.string.toast_audio_local_delete_failed)))
                }
        }
    }

    private fun importDictionaries(
        type: DictionaryType,
        uriStrings: List<String>,
    ) {
        if (uriStrings.isEmpty()) return
        updateDictionaryManagement {
            it.copy(
                isImporting = true,
                isLoading = false,
                statusText = uiText(Res.string.toast_dict_importing),
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            runCatching { dictionaryRepository.importDictionaries(type, uriStrings) }
                .onSuccess { catalog ->
                    updateDictionaryCatalog(catalog) {
                        it.copy(isImporting = false, statusText = null)
                    }
                    _effects.trySend(AppEffect.ShowMessage(uiText(Res.string.toast_dict_imported)))
                }.onFailure { throwable ->
                    updateDictionaryManagement {
                        it.copy(
                            isImporting = false,
                            statusText = null,
                            errorMessage = throwable.uiTextOr(Res.string.toast_dict_import_failed),
                        )
                    }
                }
        }
    }

    private fun updateDictionaries() {
        updateDictionaryManagement {
            it.copy(
                isUpdating = true,
                isLoading = false,
                statusText = uiText(Res.string.toast_dict_checking_updates),
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            runCatching { dictionaryRepository.updateDictionaries() }
                .onSuccess { catalog ->
                    updateDictionaryCatalog(catalog) {
                        it.copy(isUpdating = false, statusText = null)
                    }
                    _effects.trySend(AppEffect.ShowMessage(uiText(Res.string.toast_dict_updates_complete)))
                }.onFailure { throwable ->
                    updateDictionaryManagement {
                        it.copy(
                            isUpdating = false,
                            statusText = null,
                            errorMessage = throwable.uiTextOr(Res.string.toast_dict_update_failed),
                        )
                    }
                }
        }
    }

    private fun mutateDictionaries(block: suspend () -> DictionaryCatalog) {
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess { catalog ->
                    updateDictionaryCatalog(catalog) { it.copy(errorMessage = null) }
                }.onFailure { throwable ->
                    updateDictionaryManagement {
                        it.copy(errorMessage = throwable.uiTextOr(Res.string.error_dictionary_operation_failed))
                    }
                }
        }
    }

    private fun loadDictionaryCatalog() {
        viewModelScope.launch {
            runCatching { dictionaryRepository.loadDictionaries() }
                .onSuccess { catalog ->
                    updateDictionaryCatalog(catalog) {
                        it.copy(isLoading = false, statusText = null, errorMessage = null)
                    }
                }.onFailure { throwable ->
                    updateDictionaryManagement {
                        it.copy(
                            isLoading = false,
                            statusText = null,
                            errorMessage = throwable.uiTextOr(Res.string.error_dictionary_load_failed),
                        )
                    }
                }
        }
    }

    private fun updateDictionaryCatalog(
        catalog: DictionaryCatalog,
        transform: (DictionaryManagementState) -> DictionaryManagementState = { it },
    ) {
        updateDictionaryManagement {
            transform(
                it.copy(
                    termDictionaries = catalog.termDictionaries,
                    frequencyDictionaries = catalog.frequencyDictionaries,
                    pitchDictionaries = catalog.pitchDictionaries,
                ),
            )
        }
    }

    private fun updateDictionaryManagement(transform: (DictionaryManagementState) -> DictionaryManagementState) {
        _state.update { state ->
            state.copy(dictionaryManagement = transform(state.dictionaryManagement))
        }
    }
}

private fun normalizeUiScalePercent(value: Int): Int = ((value.coerceIn(80, 150) + 5) / 10) * 10
