package app.mori.reader.data.anki

import kotlinx.coroutines.flow.Flow

interface AnkiSettingsRepository {
    val settingsFlow: Flow<AnkiSettings>

    suspend fun updateSettings(transform: (AnkiSettings) -> AnkiSettings)
}
