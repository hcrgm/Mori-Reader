package app.mori.reader.ui.pages.settings.anki

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.core.platform.rememberAnkiDroidPermissionRequester
import app.mori.reader.data.anki.AnkiConnectionMode
import app.mori.reader.data.anki.AnkiDuplicateScope
import app.mori.reader.data.anki.AnkiField
import app.mori.reader.data.anki.AnkiHandlebar
import app.mori.reader.data.anki.ankiPlatformCapabilities
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
import app.mori.reader.shared.generated.resources.anki_no_fields
import app.mori.reader.shared.generated.resources.anki_no_models
import app.mori.reader.ui.components.material.MaterialBackButton
import app.mori.reader.ui.components.material.MaterialExpressiveSwitch
import app.mori.reader.shared.generated.resources.anki_options_title
import app.mori.reader.shared.generated.resources.anki_tags_label
import app.mori.reader.shared.generated.resources.anki_title
import app.mori.reader.shared.generated.resources.btn_close
import app.mori.reader.shared.generated.resources.btn_refresh
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.value_none
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.text.asString
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MaterialAnkiSettingsPage(
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
            Row {
                MaterialBackButton(onClick = onBack, contentDescription = stringResource(Res.string.cd_back))
                Spacer(modifier = Modifier.size(16.dp))
            }
        },
        actions = {
            IconButton(onClick = { showHandlebarsDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(Res.string.anki_handlebars_title),
                )
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AnimatedVisibility(
                    visible = ankiState.errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    MaterialAnkiInfoCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = errorCardMessage,
                        error = true,
                    )
                }
            }

            item {
                MaterialAnkiSection(
                    title = stringResource(Res.string.anki_connection_title),
                ) {
                    MaterialAnkiPickerRow(
                        title = stringResource(Res.string.anki_connection_mode_title),
                        summary = ankiState.settings.connectionMode.label(),
                        items = capabilities.availableModes,
                        itemLabel = { it.label() },
                        onItemSelected = { onIntent(AnkiIntent.SetConnectionMode(it)) },
                        shape =
                            if (ankiState.settings.connectionMode == AnkiConnectionMode.AnkiConnect) {
                                materialAnkiSegmentedItemShape(index = 0, count = 3)
                            } else {
                                materialAnkiSegmentedItemShape(index = 0, count = 2)
                            },
                    )
                    AnimatedVisibility(
                        visible = ankiState.settings.connectionMode == AnkiConnectionMode.AnkiConnect,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            shape = materialAnkiSegmentedItemShape(index = 1, count = 3),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                OutlinedTextField(
                                    value = ankiState.settings.ankiConnect.url,
                                    onValueChange = { onIntent(AnkiIntent.SetAnkiConnectUrl(it)) },
                                    label = { Text(text = stringResource(Res.string.anki_http_url_label)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    value =
                                        ankiState.settings.ankiConnect.timeoutMillis
                                            .toString(),
                                    onValueChange = { value ->
                                        value.toIntOrNull()?.let {
                                            onIntent(AnkiIntent.SetAnkiConnectTimeoutMillis(it))
                                        }
                                    },
                                    label = { Text(text = stringResource(Res.string.anki_http_timeout_label)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                            }
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shape =
                            if (ankiState.settings.connectionMode == AnkiConnectionMode.AnkiConnect) {
                                materialAnkiSegmentedItemShape(index = 2, count = 3)
                            } else {
                                materialAnkiSegmentedItemShape(index = 1, count = 2)
                            },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FilledTonalButton(
                            enabled = !ankiState.isFetching,
                            onClick = {
                                if (ankiState.settings.connectionMode == AnkiConnectionMode.AnkiDroid) {
                                    requestAnkiDroidPermission()
                                } else {
                                    onIntent(AnkiIntent.FetchDecksAndModels)
                                }
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Text(text = stringResource(Res.string.btn_refresh))
                        }
                    }
                }
            }

            item {
                MaterialAnkiSectionTitle(text = stringResource(Res.string.anki_field_mapping_title))
                val fieldMappingRows = 3 + maxOf(ankiState.editableFields().size, 1)
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (ankiState.settings.showLapisTemplateHint) {
                        MaterialAnkiInfoCard(
                            text = stringResource(Res.string.anki_lapis_template_hint),
                            onDismiss = { onIntent(AnkiIntent.SetShowLapisTemplateHint(false)) },
                        )
                    }
                    MaterialAnkiSegmentedColumn {
                        MaterialDeckAndModelSelectors(
                            ankiState = ankiState,
                            onIntent = onIntent,
                            totalRows = fieldMappingRows,
                        )
                        MaterialTagRow(
                            tags = ankiState.settings.tags,
                            onValueChange = { value ->
                                onIntent(AnkiIntent.SetTags(value.split(Regex("\\s+"))))
                            },
                            shape = materialAnkiSegmentedItemShape(index = 2, count = fieldMappingRows),
                        )
                        MaterialFieldMappingEditor(
                            fields = ankiState.editableFields(),
                            mappings = ankiState.effectiveFieldMappings(),
                            handlebars = handles,
                            onIntent = onIntent,
                            rowStartIndex = 3,
                            totalRows = fieldMappingRows,
                        )
                    }
                }
            }

            item {
                MaterialAnkiSection(
                    title = stringResource(Res.string.anki_options_title),
                ) {
                    MaterialSwitchRow(
                        title = stringResource(Res.string.anki_allow_duplicates_title),
                        summary = stringResource(Res.string.anki_allow_duplicates_summary),
                        checked = ankiState.settings.allowDuplicates,
                        onCheckedChange = { onIntent(AnkiIntent.SetAllowDuplicates(it)) },
                        shape = materialAnkiSegmentedItemShape(index = 0, count = 6),
                    )
                    MaterialSwitchRow(
                        title = stringResource(Res.string.anki_embed_media_title),
                        summary = stringResource(Res.string.anki_embed_media_summary),
                        checked = ankiState.settings.embedMedia,
                        onCheckedChange = { onIntent(AnkiIntent.SetEmbedMedia(it)) },
                        shape = materialAnkiSegmentedItemShape(index = 1, count = 6),
                    )
                    MaterialSwitchRow(
                        title = stringResource(Res.string.anki_compact_glossaries_title),
                        summary = stringResource(Res.string.anki_compact_glossaries_summary),
                        checked = ankiState.settings.compactGlossaries,
                        onCheckedChange = { onIntent(AnkiIntent.SetCompactGlossaries(it)) },
                        shape = materialAnkiSegmentedItemShape(index = 2, count = 6),
                    )
                    MaterialAnkiPickerRow(
                        title = stringResource(Res.string.anki_duplicate_scope_title),
                        summary = ankiState.settings.duplicateScope.label(),
                        items = remember { AnkiDuplicateScope.entries.toList() },
                        itemLabel = { it.label() },
                        onItemSelected = { onIntent(AnkiIntent.SetDuplicateScope(it)) },
                        shape = materialAnkiSegmentedItemShape(index = 3, count = 6),
                    )
                    MaterialSwitchRow(
                        title = stringResource(Res.string.anki_check_all_models_title),
                        summary = stringResource(Res.string.anki_check_all_models_summary),
                        checked = ankiState.settings.checkAllModels,
                        onCheckedChange = { onIntent(AnkiIntent.SetCheckAllModels(it)) },
                        shape = materialAnkiSegmentedItemShape(index = 4, count = 6),
                    )
                    MaterialSwitchRow(
                        title = stringResource(Res.string.anki_force_sync_title),
                        summary = stringResource(Res.string.anki_force_sync_summary),
                        checked = ankiState.settings.forceSync,
                        onCheckedChange = { onIntent(AnkiIntent.SetForceSync(it)) },
                        shape = materialAnkiSegmentedItemShape(index = 5, count = 6),
                    )
                }
            }
        }
    }

    MaterialHandlebarsDialog(
        show = showHandlebarsDialog,
        handles = handles,
        onDismiss = { showHandlebarsDialog = false },
    )
}

@Composable
private fun MaterialDeckAndModelSelectors(
    ankiState: AnkiState,
    onIntent: (AnkiIntent) -> Unit,
    totalRows: Int = 2,
) {
    val options = ankiState.deckAndNoteTypeOptions()
    val decks = options.decks
    val noteTypes = options.noteTypes

    MaterialAnkiPickerRow(
        title = stringResource(Res.string.anki_deck_title),
        summary = ankiState.settings.selectedDeck ?: stringResource(Res.string.anki_no_decks),
        items = decks,
        itemLabel = { it.name },
        enabled = decks.isNotEmpty(),
        onItemSelected = { onIntent(AnkiIntent.SelectDeck(it.name)) },
        shape = materialAnkiSegmentedItemShape(index = 0, count = totalRows),
    )
    MaterialAnkiPickerRow(
        title = stringResource(Res.string.anki_model_title),
        summary = ankiState.settings.selectedNoteType ?: stringResource(Res.string.anki_no_models),
        items = noteTypes,
        itemLabel = { it.name },
        enabled = noteTypes.isNotEmpty(),
        onItemSelected = { onIntent(AnkiIntent.SelectNoteType(it.name)) },
        shape = materialAnkiSegmentedItemShape(index = 1, count = totalRows),
    )
}

@Composable
private fun MaterialFieldMappingEditor(
    fields: List<AnkiField>,
    mappings: Map<String, String>,
    handlebars: List<AnkiHandlebar>,
    onIntent: (AnkiIntent) -> Unit,
    rowStartIndex: Int = 0,
    totalRows: Int = maxOf(fields.size, 1),
) {
    var editingField by remember(fields) { mutableStateOf<AnkiField?>(null) }
    if (fields.isEmpty()) {
        MaterialAnkiRowSurface(
            shape = materialAnkiSegmentedItemShape(index = rowStartIndex, count = totalRows),
        ) {
            Text(
                text = stringResource(Res.string.anki_no_fields),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    } else {
        fields.forEachIndexed { index, field ->
            MaterialAnkiRowSurface(
                shape = materialAnkiSegmentedItemShape(index = rowStartIndex + index, count = totalRows),
                onClick = { editingField = field },
            ) {
                ListItem(
                    headlineContent = { Text(text = field.name) },
                    supportingContent = {
                        Text(
                            text = mappings[field.name]?.takeIf(String::isNotBlank) ?: stringResource(Res.string.value_none),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = field.name,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }

    editingField?.let { field ->
        MaterialFieldMappingDialog(
            field = field,
            value = mappings[field.name].orEmpty(),
            handlebars = handlebars,
            onValueChange = { value -> onIntent(AnkiIntent.SetFieldMapping(field.name, value)) },
            onDismiss = { editingField = null },
        )
    }
}

@Composable
private fun MaterialTagRow(
    tags: List<String>,
    onValueChange: (String) -> Unit,
    shape: Shape = MaterialTheme.shapes.large,
) {
    var showDialog by remember { mutableStateOf(false) }
    MaterialAnkiRowSurface(
        shape = shape,
        onClick = { showDialog = true },
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(Res.string.anki_tags_label)) },
            supportingContent = {
                Text(
                    text = tags.joinToString(" ").ifBlank { stringResource(Res.string.value_none) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(Res.string.anki_tags_label),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }

    if (showDialog) {
        var draft by remember(tags) { mutableStateOf(tags.joinToString(" ")) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(Res.string.anki_tags_label)) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = {
                        draft = it
                        onValueChange(it)
                    },
                    label = { Text(text = stringResource(Res.string.anki_tags_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(Res.string.btn_close))
                }
            },
        )
    }
}

@Composable
private fun MaterialFieldMappingDialog(
    field: AnkiField,
    value: String,
    handlebars: List<AnkiHandlebar>,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var handlebarMenuExpanded by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = field.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = field.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(text = field.name) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        Box {
                            IconButton(onClick = { handlebarMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = stringResource(Res.string.anki_handlebars_title),
                                )
                            }
                            DropdownMenu(
                                expanded = handlebarMenuExpanded,
                                onDismissRequest = { handlebarMenuExpanded = false },
                            ) {
                                handlebars.forEach { handlebar ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = handlebar.token,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        },
                                        onClick = {
                                            onValueChange(if (value.isBlank()) handlebar.token else "$value ${handlebar.token}")
                                            handlebarMenuExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.btn_close))
            }
        },
    )
}

@Composable
private fun <T> MaterialAnkiPickerRow(
    title: String,
    summary: String,
    items: List<T>,
    itemLabel: @Composable (T) -> String,
    onItemSelected: (T) -> Unit,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.large,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        MaterialAnkiRowSurface(
            shape = shape,
            onClick = if (enabled && items.isNotEmpty()) ({ expanded = true }) else null,
        ) {
            ListItem(
                headlineContent = { Text(text = title) },
                supportingContent = {
                    Text(
                        text = summary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                trailingContent = {
                    Box {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            items.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(text = itemLabel(item)) },
                                    onClick = {
                                        onItemSelected(item)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@Composable
private fun MaterialSwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape = MaterialTheme.shapes.large,
) {
    MaterialAnkiRowSurface(
        shape = shape,
        onClick = { onCheckedChange(!checked) },
    ) {
        ListItem(
            headlineContent = { Text(text = title) },
            supportingContent = { Text(text = summary) },
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
private fun MaterialAnkiInfoCard(
    modifier: Modifier = Modifier,
    text: String,
    error: Boolean = false,
    onDismiss: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (error) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        materialInfoCardContainerColor()
                    },
                contentColor =
                    if (error) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (onDismiss != null) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(Res.string.btn_close))
                }
            }
        }
    }
}

@Composable
private fun MaterialHandlebarsDialog(
    show: Boolean,
    handles: List<AnkiHandlebar>,
    onDismiss: () -> Unit,
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.anki_handlebars_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                handles.forEach { handlebar ->
                    Text(
                        text = handlebar.token,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.btn_close))
            }
        },
    )
}

@Composable
private fun MaterialAnkiSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
private fun MaterialAnkiSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MaterialAnkiSectionTitle(text = title)
        MaterialAnkiSegmentedColumn(content = content)
    }
}

@Composable
private fun MaterialAnkiSegmentedColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp), content = content)
}

@Composable
private fun MaterialAnkiRowSurface(
    shape: Shape = MaterialTheme.shapes.large,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = shape,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ),
    ) {
        content()
    }
}

@Composable
private fun materialAnkiSegmentedItemShape(
    index: Int,
    count: Int,
): Shape =
    when {
        count == 1 -> {
            MaterialTheme.shapes.large
        }

        index == 0 -> {
            MaterialTheme.shapes.large.copy(
                bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
                bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd,
            )
        }

        index == count - 1 -> {
            MaterialTheme.shapes.large.copy(
                topStart = MaterialTheme.shapes.extraSmall.topStart,
                topEnd = MaterialTheme.shapes.extraSmall.topEnd,
            )
        }

        else -> {
            RoundedCornerShape(4.dp)
        }
    }

@Composable
private fun materialInfoCardContainerColor(): Color =
    MaterialTheme.colorScheme.secondaryContainer

@Composable
private fun AnkiDuplicateScope.label(): String =
    stringResource(
        when (this) {
            AnkiDuplicateScope.Collection -> Res.string.anki_duplicate_scope_collection
            AnkiDuplicateScope.Deck -> Res.string.anki_duplicate_scope_deck
            AnkiDuplicateScope.NoteType -> Res.string.anki_duplicate_scope_note_type
        },
    )
