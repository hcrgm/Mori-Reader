package app.mori.reader.ui.pages.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mori.reader.data.book.BookInfo
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.bookshelf.presentation.BookshelfIntent
import app.mori.reader.features.bookshelf.presentation.BookshelfState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_import
import app.mori.reader.shared.generated.resources.cd_close
import app.mori.reader.shared.generated.resources.home_empty_bookshelf
import app.mori.reader.shared.generated.resources.home_empty_bookshelf_hint
import app.mori.reader.shared.generated.resources.home_empty_category
import app.mori.reader.shared.generated.resources.home_empty_category_hint
import app.mori.reader.shared.generated.resources.home_importing
import app.mori.reader.shared.generated.resources.home_loading
import app.mori.reader.ui.components.navigation.eInkPagerSwipeModifier
import app.mori.reader.ui.text.asString
import app.mori.reader.ui.theme.MoriTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.utils.overScrollVertical
import androidx.compose.foundation.lazy.grid.items as gridItems

@Composable
fun BookshelfContent(
    home: BookshelfState,
    settings: AppSettings,
    paddingValues: PaddingValues,
    pagerState: androidx.compose.foundation.pager.PagerState,
    categoryIds: List<String?>,
    contextMenuBookId: String?,
    contextMenuPage: Int?,
    onOpenContextMenu: (Int, BookInfo) -> Unit,
    onDismissContextMenu: () -> Unit,
    onEditBookCategories: () -> Unit,
    onOpenAudiobook: () -> Unit,
    onDeleteBook: () -> Unit,
    onImportBooks: () -> Unit,
    onBookshelfIntent: (BookshelfIntent) -> Unit,
    onOpenBook: (String) -> Unit,
) {
    val reduceMotion =
        MoriTheme.uiThemeEngine == UiThemeEngine.Material &&
            MoriTheme.materialEInkMode
    val defaultOverscrollEffect = rememberOverscrollEffect()
    val defaultFlingBehavior = PagerDefaults.flingBehavior(state = pagerState)
    val pagerCoroutineScope = rememberCoroutineScope()
    val instantFlingBehavior =
        PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = tween(durationMillis = 0),
        )
    val flingBehavior = if (reduceMotion) instantFlingBehavior else defaultFlingBehavior
    HorizontalPager(
        state = pagerState,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = HomeHorizontalPadding + HomeTabInnerPadding)
                .eInkPagerSwipeModifier(
                    enabled = reduceMotion,
                    currentPage = pagerState.currentPage,
                    pageCount = categoryIds.size,
                    onPageChange = { page ->
                        pagerCoroutineScope.launch {
                            pagerState.scrollToPage(page)
                        }
                    },
                ),
        beyondViewportPageCount = 1,
        pageSpacing = HomePageSpacing,
        flingBehavior = flingBehavior,
        userScrollEnabled = !reduceMotion,
        overscrollEffect = if (reduceMotion) null else defaultOverscrollEffect,
        key = { page -> categoryIds.getOrNull(page) ?: "all" },
    ) { page ->
        val gridState = rememberLazyGridState()
        val visibleBooks =
            if (page == 0) {
                home.books.sortedFor(settings.bookshelf.sortMode)
            } else {
                home.books
                    .filter { categoryIds.getOrNull(page) in it.categoryIds }
                    .sortedFor(settings.bookshelf.sortMode)
            }
        val canScroll =
            !home.isLoading &&
                visibleBooks.isNotEmpty()
        val standaloneState =
            when {
                home.errorMessage != null || home.isImporting -> null
                home.isLoading ->
                    BookshelfEmptyStateModel(
                        face = "( -_-) zZ",
                        title = stringResource(Res.string.home_loading),
                        subtitle = null,
                        actionLabel = null,
                    )
                visibleBooks.isEmpty() && page == 0 ->
                    BookshelfEmptyStateModel(
                        face = "(_ . _)",
                        title = stringResource(Res.string.home_empty_bookshelf),
                        subtitle = stringResource(Res.string.home_empty_bookshelf_hint),
                        actionLabel = stringResource(Res.string.btn_import),
                    )
                visibleBooks.isEmpty() ->
                    BookshelfEmptyStateModel(
                        face = "( ´ . .̫ . ` )",
                        title = stringResource(Res.string.home_empty_category),
                        subtitle = stringResource(Res.string.home_empty_category_hint),
                        actionLabel = null,
                    )
                else -> null
            }

        LaunchedEffect(settings.bookshelf.sortMode) {
            if (
                !home.isLoading &&
                (visibleBooks.isNotEmpty() || home.errorMessage != null || home.isImporting)
            ) {
                gridState.scrollToItem(0)
            }
        }

        if (standaloneState != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding() + HomeContentTopSpacing)
                        .padding(bottom = paddingValues.calculateBottomPadding()),
            ) {
                BookshelfEmptyState(
                    modifier = Modifier.fillMaxSize(),
                    state = standaloneState,
                    onAction = if (standaloneState.actionLabel != null) onImportBooks else null,
                )
            }
        } else {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .then(
                            if (MoriTheme.uiThemeEngine == UiThemeEngine.Miuix && canScroll) {
                                Modifier.overScrollVertical()
                            } else {
                                Modifier
                            },
                        ),
                userScrollEnabled = canScroll,
                contentPadding =
                    PaddingValues(
                        top = paddingValues.calculateTopPadding() + HomeContentTopSpacing,
                        bottom = paddingValues.calculateBottomPadding(),
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                home.errorMessage?.let { error ->
                    item(
                        key = "error",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        ErrorCard(
                            message = error.asString(),
                            onDismiss = { onBookshelfIntent(BookshelfIntent.DismissHomeError) },
                        )
                    }
                }

                if (home.isImporting) {
                    item(
                        key = "importing",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        StatusCard(text = stringResource(Res.string.home_importing))
                    }
                }

                when {
                    visibleBooks.isEmpty() -> {
                        item(
                            key = "empty-fallback",
                            span = { GridItemSpan(maxLineSpan) },
                        ) {
                            EmptyBookshelfMessage(
                                text =
                                    if (page == 0) {
                                        stringResource(Res.string.home_empty_bookshelf)
                                    } else {
                                        stringResource(Res.string.home_empty_category)
                                    },
                            )
                        }
                    }

                    else -> {
                        gridItems(
                            items = visibleBooks,
                            key = { it.id },
                        ) { book ->
                            BookCard(
                                book = book,
                                showContextMenu = contextMenuBookId == book.id && contextMenuPage == page,
                                onOpenContextMenu = { onOpenContextMenu(page, book) },
                                onDismissContextMenu = onDismissContextMenu,
                                onEditBookCategories = onEditBookCategories,
                                onOpenAudiobook = onOpenAudiobook,
                                onDeleteBook = onDeleteBook,
                                onClick = { onOpenBook(book.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit,
) {
    if (MoriTheme.uiThemeEngine == UiThemeEngine.Material) {
        MaterialErrorCard(
            message = message,
            onDismiss = onDismiss,
        )
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onErrorContainer,
            )
            IconButton(onClick = onDismiss) {
                Icon(MiuixIcons.Close, contentDescription = stringResource(Res.string.cd_close))
            }
        }
    }
}

@Composable
fun StatusCard(text: String) {
    if (MoriTheme.uiThemeEngine == UiThemeEngine.Material) {
        MaterialStatusCard(text = text)
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
fun EmptyBookshelfMessage(text: String) {
    BookshelfEmptyState(
        modifier = Modifier,
        state = BookshelfEmptyStateModel(face = "( -_-) zZ", title = text, subtitle = null, actionLabel = null),
        onAction = null,
    )
}

private data class BookshelfEmptyStateModel(
    val face: String,
    val title: String,
    val subtitle: String?,
    val actionLabel: String?,
)

@Composable
private fun BookshelfEmptyState(
    modifier: Modifier,
    state: BookshelfEmptyStateModel,
    onAction: (() -> Unit)?,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val isWide = maxWidth >= 560.dp
        val horizontalPadding = if (isWide) 32.dp else 24.dp
        val verticalPadding = if (isWide) 30.dp else 24.dp
        val widthFraction = if (isWide) 0.72f else 1f
        val cardModifier =
            Modifier
                .fillMaxWidth(widthFraction)
                .widthIn(max = 460.dp)

        if (MoriTheme.uiThemeEngine == UiThemeEngine.Material) {
            Column(
                modifier =
                    cardModifier
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                androidx.compose.material3.Text(
                    text = state.face,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                androidx.compose.material3.Text(
                    text = state.title,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                state.subtitle?.let { subtitle ->
                    androidx.compose.material3.Text(
                        text = subtitle,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
                if (onAction != null && state.actionLabel != null) {
                    androidx.compose.material3.FilledTonalButton(
                        onClick = onAction,
                        modifier = Modifier.padding(top = 6.dp),
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                        )
                        androidx.compose.material3.Text(
                            text = state.actionLabel,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        } else {
            Column(
                modifier =
                    cardModifier
                        .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = state.face,
                    fontSize = if (isWide) top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.title1.fontSize else top.yukonga.miuix.kmp.theme.MiuixTheme.textStyles.title2.fontSize,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = state.title,
                    color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
                state.subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        textAlign = TextAlign.Center,
                    )
                }
                if (onAction != null && state.actionLabel != null) {
                    TextButton(
                        text = state.actionLabel,
                        onClick = onAction,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialErrorCard(
    message: String,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            androidx.compose.material3.Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            )
            androidx.compose.material3.IconButton(onClick = onDismiss) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(Res.string.cd_close),
                )
            }
        }
    }
}

@Composable
private fun MaterialStatusCard(text: String) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer,
        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        androidx.compose.material3.Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
        )
    }
}
