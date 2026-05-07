package app.mori.reader.ui.pages.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ReaderWebView(
    state: ReaderWebViewState,
    config: ReaderWebViewSettings,
    modifier: Modifier = Modifier,
    callbacks: ReaderWebViewCallbacks,
)
