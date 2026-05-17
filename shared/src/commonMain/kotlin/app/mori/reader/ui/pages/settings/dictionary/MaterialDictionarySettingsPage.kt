package app.mori.reader.ui.pages.settings.dictionary

import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.dictionary.DictionaryInfo
import app.mori.reader.data.dictionary.DictionaryType
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.settings.presentation.DictionaryManagementState
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_import
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_delete
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.cd_close
import app.mori.reader.shared.generated.resources.cd_delete_dictionary
import app.mori.reader.shared.generated.resources.cd_drag_sort
import app.mori.reader.shared.generated.resources.dict_settings_collapse_summary
import app.mori.reader.shared.generated.resources.dict_settings_collapse_title
import app.mori.reader.shared.generated.resources.dict_settings_compact_summary
import app.mori.reader.shared.generated.resources.dict_settings_compact_title
import app.mori.reader.shared.generated.resources.dict_settings_dedup_pitch_summary
import app.mori.reader.shared.generated.resources.dict_settings_dedup_pitch_title
import app.mori.reader.shared.generated.resources.dict_settings_delete_confirm
import app.mori.reader.shared.generated.resources.dict_settings_empty
import app.mori.reader.shared.generated.resources.dict_settings_loading
import app.mori.reader.shared.generated.resources.dict_settings_max_results
import app.mori.reader.shared.generated.resources.dict_settings_merge_freq_summary
import app.mori.reader.shared.generated.resources.dict_settings_merge_freq_title
import app.mori.reader.shared.generated.resources.dict_settings_query_display
import app.mori.reader.shared.generated.resources.dict_settings_scan_length
import app.mori.reader.shared.generated.resources.dict_settings_show_tags_summary
import app.mori.reader.shared.generated.resources.dict_settings_show_tags_title
import app.mori.reader.shared.generated.resources.tab_dictionary
import app.mori.reader.shared.generated.resources.tab_settings
import app.mori.reader.ui.components.material.MaterialBackButton
import app.mori.reader.ui.components.material.MaterialExpressiveSwitch
import app.mori.reader.ui.components.material.materialCardBorder
import app.mori.reader.ui.components.navigation.eInkPagerSwipeModifier
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MaterialSettingsGroup
import app.mori.reader.ui.components.settings.MaterialSettingsSection
import app.mori.reader.ui.components.settings.MaterialSettingsSurface
import app.mori.reader.ui.components.settings.materialSettingsSegmentedItemShape
import app.mori.reader.ui.text.UiText
import app.mori.reader.ui.text.asString
import app.mori.reader.ui.theme.MoriTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MaterialDictionarySettingsPage(
    settings: AppSettings,
    dictionaryState: DictionaryManagementState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    val dictionarySettingsPages =
        listOf(
            stringResource(Res.string.tab_dictionary),
            stringResource(Res.string.tab_settings),
        )
    val selectedType = dictionaryState.selectedType
    val dictionaries = dictionaryState.dictionaries()
    val isBusy = dictionaryState.isImporting || dictionaryState.isUpdating
    var importMenuExpanded by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { dictionarySettingsPages.size })
    val pagerCoroutineScope = rememberCoroutineScope()
    val selectedPage = pagerState.currentPage
    val reduceMotion = MoriTheme.materialEInkMode
    val defaultOverscrollEffect = rememberOverscrollEffect()
    val defaultFlingBehavior = PagerDefaults.flingBehavior(state = pagerState)
    val instantFlingBehavior =
        PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = tween(durationMillis = 0),
        )
    val flingBehavior = if (reduceMotion) instantFlingBehavior else defaultFlingBehavior
    val controller =
        rememberDictionarySettingsController(
            dictionaryState = dictionaryState,
            selectedPage = selectedPage,
            onIntent = onIntent,
        )

    MoriPageScaffold(
        title = stringResource(Res.string.tab_dictionary),
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            Row {
                MaterialBackButton(onClick = onBack, contentDescription = stringResource(Res.string.cd_back))
                Spacer(modifier = Modifier.size(16.dp))
            }
        },
        floatingActionButton = {
            if (selectedPage == 0) {
                Box(
                    modifier =
                        Modifier
                            .navigationBarsPadding()
                            .padding(end = 8.dp, bottom = 8.dp),
                ) {
                    SmallExtendedFloatingActionButton(
                        onClick = {
                            if (!isBusy) {
                                importMenuExpanded = true
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                        )
                        Text(text = stringResource(Res.string.btn_import))
                    }
                    DropdownMenu(
                        expanded = importMenuExpanded,
                        onDismissRequest = { importMenuExpanded = false },
                    ) {
                        DictionaryType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(text = type.localizedLabel()) },
                                onClick = {
                                    importMenuExpanded = false
                                    controller.launchZipPickerForType(type)
                                },
                            )
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
        ) {
            PrimaryTabRow(selectedTabIndex = selectedPage) {
                dictionarySettingsPages.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedPage == index,
                        onClick = {
                            pagerCoroutineScope.launch {
                                if (reduceMotion) {
                                    pagerState.scrollToPage(index)
                                } else {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        },
                        text = { Text(text = title) },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .eInkPagerSwipeModifier(
                            enabled = reduceMotion,
                            currentPage = pagerState.currentPage,
                            pageCount = dictionarySettingsPages.size,
                            onPageChange = { page ->
                                pagerCoroutineScope.launch {
                                    pagerState.scrollToPage(page)
                                }
                            },
                        ),
                beyondViewportPageCount = 1,
                flingBehavior = flingBehavior,
                userScrollEnabled = !reduceMotion,
                overscrollEffect = if (reduceMotion) null else defaultOverscrollEffect,
                key = { it },
            ) { page ->
                when (page) {
                    0 -> {
                        MaterialDictionaryManagementPage(
                            paddingValues = paddingValues,
                            listState = controller.listState,
                            errorMessage = dictionaryState.errorMessage,
                            statusText = dictionaryState.statusText,
                            isLoading = dictionaryState.isLoading,
                            selectedType = selectedType,
                            dictionaries = dictionaries,
                            localDictionaries = controller.localDictionaries,
                            isBusy = isBusy,
                            reorderableState = controller.reorderableState,
                            onIntent = onIntent,
                            onDeleteRequest = controller.requestDeletion,
                            modifier = Modifier,
                        )
                    }

                    else -> {
                        MaterialDictionaryLookupSettingsPage(
                            settings = settings,
                            paddingValues = paddingValues,
                            onIntent = onIntent,
                            modifier = Modifier,
                        )
                    }
                }
            }
        }
    }

    MaterialDeleteDictionaryDialog(
        pendingDeletion = controller.pendingDeletion,
        onDismiss = controller.dismissDeletion,
        onConfirm = controller.confirmDeletion,
    )
}

@Composable
private fun MaterialDictionaryManagementPage(
    paddingValues: PaddingValues,
    listState: androidx.compose.foundation.lazy.LazyListState,
    errorMessage: UiText?,
    statusText: UiText?,
    isLoading: Boolean,
    selectedType: DictionaryType,
    dictionaries: List<DictionaryInfo>,
    localDictionaries: List<DictionaryInfo>,
    isBusy: Boolean,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onIntent: (SettingsIntent) -> Unit,
    onDeleteRequest: (DictionaryType, DictionaryInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier =
            modifier
                .fillMaxSize(),
        contentPadding =
            PaddingValues(
                top = 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 96.dp,
                start = 16.dp,
                end = 16.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        errorMessage?.let { message ->
            item {
                MaterialStatusCard(
                    text = message.asString(),
                    textColor = MaterialTheme.colorScheme.error,
                    actionText = stringResource(Res.string.cd_close),
                    onActionClick = { onIntent(SettingsIntent.DismissDictionaryError) },
                )
            }
        }

        if (statusText != null || isLoading) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        text =
                            statusText?.asString()
                                ?: stringResource(Res.string.dict_settings_loading),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        item {
            MaterialDictionaryTypeTabs(
                selectedType = selectedType,
                enabled = !isBusy,
                onSelectType = { onIntent(SettingsIntent.SelectDictionaryType(it)) },
            )
        }

        if (dictionaries.isEmpty() && !isLoading) {
            item {
                MaterialSettingsSurface(shape = MaterialTheme.shapes.large) {
                    Text(
                        text = stringResource(Res.string.dict_settings_empty),
                        modifier = Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            itemsIndexed(
                items = localDictionaries,
                key = { _, dictionary -> dictionary.id },
            ) { index, dictionary ->
                MaterialDictionaryRow(
                    dictionary = dictionary,
                    index = index,
                    count = localDictionaries.size,
                    type = selectedType,
                    enabled = !isBusy,
                    reorderableState = reorderableState,
                    onIntent = onIntent,
                    onDeleteRequest = onDeleteRequest,
                )
            }
        }
    }
}

@Composable
private fun MaterialDictionaryLookupSettingsPage(
    settings: AppSettings,
    paddingValues: PaddingValues,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize(),
        contentPadding =
            PaddingValues(
                top = 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 96.dp,
                start = 16.dp,
                end = 16.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            MaterialSettingsSection(title = stringResource(Res.string.dict_settings_query_display)) {
                MaterialSettingsGroup {
                    MaterialSliderSetting(
                        title = stringResource(Res.string.dict_settings_max_results),
                        value = settings.dictionary.maxResults.toFloat(),
                        valueRange = 1f..50f,
                        steps = 48,
                        valueText = settings.dictionary.maxResults.toString(),
                        shape = materialSettingsSegmentedItemShape(index = 0, count = 7),
                        showDivider = false,
                        onValueChangeFinished = { onIntent(SettingsIntent.SetMaxResults(it.toInt())) },
                    )
                    MaterialSliderSetting(
                        title = stringResource(Res.string.dict_settings_scan_length),
                        value = settings.dictionary.scanLength.toFloat(),
                        valueRange = 1f..64f,
                        steps = 62,
                        valueText = settings.dictionary.scanLength.toString(),
                        shape = materialSettingsSegmentedItemShape(index = 1, count = 7),
                        showDivider = true,
                        onValueChangeFinished = { onIntent(SettingsIntent.SetScanLength(it.toInt())) },
                    )
                    MaterialSwitchSetting(
                        titleRes = Res.string.dict_settings_collapse_title,
                        summaryRes = Res.string.dict_settings_collapse_summary,
                        checked = settings.dictionary.collapseDictionaries,
                        shape = materialSettingsSegmentedItemShape(index = 2, count = 7),
                        showDivider = true,
                        onCheckedChange = { onIntent(SettingsIntent.SetCollapseDictionaries(it)) },
                    )
                    MaterialSwitchSetting(
                        titleRes = Res.string.dict_settings_compact_title,
                        summaryRes = Res.string.dict_settings_compact_summary,
                        checked = settings.dictionary.compactGlossaries,
                        shape = materialSettingsSegmentedItemShape(index = 3, count = 7),
                        showDivider = true,
                        onCheckedChange = { onIntent(SettingsIntent.SetCompactGlossaries(it)) },
                    )
                    MaterialSwitchSetting(
                        titleRes = Res.string.dict_settings_show_tags_title,
                        summaryRes = Res.string.dict_settings_show_tags_summary,
                        checked = settings.dictionary.showExpressionTags,
                        shape = materialSettingsSegmentedItemShape(index = 4, count = 7),
                        showDivider = true,
                        onCheckedChange = { onIntent(SettingsIntent.SetShowExpressionTags(it)) },
                    )
                    MaterialSwitchSetting(
                        titleRes = Res.string.dict_settings_merge_freq_title,
                        summaryRes = Res.string.dict_settings_merge_freq_summary,
                        checked = settings.dictionary.harmonicFrequency,
                        shape = materialSettingsSegmentedItemShape(index = 5, count = 7),
                        showDivider = true,
                        onCheckedChange = { onIntent(SettingsIntent.SetHarmonicFrequency(it)) },
                    )
                    MaterialSwitchSetting(
                        titleRes = Res.string.dict_settings_dedup_pitch_title,
                        summaryRes = Res.string.dict_settings_dedup_pitch_summary,
                        checked = settings.dictionary.deduplicatePitchAccents,
                        shape = materialSettingsSegmentedItemShape(index = 6, count = 7),
                        showDivider = true,
                        onCheckedChange = { onIntent(SettingsIntent.SetDeduplicatePitchAccents(it)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialDictionaryTypeTabs(
    selectedType: DictionaryType,
    enabled: Boolean,
    onSelectType: (DictionaryType) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth(),
    ) {
        DictionaryType.entries.forEachIndexed { index, type ->
            val selected = selectedType == type
            SegmentedButton(
                selected = selected,
                enabled = enabled,
                onClick = { onSelectType(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = DictionaryType.entries.size),
                label = {
                    Text(
                        text = type.localizedLabel(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Composable
private fun LazyItemScope.MaterialDictionaryRow(
    dictionary: DictionaryInfo,
    index: Int,
    count: Int,
    type: DictionaryType,
    enabled: Boolean,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onIntent: (SettingsIntent) -> Unit,
    onDeleteRequest: (DictionaryType, DictionaryInfo) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    ReorderableItem(
        state = reorderableState,
        key = dictionary.id,
    ) {
        MaterialSettingsSurface(
            modifier =
                Modifier
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
                    ).animateItem(),
            shape = materialSettingsSegmentedItemShape(index = index, count = count),
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = dictionary.index.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                    )
                },
                supportingContent = {
                    Text(
                        text = "revision ${dictionary.index.revision.ifBlank { "-" }}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(Res.string.cd_drag_sort),
                        tint =
                            if (enabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            },
                    )
                },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MaterialExpressiveSwitch(
                            checked = dictionary.isEnabled,
                            onCheckedChange = {
                                onIntent(SettingsIntent.SetDictionaryEnabled(type, dictionary.id, it))
                            },
                            enabled = enabled,
                        )
                        IconButton(
                            enabled = enabled,
                            onClick = { onDeleteRequest(type, dictionary) },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(Res.string.cd_delete_dictionary),
                                tint =
                                    if (enabled) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                    },
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@Composable
private fun MaterialStatusCard(
    text: String,
    textColor: Color,
    actionText: String,
    onActionClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = materialCardBorder(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = text, color = textColor)
            TextButton(onClick = onActionClick) {
                Text(text = actionText)
            }
        }
    }
}

@Composable
private fun MaterialSliderSetting(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueText: String,
    shape: Shape,
    showDivider: Boolean,
    onValueChangeFinished: (Float) -> Unit,
) {
    var currentValue by remember(value) { mutableStateOf(value) }
    MaterialSettingsSurface(
        shape = shape,
        groupedInSection = true,
        showDivider = showDivider,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = valueText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Slider(
                value = currentValue,
                onValueChange = { currentValue = it },
                valueRange = valueRange,
                steps = steps,
                onValueChangeFinished = { onValueChangeFinished(currentValue) },
            )
        }
    }
}

@Composable
private fun MaterialSwitchSetting(
    titleRes: StringResource,
    summaryRes: StringResource,
    checked: Boolean,
    shape: Shape,
    showDivider: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    MaterialSettingsSurface(
        shape = shape,
        groupedInSection = true,
        showDivider = showDivider,
        onClick = { onCheckedChange(!checked) },
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(titleRes)) },
            supportingContent = { Text(text = stringResource(summaryRes)) },
            trailingContent = {
                MaterialExpressiveSwitch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun MaterialDeleteDictionaryDialog(
    pendingDeletion: PendingDictionaryDeletion?,
    onDismiss: () -> Unit,
    onConfirm: (PendingDictionaryDeletion) -> Unit,
) {
    val current = pendingDeletion ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.cd_delete_dictionary)) },
        text = {
            Text(
                text =
                    stringResource(
                        Res.string.dict_settings_delete_confirm,
                        current.dictionary.index.title,
                    ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(current) }) {
                Text(text = stringResource(Res.string.btn_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.btn_cancel))
            }
        },
    )
}
