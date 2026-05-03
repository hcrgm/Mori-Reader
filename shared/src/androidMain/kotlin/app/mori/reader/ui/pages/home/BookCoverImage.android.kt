package app.mori.reader.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.io.File
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
actual fun BookCoverImage(
    coverPath: String?,
    title: String,
    modifier: Modifier,
) {
    val context = LocalContext.current

    Box(modifier = modifier) {
        if (!coverPath.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(coverPath))
                    .memoryCacheKey(coverPath)
                    .diskCacheKey(coverPath)
                    .crossfade(false)
                    .build(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            PlaceholderCover(
                title = title,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PlaceholderCover(
    title: String,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.background(MiuixTheme.colorScheme.tertiaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .fillMaxSize(0.18f)
                .background(MiuixTheme.colorScheme.primary.copy(alpha = 0.16f)),
        )
        Text(
            text = title.ifBlank { "EPUB" },
            modifier = Modifier.padding(10.dp),
            color = MiuixTheme.colorScheme.onTertiaryContainer,
            fontWeight = FontWeight.SemiBold,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
