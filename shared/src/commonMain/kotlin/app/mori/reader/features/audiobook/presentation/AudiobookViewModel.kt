package app.mori.reader.features.audiobook.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mori.reader.data.audiobook.AudiobookAssetBundle
import app.mori.reader.data.audiobook.AudiobookAssetType
import app.mori.reader.data.audiobook.AudiobookRepository
import app.mori.reader.data.audiobook.AudiobookStorageMode
import app.mori.reader.data.settings.SettingsRepository
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.error_audiobook_asset_delete_failed
import app.mori.reader.shared.generated.resources.error_audiobook_asset_load_failed
import app.mori.reader.shared.generated.resources.error_audiobook_audio_import_failed
import app.mori.reader.shared.generated.resources.error_audiobook_match_failed
import app.mori.reader.shared.generated.resources.error_audiobook_match_delete_failed
import app.mori.reader.shared.generated.resources.error_audiobook_subtitle_import_failed
import app.mori.reader.shared.generated.resources.toast_audiobook_asset_deleted
import app.mori.reader.shared.generated.resources.toast_audiobook_audio_imported
import app.mori.reader.shared.generated.resources.toast_audiobook_match_complete
import app.mori.reader.shared.generated.resources.toast_audiobook_match_deleted
import app.mori.reader.shared.generated.resources.toast_audiobook_subtitle_imported
import app.mori.reader.ui.text.UiText
import app.mori.reader.ui.text.uiText
import app.mori.reader.ui.text.uiTextOr
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AudiobookViewModel(
    private val audiobookRepository: AudiobookRepository,
    private val settingsRepository: SettingsRepository,
    preferredStorageMode: AudiobookStorageMode = AudiobookStorageMode.Copy,
) : ViewModel() {
    private val _state = MutableStateFlow(
        AudiobookState(preferredStorageMode = preferredStorageMode)
    )
    val state = _state.asStateFlow()

    private var _preferredStorageMode: AudiobookStorageMode = preferredStorageMode

    private val _effect = Channel<UiText>(Channel.BUFFERED)
    val effects = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                val mode = settings.sasayaki.preferredStorageMode
                if (_preferredStorageMode != mode) {
                    _preferredStorageMode = mode
                    _state.update { it.copy(preferredStorageMode = mode) }
                }
            }
        }
    }

    fun onIntent(intent: AudiobookIntent) {
        when (intent) {
            is AudiobookIntent.OpenAudiobookManager -> openAudiobookManager(intent.bookId)
            AudiobookIntent.CloseAudiobookManager -> _state.update {
                it.copy(selectedBookId = null, errorMessage = null)
            }

            is AudiobookIntent.LoadAudiobookAssets -> loadAudiobookAssets(intent.bookId)
            is AudiobookIntent.ImportAudiobookAudio -> importAudiobookAudio(
                intent.bookId,
                intent.uriString,
            )

            is AudiobookIntent.ImportAudiobookSubtitle -> importAudiobookSubtitle(
                intent.bookId,
                intent.uriString,
            )

            is AudiobookIntent.DeleteAudiobookAsset -> deleteAudiobookAsset(
                intent.bookId,
                intent.type,
            )

            is AudiobookIntent.RunAudiobookMatch -> runAudiobookMatch(
                intent.bookId,
                intent.searchWindow,
            )

            is AudiobookIntent.DeleteAudiobookMatch -> deleteAudiobookMatch(intent.bookId)
            is AudiobookIntent.SetAudiobookSearchWindow -> setAudiobookSearchWindow(intent.value)
            is AudiobookIntent.SetAudiobookStorageMode -> setAudiobookStorageMode(intent.mode)
            AudiobookIntent.DismissAudiobookError -> _state.update {
                it.copy(errorMessage = null)
            }
        }
    }

    private fun openAudiobookManager(bookId: String) {
        _state.update {
            it.copy(
                selectedBookId = bookId,
                preferredStorageMode = _preferredStorageMode,
                errorMessage = null,
            )
        }
        loadAudiobookAssets(bookId)
    }

    private fun loadAudiobookAssets(bookId: String) {
        _state.update {
            it.copy(
                selectedBookId = bookId,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            runCatching { audiobookRepository.loadAssets(bookId) }
                .onSuccess { bundle -> updateAudiobookBundle(bookId, bundle) }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            errorMessage = throwable.uiTextOr(Res.string.error_audiobook_asset_load_failed),
                        )
                    }
                }
        }
    }

    private fun importAudiobookAudio(bookId: String, uriString: String) {
        val storageMode = _state.value.preferredStorageMode
        _state.update {
            it.copy(isImportingAudio = true, errorMessage = null)
        }
        viewModelScope.launch {
            runCatching { audiobookRepository.importAudio(bookId, uriString, storageMode) }
                .onSuccess { bundle ->
                    updateAudiobookBundle(bookId, bundle) {
                        it.copy(isImportingAudio = false)
                    }
                    sendEffect(uiText(Res.string.toast_audiobook_audio_imported))
                }
                .onFailure { throwable ->
                    val message = throwable.uiTextOr(Res.string.error_audiobook_audio_import_failed)
                    _state.update {
                        it.copy(
                            isImportingAudio = false,
                            errorMessage = message,
                        )
                    }
                    sendEffect(message)
                }
        }
    }

    private fun importAudiobookSubtitle(bookId: String, uriString: String) {
        _state.update {
            it.copy(isImportingSubtitle = true, errorMessage = null)
        }
        viewModelScope.launch {
            runCatching {
                audiobookRepository.importSubtitle(
                    bookId,
                    uriString,
                    AudiobookStorageMode.Copy,
                )
            }
                .onSuccess { bundle ->
                    updateAudiobookBundle(bookId, bundle) {
                        it.copy(isImportingSubtitle = false)
                    }
                    sendEffect(uiText(Res.string.toast_audiobook_subtitle_imported))
                }
                .onFailure { throwable ->
                    val message = throwable.uiTextOr(Res.string.error_audiobook_subtitle_import_failed)
                    _state.update {
                        it.copy(
                            isImportingSubtitle = false,
                            errorMessage = message,
                        )
                    }
                    sendEffect(message)
                }
        }
    }

    private fun deleteAudiobookAsset(bookId: String, type: AudiobookAssetType) {
        viewModelScope.launch {
            runCatching { audiobookRepository.deleteAsset(bookId, type) }
                .onSuccess { bundle ->
                    updateAudiobookBundle(bookId, bundle)
                    sendEffect(uiText(Res.string.toast_audiobook_asset_deleted))
                }
                .onFailure { throwable ->
                    val message = throwable.uiTextOr(Res.string.error_audiobook_asset_delete_failed)
                    _state.update { it.copy(errorMessage = message) }
                    sendEffect(message)
                }
        }
    }

    private fun runAudiobookMatch(bookId: String, searchWindow: Int) {
        val clampedWindow = searchWindow.coerceIn(50, 350)
        _state.update {
            it.copy(
                isMatching = true,
                searchWindow = clampedWindow,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            runCatching { audiobookRepository.runMatch(bookId, clampedWindow) }
                .onSuccess { bundle ->
                    updateAudiobookBundle(bookId, bundle) {
                        it.copy(isMatching = false, searchWindow = clampedWindow)
                    }
                    val matched = bundle.matchData?.matches?.size ?: 0
                    val total = matched + (bundle.matchData?.unmatched ?: 0)
                    sendEffect(uiText(Res.string.toast_audiobook_match_complete, matched, total))
                }
                .onFailure { throwable ->
                    val message = throwable.uiTextOr(Res.string.error_audiobook_match_failed)
                    _state.update {
                        it.copy(
                            isMatching = false,
                            errorMessage = message,
                        )
                    }
                    sendEffect(message)
                }
        }
    }

    private fun deleteAudiobookMatch(bookId: String) {
        viewModelScope.launch {
            runCatching { audiobookRepository.deleteMatch(bookId) }
                .onSuccess { bundle ->
                    updateAudiobookBundle(bookId, bundle)
                    sendEffect(uiText(Res.string.toast_audiobook_match_deleted))
                }
                .onFailure { throwable ->
                    val message = throwable.uiTextOr(Res.string.error_audiobook_match_delete_failed)
                    _state.update { it.copy(errorMessage = message) }
                    sendEffect(message)
                }
        }
    }

    private fun setAudiobookSearchWindow(value: Int) {
        val clamped = value.coerceIn(50, 350)
        _state.update { it.copy(searchWindow = clamped) }
    }

    private fun setAudiobookStorageMode(mode: AudiobookStorageMode) {
        if (_preferredStorageMode == mode) return
        _preferredStorageMode = mode
        _state.update { it.copy(preferredStorageMode = mode) }
        viewModelScope.launch {
            settingsRepository.setPreferredAudiobookStorageMode(mode)
        }
    }

    private fun updateAudiobookBundle(
        bookId: String,
        bundle: AudiobookAssetBundle,
        transform: (AudiobookState) -> AudiobookState = { it },
    ) {
        _state.update {
            transform(
                it.copy(
                    selectedBookId = bookId,
                    audioAssetInfo = bundle.audioAssetInfo,
                    subtitleAssetInfo = bundle.subtitleAssetInfo,
                    subtitleData = bundle.subtitleData,
                    matchData = bundle.matchData,
                    errorMessage = null,
                )
            )
        }
    }

    private fun sendEffect(message: UiText) {
        _effect.trySend(message)
    }
}
