package app.mori.reader.ui.pages.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.BookshelfSortMode
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_category_manage
import app.mori.reader.shared.generated.resources.cd_import_book
import app.mori.reader.shared.generated.resources.cd_sort_by
import app.mori.reader.shared.generated.resources.home_sort_recent
import app.mori.reader.shared.generated.resources.home_sort_title
import app.mori.reader.ui.components.material.MaterialDropdownMenu
import app.mori.reader.ui.components.material.MaterialDropdownMenuOption
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.theme.MoriTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MaterialHomePageScaffold(
    title: String,
    settings: AppSettings,
    fixedPadding: PaddingValues,
    isLoading: Boolean,
    tabs: List<String>,
    selectedTabIndex: Int,
    currentSortMode: BookshelfSortMode,
    onTabSelected: (Int) -> Unit,
    onImportBook: () -> Unit,
    onManageCategories: () -> Unit,
    onSetSortMode: (BookshelfSortMode) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val blurEnabled = settings.appearance.blurEnabled && !MoriTheme.materialEInkMode

    MoriPageScaffold(
        title = title,
        useSmallTopBar = true,
        revealTopBarOnReverseScroll = true,
        blurEnabled = blurEnabled,
        fixedPadding = fixedPadding,
        navigationIcon = {
            Box {
                IconButton(onClick = { sortMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Sort,
                        contentDescription = stringResource(Res.string.cd_sort_by),
                    )
                }
                MaterialDropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    options =
                        BookshelfSortMode.entries.map { option ->
                            MaterialDropdownMenuOption(
                                label = option.label(),
                                selected = currentSortMode == option,
                                onSelected = { onSetSortMode(option) },
                            )
                        },
                )
            }
        },
        actions = {
            IconButton(onClick = onImportBook) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(Res.string.cd_import_book),
                )
            }
            IconButton(onClick = onManageCategories) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(Res.string.cd_category_manage),
                )
            }
        },
        header = {
            if (isLoading) {
                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                )
            } else {
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 0.dp,
                    divider = {},
                    minTabWidth = 0.dp,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = HomeHorizontalPadding),
                    containerColor =
                        if (blurEnabled) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    tabs.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { onTabSelected(index) },
                            text = {
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
private fun BookshelfSortMode.label(): String =
    when (this) {
        BookshelfSortMode.Recent -> stringResource(Res.string.home_sort_recent)
        BookshelfSortMode.Title -> stringResource(Res.string.home_sort_title)
    }
