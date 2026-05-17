package app.mori.reader.ui.pages.dictionary

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mori.reader.data.anki.AnkiMiningContent
import app.mori.reader.data.dictionary.DictionaryLookupEntry
import app.mori.reader.data.settings.AudioPlaybackMode
import app.mori.reader.data.settings.AudioSource
import app.mori.reader.features.lookup.presentation.ReaderSelectionRect

data class DictionaryWebViewState(
    val query: String = "",
    val entries: List<DictionaryLookupEntry> = emptyList(),
    val dictionaryStyles: Map<String, String> = emptyMap(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val errorMessage: String? = null,
    val searchingMessage: String = "Searching...",
    val noResultsMessage: String = "No entries found",
    val idleMessage: String = "Enter a term to look up",
    val playPronunciationLabel: String = "Play pronunciation",
)

data class DictionaryWebViewSettings(
    val maxResults: Int = 16,
    val scanLength: Int = 16,
    val collapseDictionaries: Boolean = false,
    val compactGlossaries: Boolean = true,
    val showExpressionTags: Boolean = false,
    val harmonicFrequency: Boolean = false,
    val deduplicatePitchAccents: Boolean = false,
    val isDark: Boolean = false,
    val eInkMode: Boolean = false,
    val audioSources: List<AudioSource> = listOf(AudioSource.Default),
    val audioEnableAutoplay: Boolean = false,
    val audioPlaybackMode: AudioPlaybackMode = AudioPlaybackMode.Duck,
    val contentTopPadding: Dp = 0.dp,
    val contentBottomPadding: Dp = 0.dp,
    val edgeToEdgeContent: Boolean = false,
    val transparentBackground: Boolean = false,
    val eInkEntryBorderEnabled: Boolean = true,
    val enableInternalPopup: Boolean = true,
    val swipeDismissThreshold: Int = 0,
    val ankiNeedsAudio: Boolean = false,
    val ankiAllowDuplicates: Boolean = false,
    val ankiUseAnkiConnect: Boolean = false,
    val ankiEmbedMedia: Boolean = true,
    val ankiCompactGlossaries: Boolean = true,
    val ankiDuplicateExpression: String? = null,
    val navigateBackToken: Int = 0,
    val navigateForwardToken: Int = 0,
)

data class DictionaryWebViewCallbacks(
    val onVerticalScrollActiveChange: (Boolean) -> Unit = {},
    val onPopupTextSelected: (String, ReaderSelectionRect?) -> Unit = { _, _ -> },
    val onMineEntry: (AnkiMiningContent) -> Unit = {},
    val onCheckDuplicate: (String) -> Unit = {},
    val onSwipeDismiss: () -> Unit = {},
    val onNavigationStateChange: (Boolean, Boolean) -> Unit = { _, _ -> },
)
