package app.mori.reader.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mori.reader.data.book.BookCategory
import app.mori.reader.data.book.BookInfo
import app.mori.reader.data.audiobook.AudiobookAssetInfo
import app.mori.reader.data.audiobook.AudiobookAssetType
import app.mori.reader.features.audiobook.presentation.AudiobookState
import app.mori.reader.features.bookshelf.presentation.HomeState
import app.mori.reader.data.audiobook.AudiobookStorageMode
import app.mori.reader.core.platform.rememberAudiobookAudioPicker
import app.mori.reader.core.platform.rememberAudiobookSubtitlePicker
import app.mori.reader.core.platform.rememberEpubPicker
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.BookshelfSortMode
import app.mori.reader.features.audiobook.presentation.AudiobookIntent
import app.mori.reader.features.bookshelf.presentation.BookshelfIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.audiobook_audio_title
import app.mori.reader.shared.generated.resources.audiobook_delete
import app.mori.reader.shared.generated.resources.audiobook_import_audio
import app.mori.reader.shared.generated.resources.audiobook_import_subtitle
import app.mori.reader.shared.generated.resources.audiobook_missing
import app.mori.reader.shared.generated.resources.audiobook_reimport
import app.mori.reader.shared.generated.resources.audiobook_storage_copy
import app.mori.reader.shared.generated.resources.audiobook_storage_reference
import app.mori.reader.shared.generated.resources.audiobook_storage_title
import app.mori.reader.shared.generated.resources.audiobook_subtitle_cue_count
import app.mori.reader.shared.generated.resources.audiobook_subtitle_parsed
import app.mori.reader.shared.generated.resources.audiobook_subtitle_storage_note
import app.mori.reader.shared.generated.resources.audiobook_subtitle_title
import app.mori.reader.shared.generated.resources.audiobook_title
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_confirm
import app.mori.reader.shared.generated.resources.btn_delete
import app.mori.reader.shared.generated.resources.cd_add_category
import app.mori.reader.shared.generated.resources.cd_category_manage
import app.mori.reader.shared.generated.resources.cd_close
import app.mori.reader.shared.generated.resources.cd_drag_sort
import app.mori.reader.shared.generated.resources.cd_import_book
import app.mori.reader.shared.generated.resources.cd_rename
import app.mori.reader.shared.generated.resources.cd_sort_by
import app.mori.reader.shared.generated.resources.home_adjust_category
import app.mori.reader.shared.generated.resources.home_audiobook
import app.mori.reader.shared.generated.resources.home_category_name_label
import app.mori.reader.shared.generated.resources.home_delete_book
import app.mori.reader.shared.generated.resources.home_delete_book_confirm
import app.mori.reader.shared.generated.resources.home_delete_category
import app.mori.reader.shared.generated.resources.home_empty_bookshelf
import app.mori.reader.shared.generated.resources.home_empty_category
import app.mori.reader.shared.generated.resources.home_importing
import app.mori.reader.shared.generated.resources.home_loading
import app.mori.reader.shared.generated.resources.home_no_categories
import app.mori.reader.shared.generated.resources.home_no_categories_available
import app.mori.reader.shared.generated.resources.home_rename_category
import app.mori.reader.shared.generated.resources.home_tab_all
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.settings.SettingSlider
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.window.WindowListPopup
import androidx.compose.foundation.lazy.grid.items as gridItems
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
    val progress = book.progressPercent.coerceIn(0, 100)
    val progressText = "${"%.1f".format(progress.toFloat())}%"
    val cardCornerRadius = 18.dp
    val cardShape = RoundedCornerShape(cardCornerRadius)
    val progressShape = RoundedCornerShape(50)

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = cardCornerRadius,
            colors = CardDefaults.defaultColors(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(5f / 7f)
                        .clip(cardShape),
                )
                Column(
                    modifier = Modifier
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
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(progressShape)
                                .background(MiuixTheme.colorScheme.surfaceVariant),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress / 100f)
                                    .height(3.dp)
                                    .background(MiuixTheme.colorScheme.primary),
                            )
                        }
                        Text(
                            text = progressText,
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                        )
                    }
                    Text(
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
