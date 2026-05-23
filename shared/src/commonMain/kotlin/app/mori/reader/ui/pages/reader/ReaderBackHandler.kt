package app.mori.reader.ui.pages.reader

import androidx.compose.runtime.Composable

@Composable
expect fun ReaderBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
)
