package app.mori.reader.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.mori.reader.data.audiobook.AudiobookStorageMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okio.IOException

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    val settings: Flow<AppSettings> =
        dataStore.data
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }.map { preferences ->
                val localAudioDatabaseSizeBytes = preferences[Keys.LocalAudioDatabaseSizeBytes] ?: 0L
                val enableLocalAudio =
                    (preferences[Keys.EnableLocalAudio] ?: false) &&
                        localAudioDatabaseSizeBytes > 0L
                val audioSources =
                    normalizeAudioSources(
                        sources =
                            preferences[Keys.AudioSources]?.toAudioSources(json)
                                ?: listOf(AudioSource.Default),
                        enableLocalAudio = enableLocalAudio,
                        hasLocalAudioDatabase = localAudioDatabaseSizeBytes > 0L,
                    )
                AppSettings(
                    bookshelf =
                        BookshelfSettings(
                            sortMode =
                                preferences[Keys.BookshelfSortMode]?.toBookshelfSortMode()
                                    ?: BookshelfSortMode.Recent,
                        ),
                    appearance =
                        AppearanceSettings(
                            themeMode = preferences[Keys.ThemeMode]?.toThemeMode() ?: ThemeMode.System,
                            languageMode =
                                preferences[Keys.LanguageMode]?.toLanguageMode()
                                    ?: LanguageMode.System,
                            readerThemeMode =
                                preferences[Keys.ReaderThemeMode]?.toReaderThemeMode()
                                    ?: ReaderThemeMode.FollowApp,
                            blurEnabled = preferences[Keys.BlurEnabled] ?: true,
                            readerFullscreen = preferences[Keys.ReaderFullscreen] ?: false,
                        ),
                    reader =
                        ReaderSettings(
                            verticalWriting = preferences[Keys.ReaderVerticalWriting] ?: true,
                            fontSize = (preferences[Keys.ReaderFontSize] ?: 22).coerceIn(16, 40),
                            lineHeight =
                                (preferences[Keys.ReaderLineHeight] ?: "1.65")
                                    .toDoubleOrNull()
                                    ?.coerceIn(1.0, 2.5) ?: 1.65,
                            horizontalPadding = (preferences[Keys.ReaderHorizontalPadding] ?: 5).coerceIn(0, 50),
                            verticalPadding = (preferences[Keys.ReaderVerticalPadding] ?: 0).coerceIn(0, 50),
                            avoidPageBreak = preferences[Keys.ReaderAvoidPageBreak] ?: false,
                            justifyText = preferences[Keys.ReaderJustifyText] ?: false,
                            layoutAdvanced = preferences[Keys.ReaderLayoutAdvanced] ?: false,
                            characterSpacing =
                                (preferences[Keys.ReaderCharacterSpacing] ?: "0.0")
                                    .toDoubleOrNull()
                                    ?.coerceIn(-10.0, 10.0) ?: 0.0,
                            continuousMode = preferences[Keys.ReaderContinuousMode] ?: false,
                            hideFurigana = preferences[Keys.ReaderHideFurigana] ?: false,
                        ),
                    popup =
                        PopupSettings(
                            width = (preferences[Keys.PopupWidth] ?: 320).coerceIn(100, 700),
                            height = (preferences[Keys.PopupHeight] ?: 250).coerceIn(100, 500),
                            fullWidth = preferences[Keys.PopupFullWidth] ?: false,
                            swipeToDismiss = preferences[Keys.PopupSwipeToDismiss] ?: false,
                            swipeThreshold = (preferences[Keys.PopupSwipeThreshold] ?: 40).coerceIn(20, 80),
                        ),
                    dictionary =
                        DictionarySettings(
                            maxResults = (preferences[Keys.MaxResults] ?: 16).coerceIn(1, 50),
                            scanLength = (preferences[Keys.ScanLength] ?: 16).coerceIn(1, 64),
                            collapseDictionaries = preferences[Keys.CollapseDictionaries] ?: false,
                            compactGlossaries = preferences[Keys.CompactGlossaries] ?: true,
                            showExpressionTags = preferences[Keys.ShowExpressionTags] ?: false,
                            harmonicFrequency = preferences[Keys.HarmonicFrequency] ?: false,
                            deduplicatePitchAccents = preferences[Keys.DeduplicatePitchAccents] ?: false,
                        ),
                    audio =
                        AudioSettings(
                            sources = audioSources,
                            enableLocalAudio = enableLocalAudio,
                            enableAutoplay = preferences[Keys.AudioEnableAutoplay] ?: false,
                            playbackMode =
                                preferences[Keys.AudioPlaybackMode]?.toAudioPlaybackMode()
                                    ?: AudioPlaybackMode.Duck,
                            localAudioDatabaseSizeBytes = localAudioDatabaseSizeBytes,
                        ),
                    sasayaki =
                        SasayakiSettings(
                            preferredStorageMode =
                                preferences[Keys.PreferredAudiobookStorageMode]
                                    ?.toAudiobookStorageMode() ?: AudiobookStorageMode.Copy,
                            syncEnabled = preferences[Keys.SasayakiSyncEnabled] ?: false,
                            autoScroll = preferences[Keys.SasayakiAutoScroll] ?: true,
                            autoPauseOnLookup = preferences[Keys.SasayakiAutoPauseOnLookup] ?: true,
                            highlightEnabled = preferences[Keys.SasayakiHighlightEnabled] ?: true,
                            highlightColor =
                                preferences[Keys.SasayakiHighlightColor]
                                    ?.takeIf { it.matches(Regex("^#[0-9A-Fa-f]{8}$")) }
                                    ?: "#FFC0485C",
                        ),
                )
            }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.ThemeMode] = mode.name }
    }

    suspend fun setLanguageMode(mode: LanguageMode) {
        dataStore.edit { it[Keys.LanguageMode] = mode.name }
    }

    suspend fun setBookshelfSortMode(mode: BookshelfSortMode) {
        dataStore.edit { it[Keys.BookshelfSortMode] = mode.wireName }
    }

    suspend fun setReaderThemeMode(mode: ReaderThemeMode) {
        dataStore.edit { it[Keys.ReaderThemeMode] = mode.name }
    }

    suspend fun setBlurEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BlurEnabled] = enabled }
    }

    suspend fun setMaxResults(value: Int) {
        dataStore.edit { it[Keys.MaxResults] = value.coerceIn(1, 50) }
    }

    suspend fun setScanLength(value: Int) {
        dataStore.edit { it[Keys.ScanLength] = value.coerceIn(1, 64) }
    }

    suspend fun setReaderFontSize(value: Int) {
        dataStore.edit { it[Keys.ReaderFontSize] = value.coerceIn(16, 40) }
    }

    suspend fun setReaderVerticalWriting(enabled: Boolean) {
        dataStore.edit { it[Keys.ReaderVerticalWriting] = enabled }
    }

    suspend fun setReaderLineHeight(value: Double) {
        dataStore.edit { it[Keys.ReaderLineHeight] = value.coerceIn(1.0, 2.5).toString() }
    }

    suspend fun setReaderHorizontalPadding(value: Int) {
        dataStore.edit { it[Keys.ReaderHorizontalPadding] = value.coerceIn(0, 50) }
    }

    suspend fun setReaderVerticalPadding(value: Int) {
        dataStore.edit { it[Keys.ReaderVerticalPadding] = value.coerceIn(0, 50) }
    }

    suspend fun setReaderAvoidPageBreak(enabled: Boolean) {
        dataStore.edit { it[Keys.ReaderAvoidPageBreak] = enabled }
    }

    suspend fun setReaderJustifyText(enabled: Boolean) {
        dataStore.edit { it[Keys.ReaderJustifyText] = enabled }
    }

    suspend fun setReaderLayoutAdvanced(enabled: Boolean) {
        dataStore.edit { it[Keys.ReaderLayoutAdvanced] = enabled }
    }

    suspend fun setReaderCharacterSpacing(value: Double) {
        dataStore.edit { it[Keys.ReaderCharacterSpacing] = value.coerceIn(-10.0, 10.0).toString() }
    }

    suspend fun setReaderContinuousMode(enabled: Boolean) {
        dataStore.edit { it[Keys.ReaderContinuousMode] = enabled }
    }

    suspend fun setReaderHideFurigana(enabled: Boolean) {
        dataStore.edit { it[Keys.ReaderHideFurigana] = enabled }
    }

    suspend fun setReaderFullscreen(enabled: Boolean) {
        dataStore.edit { it[Keys.ReaderFullscreen] = enabled }
    }

    suspend fun setPopupWidth(value: Int) {
        dataStore.edit { it[Keys.PopupWidth] = value.coerceIn(100, 700) }
    }

    suspend fun setPopupHeight(value: Int) {
        dataStore.edit { it[Keys.PopupHeight] = value.coerceIn(100, 500) }
    }

    suspend fun setPopupFullWidth(enabled: Boolean) {
        dataStore.edit { it[Keys.PopupFullWidth] = enabled }
    }

    suspend fun setPopupSwipeToDismiss(enabled: Boolean) {
        dataStore.edit { it[Keys.PopupSwipeToDismiss] = enabled }
    }

    suspend fun setPopupSwipeThreshold(value: Int) {
        dataStore.edit { it[Keys.PopupSwipeThreshold] = value.coerceIn(20, 80) }
    }

    suspend fun setCollapseDictionaries(enabled: Boolean) {
        dataStore.edit { it[Keys.CollapseDictionaries] = enabled }
    }

    suspend fun setCompactGlossaries(enabled: Boolean) {
        dataStore.edit { it[Keys.CompactGlossaries] = enabled }
    }

    suspend fun setShowExpressionTags(enabled: Boolean) {
        dataStore.edit { it[Keys.ShowExpressionTags] = enabled }
    }

    suspend fun setHarmonicFrequency(enabled: Boolean) {
        dataStore.edit { it[Keys.HarmonicFrequency] = enabled }
    }

    suspend fun setDeduplicatePitchAccents(enabled: Boolean) {
        dataStore.edit { it[Keys.DeduplicatePitchAccents] = enabled }
    }

    suspend fun setAudioSources(sources: List<AudioSource>) {
        dataStore.edit { preferences ->
            val enableLocalAudio =
                (preferences[Keys.EnableLocalAudio] ?: false) &&
                    (preferences[Keys.LocalAudioDatabaseSizeBytes] ?: 0L) > 0L
            val hasLocalAudioDatabase = (preferences[Keys.LocalAudioDatabaseSizeBytes] ?: 0L) > 0L
            preferences[Keys.AudioSources] =
                normalizeAudioSources(sources, enableLocalAudio, hasLocalAudioDatabase).toAudioSourcesJson(json)
        }
    }

    suspend fun setEnableLocalAudio(enabled: Boolean) {
        dataStore.edit { preferences ->
            val effectiveEnabled = enabled && (preferences[Keys.LocalAudioDatabaseSizeBytes] ?: 0L) > 0L
            val hasLocalAudioDatabase = (preferences[Keys.LocalAudioDatabaseSizeBytes] ?: 0L) > 0L
            preferences[Keys.EnableLocalAudio] = effectiveEnabled
            val sources = preferences[Keys.AudioSources]?.toAudioSources(json) ?: listOf(AudioSource.Default)
            preferences[Keys.AudioSources] =
                normalizeAudioSources(sources, effectiveEnabled, hasLocalAudioDatabase).toAudioSourcesJson(json)
        }
    }

    suspend fun setAudioEnableAutoplay(enabled: Boolean) {
        dataStore.edit { it[Keys.AudioEnableAutoplay] = enabled }
    }

    suspend fun setAudioPlaybackMode(mode: AudioPlaybackMode) {
        dataStore.edit { it[Keys.AudioPlaybackMode] = mode.wireName }
    }

    suspend fun setLocalAudioDatabaseSizeBytes(sizeBytes: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.LocalAudioDatabaseSizeBytes] = sizeBytes.coerceAtLeast(0L)
            val enableLocalAudio = (preferences[Keys.EnableLocalAudio] ?: false) && sizeBytes > 0L
            preferences[Keys.EnableLocalAudio] = enableLocalAudio
            val sources = preferences[Keys.AudioSources]?.toAudioSources(json) ?: listOf(AudioSource.Default)
            preferences[Keys.AudioSources] =
                normalizeAudioSources(
                    sources = sources,
                    enableLocalAudio = enableLocalAudio,
                    hasLocalAudioDatabase = sizeBytes > 0L,
                ).toAudioSourcesJson(json)
        }
    }

    suspend fun setPreferredAudiobookStorageMode(mode: AudiobookStorageMode) {
        dataStore.edit { it[Keys.PreferredAudiobookStorageMode] = mode.wireName }
    }

    suspend fun setSasayakiSyncEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SasayakiSyncEnabled] = enabled }
    }

    suspend fun setSasayakiAutoScroll(enabled: Boolean) {
        dataStore.edit { it[Keys.SasayakiAutoScroll] = enabled }
    }

    suspend fun setSasayakiAutoPauseOnLookup(enabled: Boolean) {
        dataStore.edit { it[Keys.SasayakiAutoPauseOnLookup] = enabled }
    }

    suspend fun setSasayakiHighlightEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SasayakiHighlightEnabled] = enabled }
    }

    suspend fun setSasayakiHighlightColor(value: String) {
        val normalized = value.takeIf { it.matches(Regex("^#[0-9A-Fa-f]{8}$")) } ?: "#FFC0485C"
        dataStore.edit { it[Keys.SasayakiHighlightColor] = normalized }
    }
}

private object Keys {
    val BookshelfSortMode = stringPreferencesKey("bookshelf_sort_mode")
    val ThemeMode = stringPreferencesKey("theme_mode")
    val LanguageMode = stringPreferencesKey("language_mode")
    val ReaderThemeMode = stringPreferencesKey("reader_theme_mode")
    val BlurEnabled = booleanPreferencesKey("blur_enabled")
    val MaxResults = intPreferencesKey("dictionary_max_results")
    val ScanLength = intPreferencesKey("dictionary_scan_length")
    val ReaderVerticalWriting = booleanPreferencesKey("reader_vertical_writing")
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
    val PreferredAudiobookStorageMode = stringPreferencesKey("audiobook_preferred_storage_mode")
    val SasayakiSyncEnabled = booleanPreferencesKey("sasayaki_sync_enabled")
    val SasayakiAutoScroll = booleanPreferencesKey("sasayaki_auto_scroll")
    val SasayakiAutoPauseOnLookup = booleanPreferencesKey("sasayaki_auto_pause_lookup")
    val SasayakiHighlightEnabled = booleanPreferencesKey("sasayaki_highlight_enabled")
    val SasayakiHighlightColor = stringPreferencesKey("sasayaki_highlight_color")
}

private fun String.toThemeMode(): ThemeMode = ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.System

private fun String.toLanguageMode(): LanguageMode = LanguageMode.entries.firstOrNull { it.name == this } ?: LanguageMode.System

private fun String.toBookshelfSortMode(): BookshelfSortMode =
    BookshelfSortMode.entries.firstOrNull { it.wireName == this || it.name == this }
        ?: BookshelfSortMode.Recent

private fun String.toReaderThemeMode(): ReaderThemeMode =
    ReaderThemeMode.entries.firstOrNull { it.name == this } ?: ReaderThemeMode.FollowApp

private fun String.toAudioPlaybackMode(): AudioPlaybackMode =
    AudioPlaybackMode.entries.firstOrNull { it.wireName == this || it.name == this }
        ?: AudioPlaybackMode.Duck

private fun String.toAudiobookStorageMode(): AudiobookStorageMode =
    AudiobookStorageMode.entries.firstOrNull { it.wireName == this || it.name == this }
        ?: AudiobookStorageMode.Copy

private fun String.toAudioSources(json: Json): List<AudioSource> =
    runCatching {
        json.decodeFromString(ListSerializer(AudioSource.serializer()), this)
    }.getOrDefault(listOf(AudioSource.Default))

private fun List<AudioSource>.toAudioSourcesJson(json: Json): String = json.encodeToString(ListSerializer(AudioSource.serializer()), this)

private fun normalizeAudioSources(
    sources: List<AudioSource>,
    enableLocalAudio: Boolean,
    hasLocalAudioDatabase: Boolean,
): List<AudioSource> {
    val withoutLocal = sources.filterNot { it.isLocal }
    val withDefault =
        if (withoutLocal.any { it.isDefault }) {
            withoutLocal
        } else {
            listOf(AudioSource.Default) + withoutLocal.filterNot { it.url == AudioSource.Default.url }
        }
    val local = AudioSource.Local.copy(isEnabled = enableLocalAudio && hasLocalAudioDatabase)
    return (listOf(local) + withDefault).distinctBy { it.url }
}
