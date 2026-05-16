package app.mori.reader.ui.pages.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mori.reader.data.book.BookInfo
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.home_adjust_category
import app.mori.reader.shared.generated.resources.home_audiobook
import app.mori.reader.shared.generated.resources.home_delete_book
import app.mori.reader.ui.theme.MoriTheme
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.window.WindowListPopup
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Text as MiuixText

@Composable
fun BookCard(
    book: BookInfo,
    showContextMenu: Boolean,
    onOpenContextMenu: () -> Unit,
    onDismissContextMenu: () -> Unit,
    onEditBookCategories: () -> Unit,
    onOpenAudiobook: () -> Unit,
    onDeleteBook: () -> Unit,
    onClick: () -> Unit,
) {
    if (MoriTheme.uiThemeEngine == UiThemeEngine.Material) {
        MaterialBookCard(
            book = book,
            showContextMenu = showContextMenu,
            onOpenContextMenu = onOpenContextMenu,
            onDismissContextMenu = onDismissContextMenu,
            onEditBookCategories = onEditBookCategories,
            onOpenAudiobook = onOpenAudiobook,
            onDeleteBook = onDeleteBook,
            onClick = onClick,
        )
        return
    }

    MiuixBookCard(
        book = book,
        showContextMenu = showContextMenu,
        onOpenContextMenu = onOpenContextMenu,
        onDismissContextMenu = onDismissContextMenu,
        onEditBookCategories = onEditBookCategories,
        onOpenAudiobook = onOpenAudiobook,
        onDeleteBook = onDeleteBook,
        onClick = onClick,
    )
}

@Composable
private fun MiuixBookCard(
    book: BookInfo,
    showContextMenu: Boolean,
    onOpenContextMenu: () -> Unit,
    onDismissContextMenu: () -> Unit,
    onEditBookCategories: () -> Unit,
    onOpenAudiobook: () -> Unit,
    onDeleteBook: () -> Unit,
    onClick: () -> Unit,
) {
    val progress = book.progressPercent.coerceIn(0, 100)
    val progressText = "${"%.1f".format(progress.toFloat())}%"
    val cardCornerRadius = 18.dp
    val cardShape = RoundedCornerShape(cardCornerRadius)
    val progressShape = RoundedCornerShape(50)

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        MiuixCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = cardCornerRadius,
            colors =
                CardDefaults.defaultColors(
                    color = Color.Transparent,
                ),
            insideMargin = PaddingValues(0.dp),
            pressFeedbackType = PressFeedbackType.Tilt,
            onClick = onClick,
            onLongPress = onOpenContextMenu,
            // holdDownState - TODO: 这个参数，miuix还没有发release，release后补上，交互体验更好
        ) {
            Column(
                modifier = Modifier.clip(cardShape),
            ) {
                BookCoverImage(
                    coverPath = book.coverPath,
                    title = book.title,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(5f / 7f)
                            .clip(cardShape),
                )
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 1.dp, end = 1.dp, top = 6.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(progressShape)
                                    .background(MiuixTheme.colorScheme.surfaceVariant),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth(progress / 100f)
                                        .height(3.dp)
                                        .background(MiuixTheme.colorScheme.primary),
                            )
                        }
                        MiuixText(
                            text = progressText,
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                        )
                    }
                    MiuixText(
                        text = book.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        WindowListPopup(
            show = showContextMenu,
            popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
            alignment = PopupPositionProvider.Align.TopStart,
            onDismissRequest = onDismissContextMenu,
            onDismissFinished = onDismissContextMenu,
        ) {
            val dismiss = LocalDismissState.current
            ListPopupColumn {
                DropdownImpl(
                    text = stringResource(Res.string.home_adjust_category),
                    optionSize = 3,
                    isSelected = false,
                    index = 0,
                    onSelectedIndexChange = {
                        onEditBookCategories()
                        dismiss?.invoke()
                    },
                )
                DropdownImpl(
                    text = stringResource(Res.string.home_audiobook),
                    optionSize = 3,
                    isSelected = false,
                    index = 1,
                    onSelectedIndexChange = {
                        onOpenAudiobook()
                        dismiss?.invoke()
                    },
                )
                DropdownImpl(
                    text = stringResource(Res.string.home_delete_book),
                    optionSize = 3,
                    isSelected = false,
                    index = 2,
                    onSelectedIndexChange = {
                        onDeleteBook()
                        dismiss?.invoke()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MaterialBookCard(
    book: BookInfo,
    showContextMenu: Boolean,
    onOpenContextMenu: () -> Unit,
    onDismissContextMenu: () -> Unit,
    onEditBookCategories: () -> Unit,
    onOpenAudiobook: () -> Unit,
    onDeleteBook: () -> Unit,
    onClick: () -> Unit,
) {
    val progress = book.progressPercent.coerceIn(0, 100)
    val progressText = "${"%.1f".format(progress.toFloat())}%"
    val cardShape = MaterialTheme.shapes.small
    val progressShape = RoundedCornerShape(50)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onOpenContextMenu,
                ).padding(4.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column {
                BookCoverImage(
                    coverPath = book.coverPath,
                    title = book.title,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(5f / 7f)
                            .clip(cardShape),
                )
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 1.dp, end = 1.dp, top = 6.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(progressShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            gapSize = 0.dp,
                            drawStopIndicator = {},
                        )
                        androidx.compose.material3.Text(
                            text = progressText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                        )
                    }
                    androidx.compose.material3.Text(
                        text = book.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = onDismissContextMenu,
        ) {
            DropdownMenuItem(
                text = { androidx.compose.material3.Text(text = stringResource(Res.string.home_adjust_category)) },
                onClick = {
                    onEditBookCategories()
                    onDismissContextMenu()
                },
            )
            DropdownMenuItem(
                text = { androidx.compose.material3.Text(text = stringResource(Res.string.home_audiobook)) },
                onClick = {
                    onOpenAudiobook()
                    onDismissContextMenu()
                },
            )
            DropdownMenuItem(
                text = { androidx.compose.material3.Text(text = stringResource(Res.string.home_delete_book)) },
                onClick = {
                    onDeleteBook()
                    onDismissContextMenu()
                },
            )
        }
    }
}
