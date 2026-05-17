package app.mori.reader.ui.pages.settings.dictionary

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.mori.reader.data.dictionary.DictionaryType
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.settings.presentation.DictionaryManagementState
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.cd_import_dictionary
import app.mori.reader.shared.generated.resources.tab_dictionary
import app.mori.reader.shared.generated.resources.tab_settings
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowListPopup

@Composable
internal fun MiuixDictionarySettingsPage(
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
    var showImportPopup by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { dictionarySettingsPages.size })
    val pagerCoroutineScope = rememberCoroutineScope()
    val selectedPage = pagerState.currentPage
    val controller =
        rememberDictionarySettingsController(
            dictionaryState = dictionaryState,
            selectedPage = selectedPage,
            onIntent = onIntent,
        )

    MoriPageScaffold(
        title = stringResource(Res.string.tab_dictionary),
        subtitle = "",
        blurEnabled = settings.appearance.blurEnabled,
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
                        modifier =
                            Modifier
                                .navigationBarsPadding()
                                .padding(end = 20.dp, bottom = 20.dp)
                                .border(
                                    width = 0.05.dp,
                                    color = MiuixTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = CircleShape,
                                ),
                        onClick = {
                            if (!isBusy) {
                                showImportPopup = true
                            }
                        },
                        containerColor = MiuixTheme.colorScheme.primary,
                        shadowElevation = 0.dp,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Add,
                            contentDescription = stringResource(Res.string.cd_import_dictionary),
                            modifier = Modifier.size(26.dp),
                            tint = MiuixTheme.colorScheme.onPrimary,
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
                                    text = type.localizedLabel(),
                                    optionSize = DictionaryType.entries.size,
                                    isSelected = false,
                                    index = index,
                                    onSelectedIndexChange = {
                                        controller.launchZipPickerForType(type)
                                        dismiss?.invoke()
                                    },
                                )
                            }
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
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                beyondViewportPageCount = 1, // avoid page reloading
                key = { it },
            ) { page ->
                when (page) {
                    0 -> {
                        DictionaryManagementPage(
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
                        )
                    }

                    else -> {
                        DictionaryLookupSettingsPage(
                            settings = settings,
                            paddingValues = paddingValues,
                            onIntent = onIntent,
                        )
                    }
                }
            }

            DeleteDictionaryDialog(
                pendingDeletion = controller.pendingDeletion,
                onDismiss = controller.dismissDeletion,
                onConfirm = controller.confirmDeletion,
            )
            DictionaryImportFeedback(
                dictionaryState = dictionaryState,
                onDismissSummary = {
                    onIntent(SettingsIntent.DismissDictionaryImportSummary)
                },
            )
        }
    }
}
