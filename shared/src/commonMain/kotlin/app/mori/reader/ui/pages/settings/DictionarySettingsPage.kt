package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mori.reader.data.dictionary.DictionaryInfo
import app.mori.reader.data.dictionary.DictionaryType
import app.mori.reader.data.dictionary.rememberDictionaryZipPicker
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.SettingSlider
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.window.WindowListPopup
import app.mori.reader.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

private val DictionarySettingsHorizontalPadding = 12.dp

private data class PendingDictionaryDeletion(
    val type: DictionaryType,
    val dictionary: DictionaryInfo,
)

@Composable
fun DictionarySettingsPage(
    state: AppState,
    message: String?,
    onIntent: (AppIntent) -> Unit,
    onBack: () -> Unit,
) {
    val dictionarySettingsPages = listOf(
        stringResource(Res.string.tab_dictionary),
        stringResource(Res.string.tab_settings),
    )
    val dictionaryState = state.settings.dictionaryManagement
    val selectedType = dictionaryState.selectedType
    val dictionaries = dictionaryState.dictionaries()
    val isBusy = dictionaryState.isImporting || dictionaryState.isUpdating
    var importType by remember { mutableStateOf(DictionaryType.Term) }
    var showImportPopup by remember { mutableStateOf(false) }
    val launchZipPicker = rememberDictionaryZipPicker { uris ->
        onIntent(AppIntent.ImportDictionaries(importType, uris))
    }
    val pagerState = rememberPagerState(pageCount = { dictionarySettingsPages.size })
    val pagerCoroutineScope = rememberCoroutineScope()
    val selectedPage = pagerState.currentPage
    val launchZipPickerForType: (DictionaryType) -> Unit = { type ->
        importType = type
        launchZipPicker()
    }
    val hapticFeedback = LocalHapticFeedback.current
    val onIntentState by rememberUpdatedState(onIntent)
    var localDictionaries by remember(selectedType) { mutableStateOf(dictionaries) }
    var pendingDeletion by remember { mutableStateOf<PendingDictionaryDeletion?>(null) }

    LaunchedEffect(dictionaries) {
        localDictionaries = dictionaries
    }

    val listState = rememberLazyListState()
    val dictionaryListStartIndex = remember(
        selectedPage,
        dictionaryState.errorMessage,
        dictionaryState.statusText,
        dictionaryState.isLoading,
    ) {
        if (selectedPage != 0) {
            -1
        } else {
            var index = 0
            if (dictionaryState.errorMessage != null) index += 1
            if (dictionaryState.statusText.isNotBlank() || dictionaryState.isLoading) index += 1
            index += 1 // DictionaryTypeTabs
            index
        }
    }
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        if (dictionaryListStartIndex < 0) return@rememberReorderableLazyListState

        val fromRelative = from.index - dictionaryListStartIndex
        val toRelative = to.index - dictionaryListStartIndex
        if (
            fromRelative !in localDictionaries.indices ||
            toRelative !in localDictionaries.indices
        ) {
            return@rememberReorderableLazyListState
        }

        localDictionaries = localDictionaries.toMutableList().apply {
            add(toRelative, removeAt(fromRelative))
        }
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LaunchedEffect(
        reorderableState.isAnyItemDragging,
        dictionaries,
        localDictionaries,
        selectedType
    ) {
        if (!reorderableState.isAnyItemDragging) {
            val updatedIds = localDictionaries.map(DictionaryInfo::id)
            if (updatedIds != dictionaries.map(DictionaryInfo::id)) {
                onIntentState(AppIntent.ReorderDictionaries(selectedType, updatedIds))
            }
        }
    }

    MoriPageScaffold(
        title = stringResource(Res.string.tab_dictionary),
        subtitle = "",
        blurEnabled = state.settings.blurEnabled,
        message = message,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
        actions = {},
        floatingActionButton = {
            if (selectedPage == 0) {
                Box {
                    FloatingActionButton(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(end = 6.dp, bottom = 6.dp),
                        onClick = {
                            if (!isBusy) {
                                showImportPopup = true
                            }
                        },
                        containerColor = MiuixTheme.colorScheme.background,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Add,
                            contentDescription = stringResource(Res.string.cd_import_dictionary),
                            modifier = Modifier.size(26.dp),
                            tint = MiuixTheme.colorScheme.primary,
                        )
                    }

                    WindowListPopup(
                        show = showImportPopup,
                        popupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
                        alignment = PopupPositionProvider.Align.BottomEnd,
                        onDismissRequest = { showImportPopup = false },
                        onDismissFinished = { showImportPopup = false },
                    ) {
                        val dismiss = LocalDismissState.current
                        ListPopupColumn {
                            DictionaryType.entries.forEachIndexed { index, type ->
                                DropdownImpl(
                                    text = when (type) {
                                        DictionaryType.Term -> "Term 词典"
                                        DictionaryType.Frequency -> "Frequency 词频"
                                        DictionaryType.Pitch -> "Pitch 音调"
                                    },
                                    optionSize = DictionaryType.entries.size,
                                    isSelected = false,
                                    index = index,
                                    onSelectedIndexChange = {
                                        launchZipPickerForType(type)
                                        dismiss?.invoke()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { paddingValues, scrollBehavior ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            PageTabs(
                pagerState = pagerState,
                tabs = dictionarySettingsPages,
                onSelectedPageChange = { page ->
                    pagerCoroutineScope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                },
                modifier = Modifier.padding(horizontal = DictionarySettingsHorizontalPadding),
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                beyondViewportPageCount = 1, // avoid page reloading
                key = { it },
            ) { page ->
                when (page) {
                    0 -> DictionaryManagementPage(
                        paddingValues = paddingValues,
                        scrollBehavior = scrollBehavior,
                        listState = listState,
                        errorMessage = dictionaryState.errorMessage,
                        statusText = dictionaryState.statusText,
                        isLoading = dictionaryState.isLoading,
                        selectedType = selectedType,
                        dictionaries = dictionaries,
                        localDictionaries = localDictionaries,
                        isBusy = isBusy,
                        reorderableState = reorderableState,
                        onIntent = onIntent,
                        onDeleteRequest = { type, dictionary ->
                            pendingDeletion = PendingDictionaryDeletion(type, dictionary)
                        },
                    )

                    else -> DictionaryLookupSettingsPage(
                        state = state,
                        paddingValues = paddingValues,
                        scrollBehavior = scrollBehavior,
                        onIntent = onIntent,
                    )
                }
            }

            DeleteDictionaryDialog(
                pendingDeletion = pendingDeletion,
                onDismiss = { pendingDeletion = null },
                onConfirm = { deletion ->
                    onIntent(AppIntent.DeleteDictionary(deletion.type, deletion.dictionary.id))
                    pendingDeletion = null
                },
            )
        }
    }
}

@Composable
private fun PageTabs(
    pagerState: PagerState,
    tabs: List<String>,
    onSelectedPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabRowWithContour(
        tabs = tabs,
        selectedTabIndex = pagerState.currentPage,
        onTabSelected = onSelectedPageChange,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun DictionaryManagementPage(
    paddingValues: PaddingValues,
    scrollBehavior: top.yukonga.miuix.kmp.basic.ScrollBehavior,
    listState: androidx.compose.foundation.lazy.LazyListState,
    errorMessage: String?,
    statusText: String,
    isLoading: Boolean,
    selectedType: DictionaryType,
    dictionaries: List<DictionaryInfo>,
    localDictionaries: List<DictionaryInfo>,
    isBusy: Boolean,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onIntent: (AppIntent) -> Unit,
    onDeleteRequest: (DictionaryType, DictionaryInfo) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(
            top = 14.dp,
            bottom = paddingValues.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        errorMessage?.let { message ->
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = DictionarySettingsHorizontalPadding)
                        .fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(text = message, color = MiuixTheme.colorScheme.error)
                        TextButton(
                            text = stringResource(Res.string.cd_close),
                            onClick = { onIntent(AppIntent.DismissDictionaryError) },
                        )
                    }
                }
            }
        }

        if (statusText.isNotBlank() || isLoading) {
            item {
                LoadingRow(
                    text = statusText.ifBlank { stringResource(Res.string.dict_settings_loading) },
                    modifier = Modifier.padding(horizontal = DictionarySettingsHorizontalPadding),
                )
            }
        }

        item {
            DictionaryTypeTabs(
                selectedType = selectedType,
                enabled = !isBusy,
                onSelectType = { onIntent(AppIntent.SelectDictionaryType(it)) },
                modifier = Modifier.padding(horizontal = DictionarySettingsHorizontalPadding),
            )
        }

        if (dictionaries.isEmpty() && !isLoading) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = DictionarySettingsHorizontalPadding)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(Res.string.dict_settings_empty),
                        modifier = Modifier.padding(18.dp),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        } else {
            dictionaryItems(
                dictionaries = localDictionaries,
                type = selectedType,
                enabled = !isBusy,
                reorderableState = reorderableState,
                onIntent = onIntent,
                onDeleteRequest = onDeleteRequest,
            )
        }
    }
}

@Composable
private fun DictionaryLookupSettingsPage(
    state: AppState,
    paddingValues: PaddingValues,
    scrollBehavior: top.yukonga.miuix.kmp.basic.ScrollBehavior,
    onIntent: (AppIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(
            bottom = paddingValues.calculateBottomPadding() + 96.dp,
        ),
    ) {
        item {
            SmallTitle(text = stringResource(Res.string.dict_settings_query_display))
        }
        item {
            DictionaryLookupSettingsCard(
                state = state,
                modifier = Modifier.padding(horizontal = DictionarySettingsHorizontalPadding),
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun DictionaryTypeTabs(
    selectedType: DictionaryType,
    enabled: Boolean,
    onSelectType: (DictionaryType) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabRowWithContour(
        tabs = DictionaryType.entries.map { it.label },
        selectedTabIndex = DictionaryType.entries.indexOf(selectedType),
        onTabSelected = { index ->
            if (enabled) {
                onSelectType(DictionaryType.entries[index])
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun LoadingRow(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LinearProgressIndicator()
        Text(
            text = text,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}

@Composable
private fun DictionaryLookupSettingsCard(
    state: AppState,
    modifier: Modifier = Modifier,
    onIntent: (AppIntent) -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SettingSlider(
                    label = stringResource(Res.string.dict_settings_max_results),
                    value = state.settings.maxResults.toFloat(),
                    range = 1f..50f,
                    steps = 48,
                    keyPoints = listOf(1f, 8f, 16f, 24f, 32f, 40f, 50f),
                    valueText = { it.toInt().toString() },
                    onCommit = { onIntent(AppIntent.SetMaxResults(it.toInt())) },
                )
                SettingSlider(
                    label = stringResource(Res.string.dict_settings_scan_length),
                    value = state.settings.scanLength.toFloat(),
                    range = 1f..64f,
                    steps = 62,
                    keyPoints = listOf(1f, 8f, 16f, 24f, 32f, 48f, 64f),
                    valueText = { it.toInt().toString() },
                    onCommit = { onIntent(AppIntent.SetScanLength(it.toInt())) },
                )
            }
            SwitchPreference(
                checked = state.settings.collapseDictionaries,
                onCheckedChange = { onIntent(AppIntent.SetCollapseDictionaries(it)) },
                title = stringResource(Res.string.dict_settings_collapse_title),
                summary = stringResource(Res.string.dict_settings_collapse_summary),
            )
            SwitchPreference(
                checked = state.settings.compactGlossaries,
                onCheckedChange = { onIntent(AppIntent.SetCompactGlossaries(it)) },
                title = stringResource(Res.string.dict_settings_compact_title),
                summary = stringResource(Res.string.dict_settings_compact_summary),
            )
            SwitchPreference(
                checked = state.settings.showExpressionTags,
                onCheckedChange = { onIntent(AppIntent.SetShowExpressionTags(it)) },
                title = stringResource(Res.string.dict_settings_show_tags_title),
                summary = stringResource(Res.string.dict_settings_show_tags_summary),
            )
            SwitchPreference(
                checked = state.settings.harmonicFrequency,
                onCheckedChange = { onIntent(AppIntent.SetHarmonicFrequency(it)) },
                title = stringResource(Res.string.dict_settings_merge_freq_title),
                summary = stringResource(Res.string.dict_settings_merge_freq_summary),
            )
            SwitchPreference(
                checked = state.settings.deduplicatePitchAccents,
                onCheckedChange = { onIntent(AppIntent.SetDeduplicatePitchAccents(it)) },
                title = stringResource(Res.string.dict_settings_dedup_pitch_title),
                summary = stringResource(Res.string.dict_settings_dedup_pitch_summary),
            )
        }
    }
}

private fun LazyListScope.dictionaryItems(
    dictionaries: List<DictionaryInfo>,
    type: DictionaryType,
    enabled: Boolean,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onIntent: (AppIntent) -> Unit,
    onDeleteRequest: (DictionaryType, DictionaryInfo) -> Unit,
) {
    items(
        items = dictionaries,
        key = { it.id },
    ) { dictionary ->
        DictionaryRow(
            dictionary = dictionary,
            type = type,
            enabled = enabled,
            reorderableState = reorderableState,
            onIntent = onIntent,
            onDeleteRequest = onDeleteRequest,
        )
    }
}

@Composable
private fun LazyItemScope.DictionaryRow(
    dictionary: DictionaryInfo,
    type: DictionaryType,
    enabled: Boolean,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onIntent: (AppIntent) -> Unit,
    onDeleteRequest: (DictionaryType, DictionaryInfo) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    ReorderableItem(
        state = reorderableState,
        key = dictionary.id,
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = DictionarySettingsHorizontalPadding)
                .fillMaxWidth()
                .then(
                    with(this) {
                        Modifier.longPressDraggableHandle(
                            onDragStarted = {
                                hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.GestureThresholdActivate,
                                )
                            },
                            onDragStopped = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                            },
                        )
                    },
                )
                .animateItem(),
        ) {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = dictionary.index.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                        color = if (dictionary.isEnabled) {
                            MiuixTheme.colorScheme.onSurface
                        } else {
                            MiuixTheme.colorScheme.onSurfaceVariantSummary
                        },
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "revision ${dictionary.index.revision.ifBlank { "-" }}",
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp,
                        )
//                        if (dictionary.isUpdatable) {
//                            Text(
//                                text = "可更新",
//                                color = MiuixTheme.colorScheme.primary,
//                                fontWeight = FontWeight.Medium,
//                            )
//                        }
                    }
                }
                Icon(
                    imageVector = MiuixIcons.Sort,
                    contentDescription = stringResource(Res.string.cd_drag_sort),
                    tint = if (enabled) {
                        MiuixTheme.colorScheme.onSurfaceVariantSummary
                    } else {
                        MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.55f)
                    },
                )
                Switch(
                    checked = dictionary.isEnabled,
                    onCheckedChange = {
                        onIntent(AppIntent.SetDictionaryEnabled(type, dictionary.id, it))
                    },
                    enabled = enabled,
                )
                IconButton(
                    enabled = enabled,
                    onClick = { onDeleteRequest(type, dictionary) },
                ) {
                    Icon(
                        imageVector = MiuixIcons.Delete,
                        contentDescription = stringResource(Res.string.cd_delete_dictionary),
                        tint = if (enabled) MiuixTheme.colorScheme.error else Color.Gray,
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteDictionaryDialog(
    pendingDeletion: PendingDictionaryDeletion?,
    onDismiss: () -> Unit,
    onConfirm: (PendingDictionaryDeletion) -> Unit,
) {
    val current = pendingDeletion ?: return
    OverlayDialog(
        title = stringResource(Res.string.cd_delete_dictionary),
        summary = stringResource(Res.string.dict_settings_delete_summary),
        show = true,
        onDismissRequest = onDismiss,
        onDismissFinished = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "确认删除「${current.dictionary.index.title}」吗？",
                modifier = Modifier.fillMaxWidth(),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    text = stringResource(Res.string.btn_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(Res.string.btn_delete),
                    onClick = { onConfirm(current) },
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
