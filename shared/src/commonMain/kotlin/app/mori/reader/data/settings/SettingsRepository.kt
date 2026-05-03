package app.mori.reader.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.mori.reader.data.anki.AnkiSettings
import app.mori.reader.data.settings.ReaderThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okio.IOException

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val settings: Flow<AppSettings> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            val localAudioDatabaseSizeBytes = preferences[SettingsKeys.LocalAudioDatabaseSizeBytes] ?: 0L
            val enableLocalAudio = (preferences[SettingsKeys.EnableLocalAudio] ?: false) &&
                localAudioDatabaseSizeBytes > 0L
            val audioSources = normalizeAudioSources(
                sources = preferences[SettingsKeys.AudioSources]?.toAudioSources(json)
                    ?: listOf(AudioSource.Default),
                enableLocalAudio = enableLocalAudio,
                hasLocalAudioDatabase = localAudioDatabaseSizeBytes > 0L,
            )
            AppSettings(
                bookshelfSortMode = preferences[SettingsKeys.BookshelfSortMode]?.toBookshelfSortMode()
                    ?: BookshelfSortMode.Recent,
                themeMode = preferences[SettingsKeys.ThemeMode]?.toThemeMode() ?: ThemeMode.System,
                languageMode = preferences[SettingsKeys.LanguageMode]?.toLanguageMode()
                    ?: LanguageMode.System,

                readerThemeMode = preferences[SettingsKeys.ReaderThemeMode]?.toReaderThemeMode()
                    ?: ReaderThemeMode.FollowApp,
                blurEnabled = preferences[SettingsKeys.BlurEnabled] ?: true,
                maxResults = (preferences[SettingsKeys.MaxResults] ?: 16).coerceIn(1, 50),
                scanLength = (preferences[SettingsKeys.ScanLength] ?: 16).coerceIn(1, 64),
                readerFontSize = (preferences[SettingsKeys.ReaderFontSize] ?: 22).coerceIn(16, 40),
                readerLineHeight = (preferences[SettingsKeys.ReaderLineHeight] ?: "1.65").toDoubleOrNull()
                    ?.coerceIn(1.0, 2.5) ?: 1.65,
                readerHorizontalPadding = (preferences[SettingsKeys.ReaderHorizontalPadding] ?: 5).coerceIn(0, 50),
                readerVerticalPadding = (preferences[SettingsKeys.ReaderVerticalPadding] ?: 0).coerceIn(0, 50),
                readerAvoidPageBreak = preferences[SettingsKeys.ReaderAvoidPageBreak] ?: false,
                readerJustifyText = preferences[SettingsKeys.ReaderJustifyText] ?: false,
                readerLayoutAdvanced = preferences[SettingsKeys.ReaderLayoutAdvanced] ?: false,
                readerCharacterSpacing = (preferences[SettingsKeys.ReaderCharacterSpacing] ?: "0.0").toDoubleOrNull()
                    ?.coerceIn(-10.0, 10.0) ?: 0.0,
                readerContinuousMode = preferences[SettingsKeys.ReaderContinuousMode] ?: false,
                readerHideFurigana = preferences[SettingsKeys.ReaderHideFurigana] ?: false,
                readerFullscreen = preferences[SettingsKeys.ReaderFullscreen] ?: false,
                popupWidth = (preferences[SettingsKeys.PopupWidth] ?: 320).coerceIn(100, 700),
                popupHeight = (preferences[SettingsKeys.PopupHeight] ?: 250).coerceIn(100, 500),
                popupFullWidth = preferences[SettingsKeys.PopupFullWidth] ?: false,
                popupSwipeToDismiss = preferences[SettingsKeys.PopupSwipeToDismiss] ?: false,
                popupSwipeThreshold = (preferences[SettingsKeys.PopupSwipeThreshold] ?: 40).coerceIn(20, 80),
                collapseDictionaries = preferences[SettingsKeys.CollapseDictionaries] ?: false,
                compactGlossaries = preferences[SettingsKeys.CompactGlossaries] ?: true,
                showExpressionTags = preferences[SettingsKeys.ShowExpressionTags] ?: false,
                harmonicFrequency = preferences[SettingsKeys.HarmonicFrequency] ?: false,
                deduplicatePitchAccents = preferences[SettingsKeys.DeduplicatePitchAccents] ?: false,
                audioSources = audioSources,
                enableLocalAudio = enableLocalAudio,
                audioEnableAutoplay = preferences[SettingsKeys.AudioEnableAutoplay] ?: false,
                audioPlaybackMode = preferences[SettingsKeys.AudioPlaybackMode]?.toAudioPlaybackMode()
                    ?: AudioPlaybackMode.Duck,
                localAudioDatabaseSizeBytes = localAudioDatabaseSizeBytes,
                anki = preferences[SettingsKeys.AnkiSettings]?.toAnkiSettings(json) ?: AnkiSettings(),
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ThemeMode] = mode.name
        }
    }

    suspend fun setLanguageMode(mode: LanguageMode) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.LanguageMode] = mode.name
        }
    }


    suspend fun setBookshelfSortMode(mode: BookshelfSortMode) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.BookshelfSortMode] = mode.wireName
        }
    }

    suspend fun setReaderThemeMode(mode: ReaderThemeMode) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderThemeMode] = mode.name
        }
    }

    suspend fun setBlurEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.BlurEnabled] = enabled
        }
    }

    suspend fun setMaxResults(value: Int) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.MaxResults] = value.coerceIn(1, 50)
        }
    }

    suspend fun setScanLength(value: Int) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ScanLength] = value.coerceIn(1, 64)
        }
    }

    suspend fun setReaderFontSize(value: Int) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderFontSize] = value.coerceIn(16, 40)
        }
    }

    suspend fun setReaderLineHeight(value: Double) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderLineHeight] = value.coerceIn(1.0, 2.5).toString()
        }
    }

    suspend fun setReaderHorizontalPadding(value: Int) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderHorizontalPadding] = value.coerceIn(0, 50)
        }
    }

    suspend fun setReaderVerticalPadding(value: Int) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderVerticalPadding] = value.coerceIn(0, 50)
        }
    }

    suspend fun setReaderAvoidPageBreak(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderAvoidPageBreak] = enabled
        }
    }

    suspend fun setReaderJustifyText(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderJustifyText] = enabled
        }
    }

    suspend fun setReaderLayoutAdvanced(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderLayoutAdvanced] = enabled
        }
    }

    suspend fun setReaderCharacterSpacing(value: Double) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderCharacterSpacing] = value.coerceIn(-10.0, 10.0).toString()
        }
    }

    suspend fun setReaderContinuousMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderContinuousMode] = enabled
        }
    }

    suspend fun setReaderHideFurigana(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderHideFurigana] = enabled
        }
    }

    suspend fun setReaderFullscreen(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ReaderFullscreen] = enabled
        }
    }

    suspend fun setPopupWidth(value: Int) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.PopupWidth] = value.coerceIn(100, 700)
        }
    }

    suspend fun setPopupHeight(value: Int) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.PopupHeight] = value.coerceIn(100, 500)
        }
    }

    suspend fun setPopupFullWidth(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.PopupFullWidth] = enabled
        }
    }

    suspend fun setPopupSwipeToDismiss(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.PopupSwipeToDismiss] = enabled
        }
    }

    suspend fun setPopupSwipeThreshold(value: Int) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.PopupSwipeThreshold] = value.coerceIn(20, 80)
        }
    }

    suspend fun setCollapseDictionaries(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.CollapseDictionaries] = enabled
        }
    }

    suspend fun setCompactGlossaries(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.CompactGlossaries] = enabled
        }
    }

    suspend fun setShowExpressionTags(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.ShowExpressionTags] = enabled
        }
    }

    suspend fun setHarmonicFrequency(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.HarmonicFrequency] = enabled
        }
    }

    suspend fun setDeduplicatePitchAccents(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.DeduplicatePitchAccents] = enabled
        }
    }

    suspend fun setAudioSources(sources: List<AudioSource>) {
        dataStore.edit { preferences ->
            val enableLocalAudio = (preferences[SettingsKeys.EnableLocalAudio] ?: false) &&
                (preferences[SettingsKeys.LocalAudioDatabaseSizeBytes] ?: 0L) > 0L
            val hasLocalAudioDatabase = (preferences[SettingsKeys.LocalAudioDatabaseSizeBytes] ?: 0L) > 0L
            preferences[SettingsKeys.AudioSources] =
                normalizeAudioSources(sources, enableLocalAudio, hasLocalAudioDatabase).toAudioSourcesJson(json)
        }
    }

    suspend fun setEnableLocalAudio(enabled: Boolean) {
        dataStore.edit { preferences ->
            val effectiveEnabled = enabled && (preferences[SettingsKeys.LocalAudioDatabaseSizeBytes] ?: 0L) > 0L
            val hasLocalAudioDatabase = (preferences[SettingsKeys.LocalAudioDatabaseSizeBytes] ?: 0L) > 0L
            preferences[SettingsKeys.EnableLocalAudio] = effectiveEnabled
            val sources = preferences[SettingsKeys.AudioSources]?.toAudioSources(json) ?: listOf(AudioSource.Default)
            preferences[SettingsKeys.AudioSources] =
                normalizeAudioSources(sources, effectiveEnabled, hasLocalAudioDatabase).toAudioSourcesJson(json)
        }
    }

    suspend fun setAudioEnableAutoplay(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.AudioEnableAutoplay] = enabled
        }
    }

    suspend fun setAudioPlaybackMode(mode: AudioPlaybackMode) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.AudioPlaybackMode] = mode.wireName
        }
    }

    suspend fun setLocalAudioDatabaseSizeBytes(sizeBytes: Long) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.LocalAudioDatabaseSizeBytes] = sizeBytes.coerceAtLeast(0L)
            val enableLocalAudio = (preferences[SettingsKeys.EnableLocalAudio] ?: false) && sizeBytes > 0L
            preferences[SettingsKeys.EnableLocalAudio] = enableLocalAudio
            val sources = preferences[SettingsKeys.AudioSources]?.toAudioSources(json) ?: listOf(AudioSource.Default)
            preferences[SettingsKeys.AudioSources] =
                normalizeAudioSources(
                    sources = sources,
                    enableLocalAudio = enableLocalAudio,
                    hasLocalAudioDatabase = sizeBytes > 0L,
                ).toAudioSourcesJson(json)
        }
    }

    suspend fun setAnkiSettings(settings: AnkiSettings) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.AnkiSettings] = json.encodeToString(AnkiSettings.serializer(), settings)
        }
    }

    suspend fun updateAnkiSettings(block: (AnkiSettings) -> AnkiSettings) {
        dataStore.edit { preferences ->
            val current = preferences[SettingsKeys.AnkiSettings]?.toAnkiSettings(json) ?: AnkiSettings()
            preferences[SettingsKeys.AnkiSettings] = json.encodeToString(AnkiSettings.serializer(), block(current))
        }
    }
}

private object SettingsKeys {
    val BookshelfSortMode = stringPreferencesKey("bookshelf_sort_mode")
    val ThemeMode = stringPreferencesKey("theme_mode")
    val LanguageMode = stringPreferencesKey("language_mode")

    val ReaderThemeMode = stringPreferencesKey("reader_theme_mode")
    val BlurEnabled = booleanPreferencesKey("blur_enabled")
    val MaxResults = intPreferencesKey("dictionary_max_results")
    val ScanLength = intPreferencesKey("dictionary_scan_length")
    val ReaderFontSize = intPreferencesKey("reader_font_size")
    val ReaderLineHeight = stringPreferencesKey("reader_line_height")
    val ReaderHorizontalPadding = intPreferencesKey("reader_horizontal_padding")
    val ReaderVerticalPadding = intPreferencesKey("reader_vertical_padding")
    val ReaderAvoidPageBreak = booleanPreferencesKey("reader_avoid_page_break")
    val ReaderJustifyText = booleanPreferencesKey("reader_justify_text")
    val ReaderLayoutAdvanced = booleanPreferencesKey("reader_layout_advanced")
    val ReaderCharacterSpacing = stringPreferencesKey("reader_character_spacing")
    val ReaderContinuousMode = booleanPreferencesKey("reader_continuous_mode")
    val ReaderHideFurigana = booleanPreferencesKey("reader_hide_furigana")
    val ReaderFullscreen = booleanPreferencesKey("reader_fullscreen")
    val PopupWidth = intPreferencesKey("popup_width")
    val PopupHeight = intPreferencesKey("popup_height")
    val PopupFullWidth = booleanPreferencesKey("popup_full_width")
    val PopupSwipeToDismiss = booleanPreferencesKey("popup_swipe_to_dismiss")
    val PopupSwipeThreshold = intPreferencesKey("popup_swipe_threshold")
    val CollapseDictionaries = booleanPreferencesKey("dictionary_collapse_dictionaries")
    val CompactGlossaries = booleanPreferencesKey("dictionary_compact_glossaries")
    val ShowExpressionTags = booleanPreferencesKey("dictionary_show_expression_tags")
    val HarmonicFrequency = booleanPreferencesKey("dictionary_harmonic_frequency")
    val DeduplicatePitchAccents = booleanPreferencesKey("dictionary_deduplicate_pitch_accents")
    val AudioSources = stringPreferencesKey("audio_sources")
    val EnableLocalAudio = booleanPreferencesKey("audio_enable_local")
    val AudioEnableAutoplay = booleanPreferencesKey("audio_enable_autoplay")
    val AudioPlaybackMode = stringPreferencesKey("audio_playback_mode")
    val LocalAudioDatabaseSizeBytes = longPreferencesKey("audio_local_database_size_bytes")
    val AnkiSettings = stringPreferencesKey("anki_settings")
}

private fun String.toThemeMode(): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.System

private fun String.toLanguageMode(): LanguageMode =
    LanguageMode.entries.firstOrNull { it.name == this } ?: LanguageMode.System

private fun String.toBookshelfSortMode(): BookshelfSortMode =
    BookshelfSortMode.entries.firstOrNull { it.wireName == this || it.name == this }
        ?: BookshelfSortMode.Recent

private fun String.toReaderThemeMode(): ReaderThemeMode =
    ReaderThemeMode.entries.firstOrNull { it.name == this } ?: ReaderThemeMode.FollowApp

private fun String.toAudioPlaybackMode(): AudioPlaybackMode =
    AudioPlaybackMode.entries.firstOrNull { it.wireName == this || it.name == this }
        ?: AudioPlaybackMode.Duck

private fun String.toAudioSources(json: Json): List<AudioSource> =
    runCatching {
        json.decodeFromString(ListSerializer(AudioSource.serializer()), this)
    }.getOrDefault(listOf(AudioSource.Default))

private fun String.toAnkiSettings(json: Json): AnkiSettings =
    runCatching {
        json.decodeFromString(AnkiSettings.serializer(), this)
    }.getOrDefault(AnkiSettings())

private fun List<AudioSource>.toAudioSourcesJson(json: Json): String =
    json.encodeToString(ListSerializer(AudioSource.serializer()), this)

private fun normalizeAudioSources(
    sources: List<AudioSource>,
    enableLocalAudio: Boolean,
    hasLocalAudioDatabase: Boolean,
): List<AudioSource> {
    val withoutLocal = sources.filterNot { it.isLocal }
    val withDefault = if (withoutLocal.any { it.isDefault }) {
        withoutLocal
    } else {
        listOf(AudioSource.Default) + withoutLocal.filterNot { it.url == AudioSource.Default.url }
    }
    val local = AudioSource.Local.copy(isEnabled = enableLocalAudio && hasLocalAudioDatabase)
    return (listOf(local) + withDefault).distinctBy { it.url }
}
