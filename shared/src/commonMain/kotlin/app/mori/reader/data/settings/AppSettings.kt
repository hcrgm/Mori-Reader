package app.mori.reader.data.settings

import app.mori.reader.data.anki.AnkiSettings
import kotlinx.serialization.Serializable

data class AppSettings(
    val bookshelfSortMode: BookshelfSortMode = BookshelfSortMode.Recent,
    val themeMode: ThemeMode = ThemeMode.System,
    val readerThemeMode: ReaderThemeMode = ReaderThemeMode.FollowApp,
    val blurEnabled: Boolean = true,
    val maxResults: Int = 16,
    val languageMode: LanguageMode = LanguageMode.System,

    val scanLength: Int = 16,
    val readerFontSize: Int = 22,
    val readerLineHeight: Double = 1.65,
    val readerHorizontalPadding: Int = 5,
    val readerVerticalPadding: Int = 0,
    val readerAvoidPageBreak: Boolean = false,
    val readerJustifyText: Boolean = false,
    val readerLayoutAdvanced: Boolean = false,
    val readerCharacterSpacing: Double = 0.0,
    val readerContinuousMode: Boolean = false,
    val readerHideFurigana: Boolean = false,
    val readerFullscreen: Boolean = false,
    val popupWidth: Int = 320,
    val popupHeight: Int = 250,
    val popupFullWidth: Boolean = false,
    val popupSwipeToDismiss: Boolean = false,
    val popupSwipeThreshold: Int = 40,
    val collapseDictionaries: Boolean = false,
    val compactGlossaries: Boolean = true,
    val showExpressionTags: Boolean = false,
    val harmonicFrequency: Boolean = false,
    val deduplicatePitchAccents: Boolean = false,
    val audioSources: List<AudioSource> = listOf(AudioSource.Local.copy(isEnabled = false), AudioSource.Default),
    val enableLocalAudio: Boolean = false,
    val audioEnableAutoplay: Boolean = false,
    val audioPlaybackMode: AudioPlaybackMode = AudioPlaybackMode.Duck,
    val localAudioDatabaseSizeBytes: Long = 0L,
    val anki: AnkiSettings = AnkiSettings(),
)

enum class BookshelfSortMode(val wireName: String, val label: String) {
    Recent("recent", "最近阅读"),
    Title("title", "书名"),
}

enum class ThemeMode {
    System,
    Light,
    Dark,
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
        val Default = AudioSource(
            name = "Default",
            url = "https://hoshi-reader.manhhaoo-do.workers.dev/?term={term}&reading={reading}",
            isEnabled = true,
            isDefault = true,
        )

        val Local = AudioSource(
            name = "Local",
            url = "local://audio?term={term}&reading={reading}",
            isEnabled = true,
        )
    }
}

enum class AudioPlaybackMode(val wireName: String, val label: String) {
    Interrupt("interrupt", "打断其他音频"),
    Duck("duck", "降低其他音量"),
    Mix("mix", "保持其他音量"),
}
