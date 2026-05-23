package app.mori.reader.features.settings.presentation

import app.mori.reader.data.audiobook.AudiobookStorageMode
import app.mori.reader.data.dictionary.DictionaryType
import app.mori.reader.data.dictionary.MoveDirection
import app.mori.reader.data.settings.AudioPlaybackMode
import app.mori.reader.data.settings.BookshelfSortMode
import app.mori.reader.data.settings.LanguageMode
import app.mori.reader.data.settings.ReaderPersonalizedScheme
import app.mori.reader.data.settings.ReaderSettings
import app.mori.reader.data.settings.ReaderThemeMode
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.data.settings.UiThemeEngine

sealed interface SettingsIntent {
    data class SetThemeMode(
        val mode: ThemeMode,
    ) : SettingsIntent

    data class SetUiThemeEngine(
        val engine: UiThemeEngine,
    ) : SettingsIntent

    data class SetUiScalePercent(
        val value: Int,
    ) : SettingsIntent

    data class SetLanguageMode(
        val mode: LanguageMode,
    ) : SettingsIntent

    data class SetBookshelfSortMode(
        val mode: BookshelfSortMode,
    ) : SettingsIntent

    data class SetReaderThemeMode(
        val mode: ReaderThemeMode,
    ) : SettingsIntent

    data class SetReaderFullscreen(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetReaderActionBarPinned(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetReaderShowReadingInfo(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetBlurEnabled(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetMonetEnabled(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetMonetKeyColor(
        val color: Long,
    ) : SettingsIntent

    data class SetMaterialEInkMode(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetReaderVerticalWriting(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetReaderFontSize(
        val value: Int,
    ) : SettingsIntent

    data class SetReaderLineHeight(
        val value: Double,
    ) : SettingsIntent

    data class SetReaderHorizontalPadding(
        val value: Int,
    ) : SettingsIntent

    data class SetReaderVerticalPadding(
        val value: Int,
    ) : SettingsIntent

    data class SetReaderAvoidPageBreak(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetReaderJustifyText(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetReaderLayoutAdvanced(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetReaderCharacterSpacing(
        val value: Double,
    ) : SettingsIntent

    data class SetReaderContinuousMode(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetReaderHideFurigana(
        val enabled: Boolean,
    ) : SettingsIntent

    data class UpdateGlobalReaderSettings(
        val settings: ReaderSettings,
    ) : SettingsIntent

    data class CreateReaderPersonalizedScheme(
        val scheme: ReaderPersonalizedScheme,
    ) : SettingsIntent

    data class RenameReaderPersonalizedScheme(
        val schemeId: String,
        val name: String,
    ) : SettingsIntent

    data class UpdateReaderPersonalizedSchemeSettings(
        val schemeId: String,
        val settings: ReaderSettings,
    ) : SettingsIntent

    data class DeleteReaderPersonalizedScheme(
        val schemeId: String,
    ) : SettingsIntent

    data class SetBookReaderScheme(
        val bookId: String,
        val schemeId: String?,
    ) : SettingsIntent

    data class SetMaxResults(
        val value: Int,
    ) : SettingsIntent

    data class SetScanLength(
        val value: Int,
    ) : SettingsIntent

    data class SetCollapseDictionaries(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetCompactGlossaries(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetShowExpressionTags(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetHarmonicFrequency(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetDeduplicatePitchAccents(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetAudioSourceEnabled(
        val url: String,
        val enabled: Boolean,
    ) : SettingsIntent

    data class AddAudioSource(
        val name: String,
        val url: String,
    ) : SettingsIntent

    data class UpdateAudioSource(
        val originalUrl: String,
        val name: String,
        val url: String,
    ) : SettingsIntent

    data class MoveAudioSource(
        val url: String,
        val direction: MoveDirection,
    ) : SettingsIntent

    data class ReorderAudioSources(
        val urls: List<String>,
    ) : SettingsIntent

    data class DeleteAudioSource(
        val url: String,
    ) : SettingsIntent

    data class SetEnableLocalAudio(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetAudioEnableAutoplay(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetAudioPlaybackMode(
        val mode: AudioPlaybackMode,
    ) : SettingsIntent

    data class ImportLocalAudioDatabase(
        val uriString: String,
    ) : SettingsIntent

    data object DeleteLocalAudioDatabase : SettingsIntent

    data class SetAudiobookStorageMode(
        val mode: AudiobookStorageMode,
    ) : SettingsIntent

    data class SetSasayakiSyncEnabled(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetSasayakiAutoScroll(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetSasayakiAutoPauseOnLookup(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetSasayakiHighlightEnabled(
        val enabled: Boolean,
    ) : SettingsIntent

    data class SetSasayakiHighlightColor(
        val color: String,
    ) : SettingsIntent

    data class SelectDictionaryType(
        val type: DictionaryType,
    ) : SettingsIntent

    data class ImportDictionaries(
        val type: DictionaryType,
        val uriStrings: List<String>,
    ) : SettingsIntent

    data class SetDictionaryEnabled(
        val type: DictionaryType,
        val id: String,
        val enabled: Boolean,
    ) : SettingsIntent

    data class MoveDictionary(
        val type: DictionaryType,
        val id: String,
        val direction: MoveDirection,
    ) : SettingsIntent

    data class ReorderDictionaries(
        val type: DictionaryType,
        val ids: List<String>,
    ) : SettingsIntent

    data class DeleteDictionary(
        val type: DictionaryType,
        val id: String,
    ) : SettingsIntent

    data object UpdateDictionaries : SettingsIntent

    data object DismissDictionaryError : SettingsIntent

    data object DismissDictionaryImportSummary : SettingsIntent
}
