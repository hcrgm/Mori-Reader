package app.mori.reader.ui.pages.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.core.platform.rememberAnkiDroidPermissionRequester
import app.mori.reader.data.anki.AnkiConnectionMode
import app.mori.reader.data.anki.AnkiDeck
import app.mori.reader.data.anki.AnkiDuplicateScope
import app.mori.reader.data.anki.AnkiField
import app.mori.reader.data.anki.AnkiHandlebar
import app.mori.reader.data.anki.AnkiNoteType
import app.mori.reader.data.anki.ankiPlatformCapabilities
import app.mori.reader.data.anki.defaultAnkiHandlebarTokens
import app.mori.reader.data.anki.lapisFieldMappings
import app.mori.reader.data.dictionary.DictionaryType
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.anki.presentation.AnkiIntent
import app.mori.reader.features.anki.presentation.AnkiState
import app.mori.reader.features.settings.presentation.DictionaryManagementState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.anki_allow_duplicates_summary
import app.mori.reader.shared.generated.resources.anki_allow_duplicates_title
import app.mori.reader.shared.generated.resources.anki_check_all_models_summary
import app.mori.reader.shared.generated.resources.anki_check_all_models_title
import app.mori.reader.shared.generated.resources.anki_compact_glossaries_summary
import app.mori.reader.shared.generated.resources.anki_compact_glossaries_title
import app.mori.reader.shared.generated.resources.anki_connection_mode_title
import app.mori.reader.shared.generated.resources.anki_connection_title
import app.mori.reader.shared.generated.resources.anki_deck_title
import app.mori.reader.shared.generated.resources.anki_duplicate_scope_collection
import app.mori.reader.shared.generated.resources.anki_duplicate_scope_deck
import app.mori.reader.shared.generated.resources.anki_duplicate_scope_note_type
import app.mori.reader.shared.generated.resources.anki_duplicate_scope_title
import app.mori.reader.shared.generated.resources.anki_embed_media_summary
import app.mori.reader.shared.generated.resources.anki_embed_media_title
import app.mori.reader.shared.generated.resources.anki_field_mapping_title
import app.mori.reader.shared.generated.resources.anki_force_sync_summary
import app.mori.reader.shared.generated.resources.anki_force_sync_title
import app.mori.reader.shared.generated.resources.anki_handlebars_title
import app.mori.reader.shared.generated.resources.anki_http_timeout_label
import app.mori.reader.shared.generated.resources.anki_http_url_label
import app.mori.reader.shared.generated.resources.anki_lapis_template_hint
import app.mori.reader.shared.generated.resources.anki_model_title
import app.mori.reader.shared.generated.resources.anki_no_decks
import app.mori.reader.shared.generated.resources.anki_no_models
import app.mori.reader.shared.generated.resources.anki_options_title
import app.mori.reader.shared.generated.resources.anki_tags_label
import app.mori.reader.shared.generated.resources.anki_title
import app.mori.reader.shared.generated.resources.btn_close
import app.mori.reader.shared.generated.resources.btn_refresh
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.value_none
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MoriWarningCard
import app.mori.reader.ui.text.asString
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.LocalDismissState
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.window.WindowListPopup

@Composable
fun AnkiSettingsPage(
    settings: AppSettings,
    ankiState: AnkiState,
    dictionaryState: DictionaryManagementState,
    onIntent: (AnkiIntent) -> Unit,
    onBack: () -> Unit,
) {
    val capabilities = remember { ankiPlatformCapabilities() }
    val handles = rememberAnkiHandlebars(dictionaryState)
    var showHandlebarsDialog by remember { mutableStateOf(false) }
    var errorCardMessage by remember { mutableStateOf("") }
    ankiState.errorMessage?.asString()?.let { errorCardMessage = it }
    val requestAnkiDroidPermission =
        rememberAnkiDroidPermissionRequester { granted ->
            if (granted) {
                onIntent(AnkiIntent.FetchDecksAndModels)
            } else {
                onIntent(AnkiIntent.CheckAvailability)
            }
        }
    MoriPageScaffold(
        title = stringResource(Res.string.anki_title),
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
        actions = {
            IconButton(onClick = { showHandlebarsDialog = true }) {
                Icon(MiuixIcons.Info, contentDescription = stringResource(Res.string.anki_handlebars_title))
            }
        },
    ) { paddingValues, scrollBehavior ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                AnimatedVisibility(
                    visible = ankiState.errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    MoriWarningCard(
                        text = errorCardMessage,
                        modifier = Modifier.padding(horizontal = AnkiSettingsHorizontalPadding),
                    )
                }
            }

            item {
                SmallTitle(text = stringResource(Res.string.anki_connection_title))
                Card(modifier = Modifier.padding(horizontal = AnkiSettingsHorizontalPadding)) {
                    WindowSpinnerPreference(
                        items = capabilities.availableModes.map { SpinnerEntry(title = it.label()) },
                        selectedIndex =
                            capabilities.availableModes
                                .indexOf(ankiState.settings.connectionMode)
                                .coerceAtLeast(0),
                        title = stringResource(Res.string.anki_connection_mode_title),
                        summary = ankiState.settings.connectionMode.label(),
                        onSelectedIndexChange = { index ->
                            onIntent(AnkiIntent.SetConnectionMode(capabilities.availableModes[index]))
                        },
                    )
                    AnimatedVisibility(
                        visible = ankiState.settings.connectionMode == AnkiConnectionMode.AnkiConnect,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        AnkiConnectSettings(
                            url = ankiState.settings.ankiConnect.url,
                            timeoutMillis = ankiState.settings.ankiConnect.timeoutMillis,
                            onUrlChange = { onIntent(AnkiIntent.SetAnkiConnectUrl(it)) },
                            onTimeoutChange = { value ->
                                value.toIntOrNull()?.let { onIntent(AnkiIntent.SetAnkiConnectTimeoutMillis(it)) }
                            },
                        )
                    }
                    AnkiFetchActions(
                        fetching = ankiState.isFetching,
                        onFetch = {
                            if (ankiState.settings.connectionMode == AnkiConnectionMode.AnkiDroid) {
                                requestAnkiDroidPermission()
                            } else {
                                onIntent(AnkiIntent.FetchDecksAndModels)
                            }
                        },
                    )
                }
            }

            item {
                SmallTitle(text = stringResource(Res.string.anki_field_mapping_title))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ankiState.settings.showLapisTemplateHint) {
                        MoriWarningCard(
                            text = stringResource(Res.string.anki_lapis_template_hint),
                            modifier = Modifier.padding(horizontal = AnkiSettingsHorizontalPadding),
                            onDismiss = { onIntent(AnkiIntent.SetShowLapisTemplateHint(false)) },
                            containerColor = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            textColor = MiuixTheme.colorScheme.primary,
                            dismissTint = MiuixTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Card(modifier = Modifier.padding(horizontal = AnkiSettingsHorizontalPadding)) {
                        DeckAndModelSelectors(
                            ankiState = ankiState,
                            onIntent = onIntent,
                        )
                        TagPreference(
                            tags = ankiState.settings.tags,
                            onValueChange = { value ->
                                onIntent(AnkiIntent.SetTags(value.split(Regex("\\s+"))))
                            },
                        )
                        FieldMappingEditor(
                            fields = ankiState.editableFields(),
                            mappings = ankiState.effectiveFieldMappings(),
                            handlebars = handles,
                            onIntent = onIntent,
                        )
                    }
                }
            }

            item {
                SmallTitle(text = stringResource(Res.string.anki_options_title))
                Card(modifier = Modifier.padding(horizontal = AnkiSettingsHorizontalPadding)) {
                    SwitchPreference(
                        checked = ankiState.settings.allowDuplicates,
                        onCheckedChange = { onIntent(AnkiIntent.SetAllowDuplicates(it)) },
                        title = stringResource(Res.string.anki_allow_duplicates_title),
                        summary = stringResource(Res.string.anki_allow_duplicates_summary),
                    )
                    SwitchPreference(
                        checked = ankiState.settings.embedMedia,
                        onCheckedChange = { onIntent(AnkiIntent.SetEmbedMedia(it)) },
                        title = stringResource(Res.string.anki_embed_media_title),
                        summary = stringResource(Res.string.anki_embed_media_summary),
                    )
                    SwitchPreference(
                        checked = ankiState.settings.compactGlossaries,
                        onCheckedChange = { onIntent(AnkiIntent.SetCompactGlossaries(it)) },
                        title = stringResource(Res.string.anki_compact_glossaries_title),
                        summary = stringResource(Res.string.anki_compact_glossaries_summary),
                    )
                    DuplicateScopeSelector(
                        scope = ankiState.settings.duplicateScope,
                        onScopeChange = { onIntent(AnkiIntent.SetDuplicateScope(it)) },
                    )
                    SwitchPreference(
                        checked = ankiState.settings.checkAllModels,
                        onCheckedChange = { onIntent(AnkiIntent.SetCheckAllModels(it)) },
                        title = stringResource(Res.string.anki_check_all_models_title),
                        summary = stringResource(Res.string.anki_check_all_models_summary),
                    )
                    SwitchPreference(
                        checked = ankiState.settings.forceSync,
                        onCheckedChange = { onIntent(AnkiIntent.SetForceSync(it)) },
                        title = stringResource(Res.string.anki_force_sync_title),
                        summary = stringResource(Res.string.anki_force_sync_summary),
                    )
                }
            }
        }
    }

    WindowDialog(
        title = stringResource(Res.string.anki_handlebars_title),
        show = showHandlebarsDialog,
        onDismissRequest = { showHandlebarsDialog = false },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HandlebarList(handles = handles)
            TextButton(
                text = stringResource(Res.string.btn_close),
                onClick = { showHandlebarsDialog = false },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AnkiConnectSettings(
    url: String,
    timeoutMillis: Int,
    onUrlChange: (String) -> Unit,
    onTimeoutChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextField(
            value = url,
            onValueChange = onUrlChange,
            label = stringResource(Res.string.anki_http_url_label),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        TextField(
            value = timeoutMillis.toString(),
            onValueChange = onTimeoutChange,
            label = stringResource(Res.string.anki_http_timeout_label),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun AnkiFetchActions(
    fetching: Boolean,
    onFetch: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(
                text = stringResource(Res.string.btn_refresh),
                enabled = !fetching,
                onClick = onFetch,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DeckAndModelSelectors(
    ankiState: AnkiState,
    onIntent: (AnkiIntent) -> Unit,
) {
    val decks =
        ankiState.decks.ifEmpty {
            ankiState.settings.selectedDeck
                ?.takeIf(String::isNotBlank)
                ?.let { listOf(AnkiDeck(id = it, name = it)) }
                .orEmpty()
        }
    val noteTypes =
        ankiState.noteTypes.ifEmpty {
            ankiState.settings.selectedNoteType
                ?.takeIf(String::isNotBlank)
                ?.let { listOf(AnkiNoteType(id = it, name = it)) }
                .orEmpty()
        }
    WindowSpinnerPreference(
        items = decks.map { SpinnerEntry(title = it.name) },
        selectedIndex = decks.indexOfFirst { it.name == ankiState.settings.selectedDeck }.coerceAtLeast(0),
        title = stringResource(Res.string.anki_deck_title),
        summary = ankiState.settings.selectedDeck ?: stringResource(Res.string.anki_no_decks),
        enabled = decks.isNotEmpty(),
        onSelectedIndexChange = { index ->
            decks.getOrNull(index)?.let { onIntent(AnkiIntent.SelectDeck(it.name)) }
        },
    )
    WindowSpinnerPreference(
        items = noteTypes.map { SpinnerEntry(title = it.name) },
        selectedIndex = noteTypes.indexOfFirst { it.name == ankiState.settings.selectedNoteType }.coerceAtLeast(0),
        title = stringResource(Res.string.anki_model_title),
        summary = ankiState.settings.selectedNoteType ?: stringResource(Res.string.anki_no_models),
        enabled = noteTypes.isNotEmpty(),
        onSelectedIndexChange = { index ->
            noteTypes.getOrNull(index)?.let { onIntent(AnkiIntent.SelectNoteType(it.name)) }
        },
    )
}

@Composable
private fun FieldMappingEditor(
    fields: List<AnkiField>,
    mappings: Map<String, String>,
    handlebars: List<AnkiHandlebar>,
    onIntent: (AnkiIntent) -> Unit,
) {
    var editingField by remember(fields) { mutableStateOf<AnkiField?>(null) }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        fields.forEach { field ->
            ArrowPreference(
                title = field.name,
                summary = mappings[field.name]?.takeIf(String::isNotBlank) ?: stringResource(Res.string.value_none),
                onClick = { editingField = field },
                startAction = {
                    Icon(
                        imageVector = MiuixIcons.Edit,
                        tint = MiuixTheme.colorScheme.onSecondaryContainer,
                        contentDescription = field.name,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }
    }

    editingField?.let { field ->
        FieldMappingDialog(
            field = field,
            value = mappings[field.name].orEmpty(),
            handlebars = handlebars,
            onValueChange = { value -> onIntent(AnkiIntent.SetFieldMapping(field.name, value)) },
            onDismiss = { editingField = null },
        )
    }
}

@Composable
private fun TagPreference(
    tags: List<String>,
    onValueChange: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    ArrowPreference(
        title = stringResource(Res.string.anki_tags_label),
        summary = tags.joinToString(" ").ifBlank { stringResource(Res.string.value_none) },
        onClick = { showDialog = true },
        startAction = {
            Icon(
                imageVector = MiuixIcons.Edit,
                tint = MiuixTheme.colorScheme.onSecondaryContainer,
                contentDescription = stringResource(Res.string.anki_tags_label),
                modifier = Modifier.size(18.dp),
            )
        },
    )
    if (showDialog) {
        var draft by remember(tags) { mutableStateOf(tags.joinToString(" ")) }
        WindowDialog(
            title = stringResource(Res.string.anki_tags_label),
            show = true,
            onDismissRequest = { showDialog = false },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = draft,
                    onValueChange = {
                        draft = it
                        onValueChange(it)
                    },
                    label = stringResource(Res.string.anki_tags_label),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                TextButton(
                    text = stringResource(Res.string.btn_close),
                    onClick = { showDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun FieldMappingDialog(
    field: AnkiField,
    value: String,
    handlebars: List<AnkiHandlebar>,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    WindowDialog(
        title = field.name,
        show = true,
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FieldMappingInput(
                field = field,
                value = value,
                handlebars = handlebars,
                onValueChange = onValueChange,
            )
            TextButton(
                text = stringResource(Res.string.btn_close),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FieldMappingInput(
    field: AnkiField,
    value: String,
    handlebars: List<AnkiHandlebar>,
    onValueChange: (String) -> Unit,
) {
    var showHandlebars by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = field.name,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = field.name,
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
            trailingIcon = {
                Box {
                    IconButton(onClick = { showHandlebars = true }, modifier = Modifier.padding(end = 12.dp)) {
                        Icon(
                            imageVector = MiuixIcons.Edit,
                            tint = MiuixTheme.colorScheme.onSecondaryContainer,
                            contentDescription = stringResource(Res.string.anki_handlebars_title),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    WindowListPopup(
                        show = showHandlebars,
                        alignment = PopupPositionProvider.Align.End,
                        onDismissRequest = { showHandlebars = false },
                        onDismissFinished = { showHandlebars = false },
                        maxHeight = 320.dp,
                    ) {
                        val dismiss = LocalDismissState.current
                        ListPopupColumn {
                            handlebars.forEachIndexed { index, handlebar ->
                                DropdownImpl(
                                    text = handlebar.token,
                                    optionSize = handlebars.size,
                                    isSelected = value == handlebar.token,
                                    index = index,
                                    onSelectedIndexChange = {
                                        onValueChange(if (value.isBlank()) handlebar.token else "$value ${handlebar.token}")
                                        dismiss?.invoke()
                                    },
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun HandlebarList(handles: List<AnkiHandlebar>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        handles.forEach { handlebar ->
            Text(
                text = handlebar.token,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

private fun AnkiState.selectedNoteType(): AnkiNoteType? = noteTypes.firstOrNull { it.name == settings.selectedNoteType }

private fun AnkiState.editableFields(): List<AnkiField> {
    if (settings.selectedDeck.isNullOrBlank() && settings.selectedNoteType.isNullOrBlank()) {
        return lapisFieldMappings().keys.map(::AnkiField)
    }

    val fetchedFields = selectedNoteType()?.fields.orEmpty()
    if (fetchedFields.isNotEmpty()) return fetchedFields

    return emptyList()
}

private fun AnkiState.effectiveFieldMappings(): Map<String, String> {
    val shouldUseLapisDefaults =
        settings.selectedDeck.isNullOrBlank() && settings.selectedNoteType.isNullOrBlank()
    val defaults = if (shouldUseLapisDefaults) lapisFieldMappings() else emptyMap()
    return defaults + settings.fieldMappings
}

@Composable
private fun rememberAnkiHandlebars(dictionaryState: DictionaryManagementState): List<AnkiHandlebar> =
    remember(dictionaryState.termDictionaries) {
        defaultAnkiHandlebars() +
            dictionaryState
                .dictionaries(DictionaryType.Term)
                .filter { it.isEnabled }
                .map { AnkiHandlebar(token = "{single-glossary-${it.index.title}}") }
    }

private fun defaultAnkiHandlebars(): List<AnkiHandlebar> = defaultAnkiHandlebarTokens().map(::AnkiHandlebar)

@Composable
private fun DuplicateScopeSelector(
    scope: AnkiDuplicateScope,
    onScopeChange: (AnkiDuplicateScope) -> Unit,
) {
    val scopes = remember { AnkiDuplicateScope.entries.toList() }
    WindowSpinnerPreference(
        items = scopes.map { SpinnerEntry(title = it.label()) },
        selectedIndex = scopes.indexOf(scope).coerceAtLeast(0),
        title = stringResource(Res.string.anki_duplicate_scope_title),
        summary = scope.label(),
        onSelectedIndexChange = { index -> onScopeChange(scopes[index]) },
    )
}

@Composable
private fun AnkiDuplicateScope.label(): String =
    stringResource(
        when (this) {
            AnkiDuplicateScope.Collection -> Res.string.anki_duplicate_scope_collection
            AnkiDuplicateScope.Deck -> Res.string.anki_duplicate_scope_deck
            AnkiDuplicateScope.NoteType -> Res.string.anki_duplicate_scope_note_type
        },
    )

private val AnkiSettingsHorizontalPadding = 12.dp
