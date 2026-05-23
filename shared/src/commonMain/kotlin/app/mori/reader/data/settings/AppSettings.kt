package app.mori.reader.data.settings

import app.mori.reader.data.anki.AnkiSettings
import app.mori.reader.data.audiobook.AudiobookStorageMode
import kotlinx.serialization.Serializable

data class BookshelfSettings(
    val sortMode: BookshelfSortMode = BookshelfSortMode.Recent,
)

data class AppearanceSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val uiThemeEngine: UiThemeEngine = UiThemeEngine.Miuix,
    val uiScalePercent: Int = 100,
    val languageMode: LanguageMode = LanguageMode.System,
    val readerThemeMode: ReaderThemeMode = ReaderThemeMode.FollowApp,
    val monetEnabled: Boolean = false,
    val monetKeyColor: Long = 0L,
    val materialEInkMode: Boolean = false,
    val blurEnabled: Boolean = true,
)

@Serializable
data class ReaderSettings(
    val fullscreen: Boolean = true,
    val actionBarPinned: Boolean = false,
    val showReadingInfo: Boolean = true,
    val verticalWriting: Boolean = true,
    val fontFamily: String? = null,
    val fontSize: Int = 22,
    val lineHeight: Double = 1.65,
    val horizontalPadding: Int = 5,
    val verticalPadding: Int = 0,
    val avoidPageBreak: Boolean = false,
    val justifyText: Boolean = false,
    val layoutAdvanced: Boolean = false,
    val characterSpacing: Double = 0.0,
    val continuousMode: Boolean = false,
    val hideFurigana: Boolean = false,
    val popupWidth: Int = 320,
    val popupHeight: Int = 250,
    val popupFullWidth: Boolean = false,
    val popupSwipeToDismiss: Boolean = false,
    val popupSwipeThreshold: Int = 40,
)

@Serializable
data class ReaderPersonalizedScheme(
    val id: String,
    val name: String,
    val settings: ReaderSettings,
    val createdAt: Long,
)

data class DictionarySettings(
    val maxResults: Int = 16,
    val scanLength: Int = 16,
    val collapseDictionaries: Boolean = false,
    val compactGlossaries: Boolean = true,
    val showExpressionTags: Boolean = false,
    val harmonicFrequency: Boolean = false,
    val deduplicatePitchAccents: Boolean = false,
)

data class AudioSettings(
    val sources: List<AudioSource> =
        listOf(
            AudioSource.Local.copy(isEnabled = false),
            AudioSource.Default,
        ),
    val enableLocalAudio: Boolean = false,
    val enableAutoplay: Boolean = false,
    val playbackMode: AudioPlaybackMode = AudioPlaybackMode.Duck,
    val localAudioDatabaseSizeBytes: Long = 0L,
)

data class SasayakiSettings(
    val preferredStorageMode: AudiobookStorageMode = AudiobookStorageMode.Copy,
    val syncEnabled: Boolean = false,
    val autoScroll: Boolean = true,
    val autoPauseOnLookup: Boolean = true,
    val highlightEnabled: Boolean = true,
    val highlightColor: String = "#FFC0485C",
)

data class AppSettings(
    val bookshelf: BookshelfSettings = BookshelfSettings(),
    val appearance: AppearanceSettings = AppearanceSettings(),
    val reader: ReaderSettings = ReaderSettings(),
    val readerPersonalizedSchemes: List<ReaderPersonalizedScheme> = emptyList(),
    val dictionary: DictionarySettings = DictionarySettings(),
    val audio: AudioSettings = AudioSettings(),
    val sasayaki: SasayakiSettings = SasayakiSettings(),
    val anki: AnkiSettings = AnkiSettings(),
)

fun AppSettings.findReaderScheme(schemeId: String?): ReaderPersonalizedScheme? =
    schemeId?.let { targetId -> readerPersonalizedSchemes.firstOrNull { it.id == targetId } }

fun AppSettings.effectiveReaderSettings(schemeId: String?): ReaderSettings =
    findReaderScheme(schemeId)?.settings ?: reader

enum class BookshelfSortMode(
    val wireName: String,
) {
    Recent("recent"),
    Title("title"),
}

enum class ThemeMode {
    System,
    Light,
    Dark,
}

enum class UiThemeEngine {
    Miuix,
    Material,
}

enum class LanguageMode {
    System,
    English,
    Chinese,
}

enum class ReaderThemeMode {
    FollowApp,
    Light,
    Dark,
}

@Serializable
data class AudioSource(
    val name: String,
    val url: String,
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false,
) {
    val id: String
        get() = url

    val isLocal: Boolean
        get() = url == Local.url

    companion object {
        val Default =
            AudioSource(
                name = "Default",
                url = "https://hoshi-reader.manhhaoo-do.workers.dev/?term={term}&reading={reading}",
                isEnabled = true,
                isDefault = true,
            )

        val Local =
            AudioSource(
                name = "Local",
                url = "local://audio?term={term}&reading={reading}",
                isEnabled = true,
            )
    }
}

enum class AudioPlaybackMode(
    val wireName: String,
) {
    Interrupt("interrupt"),
    Duck("duck"),
    Mix("mix"),
}
