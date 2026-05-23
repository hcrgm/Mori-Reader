package app.mori.reader.ui.pages.reader

import androidx.compose.runtime.Composable

data class ReaderSystemFont(
    val family: String,
    val label: String = family,
)

@Composable
expect fun rememberReaderSystemFonts(): List<ReaderSystemFont>
