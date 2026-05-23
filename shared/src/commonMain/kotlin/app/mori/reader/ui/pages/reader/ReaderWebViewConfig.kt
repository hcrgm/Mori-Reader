package app.mori.reader.ui.pages.reader

import app.mori.reader.data.audiobook.SasayakiCueRange
import app.mori.reader.data.book.ReaderChapter
import app.mori.reader.features.lookup.presentation.ReaderSelectionRect

data class ReaderWebViewState(
    val chapter: ReaderChapter? = null,
    val progress: Double = 0.0,
    val navigationVersion: Int = 0,
    val fragment: String? = null,
    val capturePageTextRequestKey: Int = 0,
    val selectionHighlightLength: Int? = null,
    val sasayakiCues: List<SasayakiCueRange> = emptyList(),
    val highlightedSasayakiCueId: String? = null,
)

data class ReaderWebViewSettings(
    val verticalWriting: Boolean = true,
    val isDark: Boolean = false,
    val eInkMode: Boolean = false,
    val scanLength: Int = 16,
    val fontFamily: String? = null,
    val fontSize: Int = 22,
    val lineHeight: Double = 1.65,
    val horizontalPadding: Int = 5,
    val verticalPadding: Int = 0,
    val avoidPageBreak: Boolean = false,
    val justifyText: Boolean = false,
    val characterSpacing: Double = 0.0,
    val continuousMode: Boolean = false,
    val hideFurigana: Boolean = false,
    val viewportLayoutKey: Int = 0,
    val sasayakiAutoScroll: Boolean = true,
    val sasayakiHighlightEnabled: Boolean = true,
    val sasayakiHighlightColor: String = "#FFC0485C",
    val stabilizeForBackdrop: Boolean = false,
)

data class ReaderWebViewCallbacks(
    val onUserInteraction: () -> Unit = {},
    val onProgressChanged: (Double) -> Unit = {},
    val onProgressSaved: (Double) -> Unit = {},
    val onPageTextCaptured: (String) -> Unit = {},
    val onTextSelected: (text: String, sentence: String, rect: ReaderSelectionRect?) -> Unit = { _, _, _ -> },
    val onLinkActivated: (href: String) -> Unit = {},
    val onTapOutside: () -> Unit = {},
    val onNextChapter: () -> Unit = {},
    val onPreviousChapter: () -> Unit = {},
)
