package app.mori.reader.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.mori.reader.data.anki.AnkiConnectConfig
import app.mori.reader.data.anki.AnkiConnectionMode
import app.mori.reader.data.anki.AnkiDuplicateScope
import app.mori.reader.data.anki.AnkiSettings
import app.mori.reader.data.anki.AnkiSettingsRepository
import app.mori.reader.data.audiobook.AudiobookStorageMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okio.IOException

class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : AnkiSettingsRepository {
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
                val localAudioDatabaseSizeBytes =
                    preferences[Keys.LocalAudioDatabaseSizeBytes] ?: 0L
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
                            themeMode =
                                preferences[Keys.ThemeMode]?.toThemeMode()
                                    ?: ThemeMode.System,
                            uiThemeEngine =
                                preferences[Keys.UiThemeEngine]?.toUiThemeEngine()
                                    ?: UiThemeEngine.Miuix,
                            uiScalePercent =
                                normalizeUiScalePercent(
                                    preferences[Keys.UiScalePercent] ?: 100,
                                ),
                            languageMode =
                                preferences[Keys.LanguageMode]?.toLanguageMode()
                                    ?: LanguageMode.System,
                            readerThemeMode =
                                preferences[Keys.ReaderThemeMode]?.toReaderThemeMode()
                                    ?: ReaderThemeMode.FollowApp,
                            monetEnabled = preferences[Keys.MonetEnabled] ?: false,
                            monetKeyColor = preferences[Keys.MonetKeyColor] ?: 0L,
                            materialEInkMode = preferences[Keys.MaterialEInkMode] ?: false,
                            blurEnabled = preferences[Keys.BlurEnabled] ?: true,
                        ),
                    reader =
                        ReaderSettings(
                            fullscreen = preferences[Keys.ReaderFullscreen] ?: true,
                            actionBarPinned = preferences[Keys.ReaderActionBarPinned] ?: false,
                            showReadingInfo = preferences[Keys.ReaderShowReadingInfo] ?: true,
                            verticalWriting = preferences[Keys.ReaderVerticalWriting] ?: true,
                            fontFamily = preferences[Keys.ReaderFontFamily],
                            fontSize = (preferences[Keys.ReaderFontSize] ?: 22).coerceIn(16, 40),
                            lineHeight =
                                (preferences[Keys.ReaderLineHeight] ?: "1.65")
                                    .toDoubleOrNull()
                                    ?.coerceIn(1.0, 2.5) ?: 1.65,
                            horizontalPadding =
                                (
                                    preferences[Keys.ReaderHorizontalPadding]
                                        ?: 5
                                ).coerceIn(0, 50),
                            verticalPadding =
                                (
                                    preferences[Keys.ReaderVerticalPadding]
                                        ?: 0
                                ).coerceIn(0, 50),
                            avoidPageBreak = preferences[Keys.ReaderAvoidPageBreak] ?: false,
                            justifyText = preferences[Keys.ReaderJustifyText] ?: false,
                            layoutAdvanced = preferences[Keys.ReaderLayoutAdvanced] ?: false,
                            characterSpacing =
                                (preferences[Keys.ReaderCharacterSpacing] ?: "0.0")
                                    .toDoubleOrNull()
                                    ?.coerceIn(-10.0, 10.0) ?: 0.0,
                            continuousMode = preferences[Keys.ReaderContinuousMode] ?: false,
                            hideFurigana = preferences[Keys.ReaderHideFurigana] ?: false,
                            popupWidth = (preferences[Keys.PopupWidth] ?: 320).coerceIn(100, 700),
                            popupHeight = (preferences[Keys.PopupHeight] ?: 250).coerceIn(100, 500),
                            popupFullWidth = preferences[Keys.PopupFullWidth] ?: false,
                            popupSwipeToDismiss = preferences[Keys.PopupSwipeToDismiss] ?: false,
                            popupSwipeThreshold =
                                (preferences[Keys.PopupSwipeThreshold] ?: 40).coerceIn(20, 80),
                        ),
                    readerPersonalizedSchemes =
                        preferences[Keys.ReaderPersonalizedSchemes]
                            ?.toReaderPersonalizedSchemes(json)
                            .orEmpty(),
                    dictionary =
                        DictionarySettings(
                            maxResults = (preferences[Keys.MaxResults] ?: 16).coerceIn(1, 50),
                            scanLength = (preferences[Keys.ScanLength] ?: 16).coerceIn(1, 64),
                            collapseDictionaries = preferences[Keys.CollapseDictionaries] ?: false,
                            compactGlossaries = preferences[Keys.CompactGlossaries] ?: true,
                            showExpressionTags = preferences[Keys.ShowExpressionTags] ?: false,
                            harmonicFrequency = preferences[Keys.HarmonicFrequency] ?: false,
                            deduplicatePitchAccents =
                                preferences[Keys.DeduplicatePitchAccents]
                                    ?: false,
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
                    anki = preferences[Keys.AnkiSettings]?.toAnkiSettings(json) ?: AnkiSettings(),
                )
            }

    override val settingsFlow: Flow<AnkiSettings> = settings.map { it.anki }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.ThemeMode] = mode.name }
    }

    suspend fun setUiThemeEngine(engine: UiThemeEngine) {
        dataStore.edit { it[Keys.UiThemeEngine] = engine.name }
    }

    suspend fun setUiScalePercent(value: Int) {
        dataStore.edit { it[Keys.UiScalePercent] = normalizeUiScalePercent(value) }
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

    suspend fun setMonetEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MonetEnabled] = enabled }
    }

    suspend fun setMonetKeyColor(value: Long) {
        dataStore.edit { it[Keys.MonetKeyColor] = value.coerceAtLeast(0L) }
    }

    suspend fun setMaterialEInkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.MaterialEInkMode] = enabled }
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

    suspend fun setReaderSettings(settings: ReaderSettings) {
        dataStore.edit {
            it[Keys.ReaderFullscreen] = settings.fullscreen
            it[Keys.ReaderActionBarPinned] = settings.actionBarPinned
            it[Keys.ReaderShowReadingInfo] = settings.showReadingInfo
            it[Keys.ReaderVerticalWriting] = settings.verticalWriting
            settings.fontFamily
                ?.trim()
                ?.takeIf { fontFamily -> fontFamily.isNotBlank() }
                ?.let { fontFamily -> it[Keys.ReaderFontFamily] = fontFamily }
                ?: it.remove(Keys.ReaderFontFamily)
            it[Keys.ReaderFontSize] = settings.fontSize.coerceIn(16, 40)
            it[Keys.ReaderLineHeight] = settings.lineHeight.coerceIn(1.0, 2.5).toString()
            it[Keys.ReaderHorizontalPadding] = settings.horizontalPadding.coerceIn(0, 50)
            it[Keys.ReaderVerticalPadding] = settings.verticalPadding.coerceIn(0, 50)
            it[Keys.ReaderAvoidPageBreak] = settings.avoidPageBreak
            it[Keys.ReaderJustifyText] = settings.justifyText
            it[Keys.ReaderLayoutAdvanced] = settings.layoutAdvanced
            it[Keys.ReaderCharacterSpacing] = settings.characterSpacing.coerceIn(-10.0, 10.0).toString()
            it[Keys.ReaderContinuousMode] = settings.continuousMode
            it[Keys.ReaderHideFurigana] = settings.hideFurigana
            it[Keys.PopupWidth] = settings.popupWidth.coerceIn(100, 700)
            it[Keys.PopupHeight] = settings.popupHeight.coerceIn(100, 500)
            it[Keys.PopupFullWidth] = settings.popupFullWidth
            it[Keys.PopupSwipeToDismiss] = settings.popupSwipeToDismiss
            it[Keys.PopupSwipeThreshold] = settings.popupSwipeThreshold.coerceIn(20, 80)
        }
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

    suspend fun setReaderActionBarPinned(enabled: Boolean) {
        dataStore.edit { it[Keys.ReaderActionBarPinned] = enabled }
    }

    suspend fun setReaderShowReadingInfo(enabled: Boolean) {
        dataStore.edit { it[Keys.ReaderShowReadingInfo] = enabled }
    }

    suspend fun createReaderPersonalizedScheme(scheme: ReaderPersonalizedScheme): ReaderPersonalizedScheme {
        val normalizedScheme =
            scheme.copy(
                name = scheme.name.trim().ifBlank { "未命名方案" },
            )
        dataStore.edit { preferences ->
            val current = preferences[Keys.ReaderPersonalizedSchemes]?.toReaderPersonalizedSchemes(json).orEmpty()
            preferences[Keys.ReaderPersonalizedSchemes] = (current + normalizedScheme).toReaderPersonalizedSchemesJson(json)
        }
        return normalizedScheme
    }

    suspend fun renameReaderPersonalizedScheme(
        schemeId: String,
        name: String,
    ) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.ReaderPersonalizedSchemes]?.toReaderPersonalizedSchemes(json).orEmpty()
            preferences[Keys.ReaderPersonalizedSchemes] =
                current
                    .map { scheme ->
                        if (scheme.id == schemeId) {
                            scheme.copy(name = name.trim().ifBlank { "未命名方案" })
                        } else {
                            scheme
                        }
                    }.toReaderPersonalizedSchemesJson(json)
        }
    }

    suspend fun updateReaderPersonalizedSchemeSettings(
        schemeId: String,
        settings: ReaderSettings,
    ) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.ReaderPersonalizedSchemes]?.toReaderPersonalizedSchemes(json).orEmpty()
            preferences[Keys.ReaderPersonalizedSchemes] =
                current
                    .map { scheme ->
                        if (scheme.id == schemeId) {
                            scheme.copy(settings = settings)
                        } else {
                            scheme
                        }
                    }.toReaderPersonalizedSchemesJson(json)
        }
    }

    suspend fun deleteReaderPersonalizedScheme(schemeId: String) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.ReaderPersonalizedSchemes]?.toReaderPersonalizedSchemes(json).orEmpty()
            preferences[Keys.ReaderPersonalizedSchemes] =
                current
                    .filterNot { it.id == schemeId }
                    .toReaderPersonalizedSchemesJson(json)
        }
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
                normalizeAudioSources(
                    sources,
                    enableLocalAudio,
                    hasLocalAudioDatabase,
                ).toAudioSourcesJson(json)
        }
    }

    suspend fun setEnableLocalAudio(enabled: Boolean) {
        dataStore.edit { preferences ->
            val effectiveEnabled =
                enabled && (preferences[Keys.LocalAudioDatabaseSizeBytes] ?: 0L) > 0L
            val hasLocalAudioDatabase = (preferences[Keys.LocalAudioDatabaseSizeBytes] ?: 0L) > 0L
            preferences[Keys.EnableLocalAudio] = effectiveEnabled
            val sources =
                preferences[Keys.AudioSources]?.toAudioSources(json) ?: listOf(AudioSource.Default)
            preferences[Keys.AudioSources] =
                normalizeAudioSources(
                    sources,
                    effectiveEnabled,
                    hasLocalAudioDatabase,
                ).toAudioSourcesJson(json)
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
            val sources =
                preferences[Keys.AudioSources]?.toAudioSources(json) ?: listOf(AudioSource.Default)
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

    override suspend fun updateSettings(transform: (AnkiSettings) -> AnkiSettings) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.AnkiSettings]?.toAnkiSettings(json) ?: AnkiSettings()
            preferences[Keys.AnkiSettings] = json.encodeToString(transform(current))
        }
    }

    suspend fun setAnkiConnectionMode(mode: AnkiConnectionMode) {
        updateSettings { it.copy(connectionMode = mode) }
    }

    suspend fun setAnkiConnectUrl(url: String) {
        updateSettings { it.copy(ankiConnect = it.ankiConnect.copy(url = url)) }
    }

    suspend fun setAnkiConnectTimeoutMillis(timeoutMillis: Int) {
        updateSettings {
            it.copy(ankiConnect = it.ankiConnect.copy(timeoutMillis = timeoutMillis.coerceIn(1_000, 60_000)))
        }
    }

    suspend fun setAnkiConnectConfig(config: AnkiConnectConfig) {
        updateSettings {
            it.copy(
                ankiConnect =
                    config.copy(
                        timeoutMillis = config.timeoutMillis.coerceIn(1_000, 60_000),
                    ),
            )
        }
    }

    suspend fun setAnkiDuplicateScope(scope: AnkiDuplicateScope) {
        updateSettings { it.copy(duplicateScope = scope) }
    }

    suspend fun setAnkiCheckAllModels(enabled: Boolean) {
        updateSettings { it.copy(checkAllModels = enabled) }
    }

    suspend fun setAnkiForceSync(enabled: Boolean) {
        updateSettings { it.copy(forceSync = enabled) }
    }

    suspend fun setAnkiSelectedDeck(deckName: String?) {
        updateSettings { it.copy(selectedDeck = deckName?.takeIf(String::isNotBlank)) }
    }

    suspend fun setAnkiSelectedNoteType(noteTypeName: String?) {
        updateSettings { it.copy(selectedNoteType = noteTypeName?.takeIf(String::isNotBlank)) }
    }

    suspend fun setAnkiFieldMappings(mappings: Map<String, String>) {
        updateSettings { it.copy(fieldMappings = mappings) }
    }

    suspend fun setAnkiTags(tags: List<String>) {
        updateSettings { it.copy(tags = tags.map(String::trim).filter(String::isNotBlank).distinct()) }
    }

    suspend fun setAnkiAllowDuplicates(enabled: Boolean) {
        updateSettings { it.copy(allowDuplicates = enabled) }
    }

    suspend fun setAnkiCompactGlossaries(enabled: Boolean) {
        updateSettings { it.copy(compactGlossaries = enabled) }
    }

    suspend fun setAnkiEmbedMedia(enabled: Boolean) {
        updateSettings { it.copy(embedMedia = enabled) }
    }
}

private object Keys {
    val BookshelfSortMode = stringPreferencesKey("bookshelf_sort_mode")
    val ThemeMode = stringPreferencesKey("theme_mode")
    val UiThemeEngine = stringPreferencesKey("ui_theme_engine")
    val UiScalePercent = intPreferencesKey("ui_scale_percent")
    val LanguageMode = stringPreferencesKey("language_mode")
    val ReaderThemeMode = stringPreferencesKey("reader_theme_mode")
    val MonetEnabled = booleanPreferencesKey("monet_enabled")
    val MonetKeyColor = longPreferencesKey("monet_key_color")
    val MaterialEInkMode = booleanPreferencesKey("material_eink_mode")
    val BlurEnabled = booleanPreferencesKey("blur_enabled")
    val MaxResults = intPreferencesKey("dictionary_max_results")
    val ScanLength = intPreferencesKey("dictionary_scan_length")
    val ReaderFullscreen = booleanPreferencesKey("reader_fullscreen")
    val ReaderActionBarPinned = booleanPreferencesKey("reader_action_bar_pinned")
    val ReaderShowReadingInfo = booleanPreferencesKey("reader_show_reading_info")
    val ReaderVerticalWriting = booleanPreferencesKey("reader_vertical_writing")
    val ReaderFontFamily = stringPreferencesKey("reader_font_family")
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
    val ReaderPersonalizedSchemes = stringPreferencesKey("reader_personalized_schemes")
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
    val AnkiSettings = stringPreferencesKey("anki_settings")
}

private fun String.toThemeMode(): ThemeMode = ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.System

private fun String.toUiThemeEngine(): UiThemeEngine =
    UiThemeEngine.entries.firstOrNull { it.name == this }
        ?: UiThemeEngine.Miuix

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

private fun String.toReaderPersonalizedSchemes(json: Json): List<ReaderPersonalizedScheme> =
    runCatching {
        json.decodeFromString(ListSerializer(ReaderPersonalizedScheme.serializer()), this)
    }.getOrDefault(emptyList())

private fun normalizeUiScalePercent(value: Int): Int = ((value.coerceIn(80, 150) + 5) / 10) * 10

private fun List<AudioSource>.toAudioSourcesJson(json: Json): String = json.encodeToString(ListSerializer(AudioSource.serializer()), this)

private fun List<ReaderPersonalizedScheme>.toReaderPersonalizedSchemesJson(json: Json): String =
    json.encodeToString(ListSerializer(ReaderPersonalizedScheme.serializer()), this)

private fun String.toAnkiSettings(json: Json): AnkiSettings =
    runCatching {
        json.decodeFromString<AnkiSettings>(this)
    }.recoverCatching { throwable ->
        if (throwable is SerializationException || throwable is IllegalArgumentException) {
            AnkiSettings()
        } else {
            throw throwable
        }
    }.getOrDefault(AnkiSettings())

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
