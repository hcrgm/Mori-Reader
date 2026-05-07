package app.mori.reader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RootViewModel(
    private val settingsRepository: SettingsRepository,
    initialSettings: AppSettings? = null,
) : ViewModel() {
    private val _state =
        MutableStateFlow(
            AppState(
                settings = initialSettings ?: AppSettings(),
                settingsLoaded = initialSettings != null,
            ),
        )
    val state = _state.asStateFlow()

    private val effectChannel = Channel<AppEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update { state ->
                    state.copy(settings = settings, settingsLoaded = true)
                }
            }
        }
    }

    fun onIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.OpenBook -> {
                viewModelScope.launch {
                    effectChannel.send(AppEffect.OpenReader(intent.id))
                }
            }
        }
    }
}
