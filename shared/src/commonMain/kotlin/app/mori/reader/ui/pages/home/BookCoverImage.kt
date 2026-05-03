package app.mori.reader.ui.pages.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun BookCoverImage(
    coverPath: String?,
    title: String,
    modifier: Modifier = Modifier,
)
