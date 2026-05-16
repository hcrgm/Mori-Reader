package app.mori.reader.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.ui.theme.MoriTheme
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File

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
                model =
                    ImageRequest
                        .Builder(context)
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
    val containerColor: androidx.compose.ui.graphics.Color
    val accentColor: androidx.compose.ui.graphics.Color
    val textColor: androidx.compose.ui.graphics.Color
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            containerColor = MiuixTheme.colorScheme.tertiaryContainer
            accentColor = MiuixTheme.colorScheme.primary
            textColor = MiuixTheme.colorScheme.onTertiaryContainer
        }

        UiThemeEngine.Material -> {
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
            accentColor = MaterialTheme.colorScheme.primary
            textColor = MaterialTheme.colorScheme.onTertiaryContainer
        }
    }
    Box(
        modifier = modifier.background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .fillMaxSize(0.18f)
                    .background(accentColor.copy(alpha = 0.16f)),
        )
        Text(
            text = title.ifBlank { "EPUB" },
            modifier = Modifier.padding(10.dp),
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
