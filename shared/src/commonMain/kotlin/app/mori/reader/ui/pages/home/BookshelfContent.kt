package app.mori.reader.ui.pages.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.mori.reader.data.book.BookInfo
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.bookshelf.presentation.BookshelfIntent
import app.mori.reader.features.bookshelf.presentation.BookshelfState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_close
import app.mori.reader.shared.generated.resources.home_empty_bookshelf
import app.mori.reader.shared.generated.resources.home_empty_category
import app.mori.reader.shared.generated.resources.home_importing
import app.mori.reader.shared.generated.resources.home_loading
import app.mori.reader.ui.text.asString
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import androidx.compose.foundation.lazy.grid.items as gridItems

@Composable
fun BookshelfContent(
    home: BookshelfState,
    settings: AppSettings,
    paddingValues: PaddingValues,
    scrollBehavior: top.yukonga.miuix.kmp.basic.ScrollBehavior,
    pagerState: androidx.compose.foundation.pager.PagerState,
    categoryIds: List<String?>,
    contextMenuBookId: String?,
    contextMenuPage: Int?,
    onOpenContextMenu: (Int, BookInfo) -> Unit,
    onDismissContextMenu: () -> Unit,
    onEditBookCategories: () -> Unit,
    onOpenAudiobook: () -> Unit,
    onDeleteBook: () -> Unit,
    onBookshelfIntent: (BookshelfIntent) -> Unit,
    onOpenBook: (String) -> Unit,
) {
    HorizontalPager(
        state = pagerState,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = HomeHorizontalPadding + HomeTabInnerPadding),
        beyondViewportPageCount = 1,
        pageSpacing = HomePageSpacing,
        userScrollEnabled = true,
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

        LaunchedEffect(settings.bookshelf.sortMode) {
            if (
                !home.isLoading &&
                (visibleBooks.isNotEmpty() || home.errorMessage != null || home.isImporting)
            ) {
                gridState.scrollToItem(0)
            }
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .overScrollVertical(),
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding() + HomeContentTopSpacing,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
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
                home.isLoading -> {
                    item(
                        key = "loading",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        EmptyBookshelfMessage(text = stringResource(Res.string.home_loading))
                    }
                }

                visibleBooks.isEmpty() -> {
                    item(
                        key = "empty",
                        span = { GridItemSpan(maxLineSpan) },
                    ) {
                        val emptyText =
                            if (page == 0) {
                                stringResource(Res.string.home_empty_bookshelf)
                            } else {
                                stringResource(
                                    Res.string.home_empty_category,
                                )
                            }
                        EmptyBookshelfMessage(text = emptyText)
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

@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MiuixTheme.colorScheme.onErrorContainer,
            )
            IconButton(onClick = onDismiss) {
                Icon(MiuixIcons.Close, contentDescription = stringResource(Res.string.cd_close))
            }
        }
    }
}

@Composable
fun StatusCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
fun EmptyBookshelfMessage(text: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(340.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}
