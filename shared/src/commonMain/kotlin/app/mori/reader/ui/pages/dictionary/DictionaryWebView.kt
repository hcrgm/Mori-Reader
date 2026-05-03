package app.mori.reader.ui.pages.dictionary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mori.reader.data.anki.AnkiCardPayload
import app.mori.reader.data.dictionary.DictionaryLookupEntry
import app.mori.reader.data.settings.AudioPlaybackMode
import app.mori.reader.data.settings.AudioSource
import app.mori.reader.ui.ReaderSelectionRect

@Composable
expect fun DictionaryWebView(
    query: String,
    entries: List<DictionaryLookupEntry>,
    dictionaryStyles: Map<String, String>,
    isSearching: Boolean,
    hasSearched: Boolean,
    errorMessage: String?,
    maxResults: Int,
    scanLength: Int,
    collapseDictionaries: Boolean,
    compactGlossaries: Boolean,
    showExpressionTags: Boolean,
    harmonicFrequency: Boolean,
    deduplicatePitchAccents: Boolean,
    isDark: Boolean,
    audioSources: List<AudioSource>,
    audioEnableAutoplay: Boolean,
    audioPlaybackMode: AudioPlaybackMode,
    ankiEnabled: Boolean = false,
    modifier: Modifier = Modifier,
    contentTopPadding: Dp = 0.dp,
    contentBottomPadding: Dp = 0.dp,
    edgeToEdgeContent: Boolean = false,
    transparentBackground: Boolean = false,
    enableInternalPopup: Boolean = true,
    swipeDismissThreshold: Int = 0,
    onVerticalScrollActiveChange: (Boolean) -> Unit = {},
    onPopupTextSelected: (String, ReaderSelectionRect?) -> Unit = { _, _ -> },
    onSwipeDismiss: () -> Unit = {},
    onAddAnkiCard: (AnkiCardPayload) -> Unit = {},
)
