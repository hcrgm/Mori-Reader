package app.mori.reader.ui.pages.settings.dictionary

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import app.mori.reader.core.platform.rememberDictionaryZipPicker
import app.mori.reader.data.dictionary.DictionaryInfo
import app.mori.reader.data.dictionary.DictionaryType
import app.mori.reader.features.settings.presentation.DictionaryManagementState
import app.mori.reader.features.settings.presentation.SettingsIntent
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

internal class DictionarySettingsController(
    val listState: LazyListState,
    val reorderableState: ReorderableLazyListState,
    val localDictionaries: List<DictionaryInfo>,
    val pendingDeletion: PendingDictionaryDeletion?,
    val launchZipPickerForType: (DictionaryType) -> Unit,
    val requestDeletion: (DictionaryType, DictionaryInfo) -> Unit,
    val dismissDeletion: () -> Unit,
    val confirmDeletion: (PendingDictionaryDeletion) -> Unit,
)

@Composable
internal fun rememberDictionarySettingsController(
    dictionaryState: DictionaryManagementState,
    selectedPage: Int,
    onIntent: (SettingsIntent) -> Unit,
): DictionarySettingsController {
    val selectedType = dictionaryState.selectedType
    val dictionaries = dictionaryState.dictionaries()
    var importType by remember { mutableStateOf(DictionaryType.Term) }
    var localDictionaries by remember(selectedType) { mutableStateOf(dictionaries) }
    var pendingDeletion by remember { mutableStateOf<PendingDictionaryDeletion?>(null) }
    val hapticFeedback = LocalHapticFeedback.current
    val onIntentState by rememberUpdatedState(onIntent)
    val launchZipPicker =
        rememberDictionaryZipPicker { uris ->
            onIntentState(SettingsIntent.ImportDictionaries(importType, uris))
        }

    LaunchedEffect(dictionaries) {
        localDictionaries = dictionaries
    }

    val listState = rememberLazyListState()
    val dictionaryListStartIndex =
        remember(
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
                if (dictionaryState.statusText != null || dictionaryState.isLoading) index += 1
                index += 1
                index
            }
        }
    val reorderableState =
        rememberReorderableLazyListState(listState) { from, to ->
            if (dictionaryListStartIndex < 0) return@rememberReorderableLazyListState

            val fromRelative = from.index - dictionaryListStartIndex
            val toRelative = to.index - dictionaryListStartIndex
            if (
                fromRelative !in localDictionaries.indices ||
                toRelative !in localDictionaries.indices
            ) {
                return@rememberReorderableLazyListState
            }

            localDictionaries =
                localDictionaries.toMutableList().apply {
                    add(toRelative, removeAt(fromRelative))
                }
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }

    LaunchedEffect(
        reorderableState.isAnyItemDragging,
        dictionaries,
        localDictionaries,
        selectedType,
    ) {
        if (!reorderableState.isAnyItemDragging) {
            val updatedIds = localDictionaries.map(DictionaryInfo::id)
            if (updatedIds != dictionaries.map(DictionaryInfo::id)) {
                onIntentState(SettingsIntent.ReorderDictionaries(selectedType, updatedIds))
            }
        }
    }

    return DictionarySettingsController(
        listState = listState,
        reorderableState = reorderableState,
        localDictionaries = localDictionaries,
        pendingDeletion = pendingDeletion,
        launchZipPickerForType = { type ->
            importType = type
            launchZipPicker()
        },
        requestDeletion = { type, dictionary ->
            pendingDeletion = PendingDictionaryDeletion(type, dictionary)
        },
        dismissDeletion = {
            pendingDeletion = null
        },
        confirmDeletion = { deletion ->
            onIntentState(SettingsIntent.DeleteDictionary(deletion.type, deletion.dictionary.id))
            pendingDeletion = null
        },
    )
}
