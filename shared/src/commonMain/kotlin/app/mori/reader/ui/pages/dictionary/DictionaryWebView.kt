package app.mori.reader.ui.pages.dictionary

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun DictionaryWebView(
    state: DictionaryWebViewState,
    config: DictionaryWebViewSettings,
    modifier: Modifier = Modifier,
    callbacks: DictionaryWebViewCallbacks = DictionaryWebViewCallbacks(),
)
