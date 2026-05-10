package app.mori.reader.features.anki.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mori.reader.data.anki.AnkiConnectConfig
import app.mori.reader.data.anki.AnkiConnectionMode
import app.mori.reader.data.anki.AnkiDuplicateScope
import app.mori.reader.data.anki.AnkiMiningContent
import app.mori.reader.data.anki.AnkiMiningContext
import app.mori.reader.data.anki.AnkiService
import app.mori.reader.data.anki.AnkiSettings
import app.mori.reader.data.anki.AnkiSettingsRepository
import app.mori.reader.data.anki.isLapis
import app.mori.reader.data.anki.lapisFieldMappings
import app.mori.reader.ui.AppEffect
import app.mori.reader.ui.text.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnkiViewModel(
    private val settingsRepository: AnkiSettingsRepository,
    private val ankiService: AnkiService,
) : ViewModel() {
    private val _state = MutableStateFlow(AnkiState())
    val state = _state.asStateFlow()

    private val _effects = Channel<AppEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var cachedSettings = AnkiSettings()

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                cachedSettings = settings
                _state.update {
                    it.copy(
                        settings = settings,
                        decks = settings.cachedDecks,
                        noteTypes = settings.cachedNoteTypes,
                    )
                }
            }
        }
    }

    fun onIntent(intent: AnkiIntent) {
        when (intent) {
            AnkiIntent.Connect -> {
                connect(showFeedback = true)
            }

            AnkiIntent.CheckAvailability -> {
                connect(showFeedback = false)
            }

            AnkiIntent.FetchDecksAndModels -> {
                fetchDecksAndModels()
            }

            is AnkiIntent.SelectDeck -> {
                updateSettings { it.copy(selectedDeck = intent.deckName?.takeIf(String::isNotBlank)) }
            }

            is AnkiIntent.SelectNoteType -> {
                selectNoteType(intent.noteTypeName)
            }

            is AnkiIntent.SetFieldMapping -> {
                setFieldMapping(intent.fieldName, intent.template)
            }

            is AnkiIntent.SetTags -> {
                updateSettings {
                    it.copy(
                        tags =
                            intent.tags
                                .map(String::trim)
                                .filter(String::isNotBlank)
                                .distinct(),
                    )
                }
            }

            is AnkiIntent.SetConnectionMode -> {
                setConnectionMode(intent.mode)
            }

            is AnkiIntent.SetAnkiConnectUrl -> {
                updateSettings { it.copy(ankiConnect = it.ankiConnect.copy(url = intent.url)) }
            }

            is AnkiIntent.SetAnkiConnectTimeoutMillis -> {
                setAnkiConnectTimeoutMillis(intent.timeoutMillis)
            }

            is AnkiIntent.SetDuplicateScope -> {
                setDuplicateScope(intent.scope)
            }

            is AnkiIntent.SetCheckAllModels -> {
                updateSettings { it.copy(checkAllModels = intent.enabled) }
            }

            is AnkiIntent.SetForceSync -> {
                updateSettings { it.copy(forceSync = intent.enabled) }
            }

            is AnkiIntent.SetAllowDuplicates -> {
                updateSettings { it.copy(allowDuplicates = intent.enabled) }
            }

            is AnkiIntent.SetCompactGlossaries -> {
                updateSettings { it.copy(compactGlossaries = intent.enabled) }
            }

            is AnkiIntent.SetEmbedMedia -> {
                updateSettings { it.copy(embedMedia = intent.enabled) }
            }

            is AnkiIntent.SetShowLapisTemplateHint -> {
                updateSettings { it.copy(showLapisTemplateHint = intent.show) }
            }

            is AnkiIntent.MineNote -> {
                mineNote(intent.content, intent.context)
            }

            is AnkiIntent.CheckDuplicate -> {
                checkDuplicate(intent.expression)
            }

            AnkiIntent.DismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun connect(showFeedback: Boolean) {
        _state.update { it.copy(isConnecting = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { ankiService.connect(cachedSettings) }
                .onSuccess { connected ->
                    _state.update {
                        it.copy(
                            isConnecting = false,
                            isConnected = connected,
                            errorMessage = if (connected) null else UiText.Plain("Anki unavailable"),
                        )
                    }
                    if (showFeedback) {
                        sendMessage(if (connected) "Anki connected" else "Anki unavailable")
                    }
                }.onFailure { throwable ->
                    val message = throwable.toUiText("Anki connection failed")
                    _state.update {
                        it.copy(
                            isConnecting = false,
                            isConnected = false,
                            errorMessage = message,
                        )
                    }
                    if (showFeedback) {
                        _effects.send(AppEffect.ShowMessage(message))
                    }
                }
        }
    }

    private fun fetchDecksAndModels() {
        _state.update { it.copy(isFetching = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { ankiService.fetchDecksAndModels(cachedSettings) }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isFetching = false,
                            isConnected = true,
                            decks = result.decks,
                            noteTypes = result.noteTypes,
                        )
                    }
                    sendMessage("Anki decks and note types updated")
                }.onFailure { throwable ->
                    val message = throwable.toUiText("Failed to fetch Anki data")
                    _state.update {
                        it.copy(
                            isFetching = false,
                            isConnected = false,
                            errorMessage = message,
                        )
                    }
                    _effects.send(AppEffect.ShowMessage(message))
                }
        }
    }

    private fun selectNoteType(noteTypeName: String?) {
        val noteType =
            _state.value.noteTypes
                .firstOrNull { it.name == noteTypeName }
        val fields = noteType?.fields.orEmpty()
        updateSettings {
            val selectedName = noteTypeName?.takeIf(String::isNotBlank)
            val lapisMappings =
                if (noteType?.isLapis() == true || selectedName?.contains("lapis", ignoreCase = true) == true) {
                    lapisFieldMappings()
                } else {
                    emptyMap()
                }
            if (fields.isEmpty() && lapisMappings.isEmpty()) {
                it.copy(selectedNoteType = selectedName)
            } else if (fields.isEmpty()) {
                it.copy(
                    selectedNoteType = selectedName,
                    fieldMappings = lapisMappings + it.fieldMappings.filterValues(String::isNotBlank),
                )
            } else {
                it.copy(
                    selectedNoteType = selectedName,
                    fieldMappings =
                        fields.associate { field ->
                            val existingTemplate = it.fieldMappings[field.name].orEmpty()
                            field.name to
                                existingTemplate.ifBlank {
                                    lapisMappings[field.name].orEmpty()
                                }
                        },
                )
            }
        }
    }

    private fun setFieldMapping(
        fieldName: String,
        template: String,
    ) {
        updateSettings {
            it.copy(fieldMappings = it.fieldMappings + (fieldName to template))
        }
    }

    private fun setConnectionMode(mode: AnkiConnectionMode) {
        _state.update {
            it.copy(
                errorMessage = null,
                isConnected = false,
            )
        }
        updateSettings { it.copy(connectionMode = mode) }
    }

    private fun setAnkiConnectTimeoutMillis(timeoutMillis: Int) {
        updateSettings {
            it.copy(
                ankiConnect =
                    AnkiConnectConfig(
                        url = it.ankiConnect.url,
                        timeoutMillis = timeoutMillis.coerceIn(1_000, 60_000),
                    ),
            )
        }
    }

    private fun setDuplicateScope(scope: AnkiDuplicateScope) {
        updateSettings { it.copy(duplicateScope = scope) }
    }

    private fun mineNote(
        content: AnkiMiningContent,
        context: AnkiMiningContext,
    ) {
        _state.update { it.copy(isAddingNote = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { ankiService.mineNote(cachedSettings, content, context) }
                .onSuccess {
                    _state.update { it.copy(isAddingNote = false, isConnected = true) }
                    sendMessage("Added Anki note")
                }.onFailure { throwable ->
                    val message = throwable.toUiText("Failed to add Anki note")
                    _state.update {
                        it.copy(
                            isAddingNote = false,
                            isConnected = false,
                            errorMessage = message,
                        )
                    }
                    _effects.send(AppEffect.ShowMessage(message))
                }
        }
    }

    private fun checkDuplicate(expression: String) {
        viewModelScope.launch {
            runCatching { ankiService.checkDuplicate(cachedSettings, expression) }
                .onSuccess { isDuplicate ->
                    _state.update {
                        it.copy(
                            isConnected = true,
                            duplicateExpression = expression.takeIf { isDuplicate },
                        )
                    }
                }.onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isConnected = false,
                            errorMessage = throwable.toUiText("Anki duplicate check failed"),
                        )
                    }
                }
        }
    }

    private fun updateSettings(transform: (AnkiSettings) -> AnkiSettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(transform)
        }
    }

    private suspend fun sendMessage(value: String) {
        _effects.send(AppEffect.ShowMessage(UiText.Plain(value)))
    }

    private fun Throwable.toUiText(fallback: String): UiText = message?.let(UiText::Plain) ?: UiText.Plain(fallback)
}
