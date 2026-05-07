package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
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
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_delete
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
import app.mori.reader.shared.generated.resources.dict_settings_delete_summary
import app.mori.reader.shared.generated.resources.dict_settings_empty
import app.mori.reader.shared.generated.resources.dict_settings_loading
import app.mori.reader.shared.generated.resources.dict_settings_max_results
import app.mori.reader.shared.generated.resources.dict_settings_merge_freq_summary
import app.mori.reader.shared.generated.resources.dict_settings_merge_freq_title
import app.mori.reader.shared.generated.resources.dict_settings_query_display
import app.mori.reader.shared.generated.resources.dict_settings_scan_length
import app.mori.reader.shared.generated.resources.dict_settings_show_tags_summary
import app.mori.reader.shared.generated.resources.dict_settings_show_tags_title
import app.mori.reader.shared.generated.resources.dict_settings_type_frequency
import app.mori.reader.shared.generated.resources.dict_settings_type_pitch
import app.mori.reader.shared.generated.resources.dict_settings_type_term
import app.mori.reader.ui.components.settings.SettingSlider
import app.mori.reader.ui.text.UiText
import app.mori.reader.ui.text.asString
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.window.WindowDialog

internal val DictionarySettingsHorizontalPadding = 12.dp

internal data class PendingDictionaryDeletion(
    val type: DictionaryType,
    val dictionary: DictionaryInfo,
)

@Composable
internal fun PageTabs(
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
internal fun DictionaryManagementPage(
    paddingValues: PaddingValues,
    scrollBehavior: ScrollBehavior,
    listState: androidx.compose.foundation.lazy.LazyListState,
    errorMessage: UiText?,
    statusText: UiText?,
    isLoading: Boolean,
    selectedType: DictionaryType,
    dictionaries: List<DictionaryInfo>,
    localDictionaries: List<DictionaryInfo>,
    isBusy: Boolean,
    reorderableState: ReorderableLazyListState,
    onIntent: (SettingsIntent) -> Unit,
    onDeleteRequest: (DictionaryType, DictionaryInfo) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier =
            Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding =
            PaddingValues(
                top = 14.dp,
                bottom = paddingValues.calculateBottomPadding() + 96.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        errorMessage?.let { message ->
            item {
                Card(
                    modifier =
                        Modifier
                            .padding(horizontal = DictionarySettingsHorizontalPadding)
                            .fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(text = message.asString(), color = MiuixTheme.colorScheme.error)
                        TextButton(
                            text = stringResource(Res.string.cd_close),
                            onClick = { onIntent(SettingsIntent.DismissDictionaryError) },
                        )
                    }
                }
            }
        }

        if (statusText != null || isLoading) {
            item {
                LoadingRow(
                    text =
                        statusText?.asString()
                            ?: stringResource(Res.string.dict_settings_loading),
                    modifier = Modifier.padding(horizontal = DictionarySettingsHorizontalPadding),
                )
            }
        }

        item {
            DictionaryTypeTabs(
                selectedType = selectedType,
                enabled = !isBusy,
                onSelectType = { onIntent(SettingsIntent.SelectDictionaryType(it)) },
                modifier = Modifier.padding(horizontal = DictionarySettingsHorizontalPadding),
            )
        }

        if (dictionaries.isEmpty() && !isLoading) {
            item {
                Card(
                    modifier =
                        Modifier
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
internal fun DictionaryLookupSettingsPage(
    settings: AppSettings,
    paddingValues: PaddingValues,
    scrollBehavior: ScrollBehavior,
    onIntent: (SettingsIntent) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding =
            PaddingValues(
                bottom = paddingValues.calculateBottomPadding() + 96.dp,
            ),
    ) {
        item {
            SmallTitle(text = stringResource(Res.string.dict_settings_query_display))
        }
        item {
            DictionaryLookupSettingsCard(
                settings = settings,
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
        tabs = DictionaryType.entries.map { it.localizedLabel() },
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
    settings: AppSettings,
    modifier: Modifier = Modifier,
    onIntent: (SettingsIntent) -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SettingSlider(
                    label = stringResource(Res.string.dict_settings_max_results),
                    value = settings.dictionary.maxResults.toFloat(),
                    range = 1f..50f,
                    steps = 48,
                    keyPoints = listOf(1f, 8f, 16f, 24f, 32f, 40f, 50f),
                    valueText = { it.toInt().toString() },
                    onCommit = { onIntent(SettingsIntent.SetMaxResults(it.toInt())) },
                )
                SettingSlider(
                    label = stringResource(Res.string.dict_settings_scan_length),
                    value = settings.dictionary.scanLength.toFloat(),
                    range = 1f..64f,
                    steps = 62,
                    keyPoints = listOf(1f, 8f, 16f, 24f, 32f, 48f, 64f),
                    valueText = { it.toInt().toString() },
                    onCommit = { onIntent(SettingsIntent.SetScanLength(it.toInt())) },
                )
            }
            SwitchPreference(
                checked = settings.dictionary.collapseDictionaries,
                onCheckedChange = { onIntent(SettingsIntent.SetCollapseDictionaries(it)) },
                title = stringResource(Res.string.dict_settings_collapse_title),
                summary = stringResource(Res.string.dict_settings_collapse_summary),
            )
            SwitchPreference(
                checked = settings.dictionary.compactGlossaries,
                onCheckedChange = { onIntent(SettingsIntent.SetCompactGlossaries(it)) },
                title = stringResource(Res.string.dict_settings_compact_title),
                summary = stringResource(Res.string.dict_settings_compact_summary),
            )
            SwitchPreference(
                checked = settings.dictionary.showExpressionTags,
                onCheckedChange = { onIntent(SettingsIntent.SetShowExpressionTags(it)) },
                title = stringResource(Res.string.dict_settings_show_tags_title),
                summary = stringResource(Res.string.dict_settings_show_tags_summary),
            )
            SwitchPreference(
                checked = settings.dictionary.harmonicFrequency,
                onCheckedChange = { onIntent(SettingsIntent.SetHarmonicFrequency(it)) },
                title = stringResource(Res.string.dict_settings_merge_freq_title),
                summary = stringResource(Res.string.dict_settings_merge_freq_summary),
            )
            SwitchPreference(
                checked = settings.dictionary.deduplicatePitchAccents,
                onCheckedChange = { onIntent(SettingsIntent.SetDeduplicatePitchAccents(it)) },
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
    reorderableState: ReorderableLazyListState,
    onIntent: (SettingsIntent) -> Unit,
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
    reorderableState: ReorderableLazyListState,
    onIntent: (SettingsIntent) -> Unit,
    onDeleteRequest: (DictionaryType, DictionaryInfo) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current

    ReorderableItem(
        state = reorderableState,
        key = dictionary.id,
    ) {
        Card(
            modifier =
                Modifier
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
                    ).animateItem(),
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
                        color =
                            if (dictionary.isEnabled) {
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
                    }
                }
                Icon(
                    imageVector = MiuixIcons.Sort,
                    contentDescription = stringResource(Res.string.cd_drag_sort),
                    tint =
                        if (enabled) {
                            MiuixTheme.colorScheme.onSurfaceVariantSummary
                        } else {
                            MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.55f)
                        },
                )
                Switch(
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
internal fun DeleteDictionaryDialog(
    pendingDeletion: PendingDictionaryDeletion?,
    onDismiss: () -> Unit,
    onConfirm: (PendingDictionaryDeletion) -> Unit,
) {
    val current = pendingDeletion ?: return
    WindowDialog(
        title = stringResource(Res.string.cd_delete_dictionary),
        summary = stringResource(Res.string.dict_settings_delete_summary),
        show = true,
        onDismissRequest = onDismiss,
        onDismissFinished = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text =
                    stringResource(
                        Res.string.dict_settings_delete_confirm,
                        current.dictionary.index.title,
                    ),
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

@Composable
internal fun DictionaryType.localizedLabel(): String =
    when (this) {
        DictionaryType.Term -> stringResource(Res.string.dict_settings_type_term)
        DictionaryType.Frequency -> stringResource(Res.string.dict_settings_type_frequency)
        DictionaryType.Pitch -> stringResource(Res.string.dict_settings_type_pitch)
    }
