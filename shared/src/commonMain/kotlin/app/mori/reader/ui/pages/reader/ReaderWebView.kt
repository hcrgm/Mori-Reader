package app.mori.reader.ui.pages.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.mori.reader.data.book.ReaderChapter
import app.mori.reader.ui.ReaderSelectionRect

@Composable
expect fun ReaderWebView(
    chapter: ReaderChapter?,
    progress: Double,
    navigationVersion: Int,
    fragment: String?,
    verticalWriting: Boolean,
    isDark: Boolean,
    scanLength: Int,
    fontSize: Int,
    lineHeight: Double,
    horizontalPadding: Int,
    verticalPadding: Int,
    avoidPageBreak: Boolean,
    justifyText: Boolean,
    characterSpacing: Double,
    continuousMode: Boolean,
    hideFurigana: Boolean,
    selectionHighlightLength: Int?,
    stabilizeForBackdrop: Boolean = false,
    modifier: Modifier = Modifier,
    onProgressChanged: (Double) -> Unit,
    onProgressSaved: (Double) -> Unit,
    onTextSelected: (text: String, sentence: String, rect: ReaderSelectionRect?) -> Unit,
    onLinkActivated: (href: String) -> Unit,
    onTapOutside: () -> Unit,
    onNextChapter: () -> Unit,
    onPreviousChapter: () -> Unit,
)
