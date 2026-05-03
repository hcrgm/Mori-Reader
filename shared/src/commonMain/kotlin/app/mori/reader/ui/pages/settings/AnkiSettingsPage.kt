package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mori.reader.data.anki.AnkiFieldMapping
import app.mori.reader.data.anki.AnkiTemplateToken
import app.mori.reader.data.anki.DuplicateScope
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.OverlaySpinnerPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import app.mori.reader.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AnkiSettingsPage(
    state: AppState,
    message: String?,
    onIntent: (AppIntent) -> Unit,
    onBack: () -> Unit,
) {
    MoriPageScaffold(
        title = "Anki",
        subtitle = "",
        blurEnabled = state.settings.blurEnabled,
        message = message,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
    ) { paddingValues, scrollBehavior ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            state.anki.errorMessage?.let { error ->
                item {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = error, color = MiuixTheme.colorScheme.error)
                            TextButton(text = stringResource(Res.string.cd_close), onClick = { onIntent(AppIntent.DismissAnkiError) })
                        }
                    }
                }
            }

            item {
                SmallTitle(text = stringResource(Res.string.btn_connect))
                Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                    ConnectionCard(state = state, onIntent = onIntent)
                }
            }

            item {
                SmallTitle(text = stringResource(Res.string.anki_deck_model_title))
                Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                    DeckModelCard(state = state, onIntent = onIntent)
                }
            }

            item {
                SmallTitle(text = stringResource(Res.string.anki_duplicate_sync_title))
                Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                    DuplicateCard(state = state, onIntent = onIntent)
                }
            }

            item {
                SmallTitle(text = stringResource(Res.string.anki_tags_glossary_title))
                Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                    TagsCard(state = state, onIntent = onIntent)
                }
            }

            item {
                SmallTitle(text = stringResource(Res.string.anki_field_mapping_title))
            }

            val mappings = state.settings.anki.selectedFieldMappings
            if (mappings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(Res.string.anki_field_mapping_hint),
                            modifier = Modifier.padding(18.dp),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            } else {
                items(items = mappings, key = { it.fieldName }) { mapping ->
                    Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                        FieldMappingRow(mapping = mapping, onIntent = onIntent)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
) {
    val anki = state.settings.anki
    Column {
        SwitchPreference(
            checked = anki.enabled,
            onCheckedChange = { onIntent(AppIntent.SetAnkiEnabled(it)) },
            title = stringResource(Res.string.anki_enable_title),
            summary = stringResource(Res.string.anki_enable_summary),
        )
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(Res.string.anki_require_permission),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                TextButton(
                    text = stringResource(Res.string.btn_connect),
                    enabled = !state.anki.isLoading,
                    minHeight = 36.dp,
                    onClick = { onIntent(AppIntent.TestAnkiConnection) },
                )
                TextButton(
                    text = stringResource(Res.string.btn_refresh),
                    enabled = !state.anki.isLoading,
                    minHeight = 36.dp,
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = { onIntent(AppIntent.RefreshAnkiCatalog) },
                )
            }
        }
    }
}

@Composable
private fun DeckModelCard(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
) {
    val settings = state.settings.anki
    val decks = state.anki.decks
    val models = state.anki.noteTypes.map { it.name }
    val notLoaded = stringResource(Res.string.anki_not_loaded)
    val deckItems = remember(decks, notLoaded) { decks.ifEmpty { listOf(notLoaded) }.map { SpinnerEntry(title = it) } }
    val modelItems = remember(models, notLoaded) { models.ifEmpty { listOf(notLoaded) }.map { SpinnerEntry(title = it) } }
    Column {
        OverlaySpinnerPreference(
            items = deckItems,
            selectedIndex = decks.indexOf(settings.selectedDeck).takeIf { it >= 0 } ?: 0,
            title = stringResource(Res.string.anki_deck_title),
            summary = settings.selectedDeck.ifBlank { stringResource(Res.string.anki_not_selected) },
            onSelectedIndexChange = { index ->
                decks.getOrNull(index)?.let { onIntent(AppIntent.SelectAnkiDeck(it)) }
            },
        )
        OverlaySpinnerPreference(
            items = modelItems,
            selectedIndex = models.indexOf(settings.selectedModel).takeIf { it >= 0 } ?: 0,
            title = stringResource(Res.string.anki_model_title),
            summary = settings.selectedModel.ifBlank { stringResource(Res.string.anki_not_selected) },
            onSelectedIndexChange = { index ->
                models.getOrNull(index)?.let { onIntent(AppIntent.SelectAnkiModel(it)) }
            },
        )
    }
}

@Composable
private fun DuplicateCard(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
) {
    val settings = state.settings.anki
    val scopes = remember { DuplicateScope.entries.toList() }
    val scopeItems = remember(scopes) { scopes.map { SpinnerEntry(title = it.label) } }
    Column {
        SwitchPreference(
            checked = settings.allowDuplicates,
            onCheckedChange = { onIntent(AppIntent.SetAnkiAllowDuplicates(it)) },
            title = stringResource(Res.string.anki_allow_duplicate_title),
        )
        OverlaySpinnerPreference(
            items = scopeItems,
            selectedIndex = scopes.indexOf(settings.duplicateScope).coerceAtLeast(0),
            title = stringResource(Res.string.anki_duplicate_scope_title),
            summary = settings.duplicateScope.label,
            onSelectedIndexChange = { index -> onIntent(AppIntent.SetAnkiDuplicateScope(scopes[index])) },
        )
        SwitchPreference(
            checked = settings.checkAllModels,
            onCheckedChange = { onIntent(AppIntent.SetAnkiCheckAllModels(it)) },
            title = stringResource(Res.string.anki_check_all_models_title),
        )
        SwitchPreference(
            checked = settings.forceSync,
            onCheckedChange = { onIntent(AppIntent.SetAnkiForceSync(it)) },
            title = stringResource(Res.string.anki_sync_after_add_title),
        )
    }
}

@Composable
private fun TagsCard(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
) {
    val settings = state.settings.anki
    Column {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextField(
                value = settings.tags,
                onValueChange = { onIntent(AppIntent.SetAnkiTags(it)) },
                label = stringResource(Res.string.anki_tags_label),
                useLabelAsPlaceholder = true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(start = 20.dp))
        SwitchPreference(
            checked = settings.compactGlossaries,
            onCheckedChange = { onIntent(AppIntent.SetAnkiCompactGlossaries(it)) },
            title = stringResource(Res.string.dict_settings_compact_title),
            summary = stringResource(Res.string.anki_compact_glossary_summary),
        )
    }
}

@Composable
private fun FieldMappingRow(
    mapping: AnkiFieldMapping,
    onIntent: (AppIntent) -> Unit,
) {
    val insertTokenLabel = stringResource(Res.string.anki_insert_token)
    val tokenItems = remember(insertTokenLabel) {
        listOf(SpinnerEntry(title = insertTokenLabel)) +
            AnkiTemplateToken.entries.map { SpinnerEntry(title = "${it.label} ${it.token}") }
    }
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = mapping.fieldName, fontWeight = FontWeight.Medium)
        TextField(
            value = mapping.template,
            onValueChange = { onIntent(AppIntent.SetAnkiFieldTemplate(mapping.fieldName, it)) },
            label = stringResource(Res.string.anki_field_template_label),
            useLabelAsPlaceholder = true,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OverlaySpinnerPreference(
            items = tokenItems,
            selectedIndex = 0,
            title = stringResource(Res.string.anki_token_title),
            summary = stringResource(Res.string.anki_token_summary),
            onSelectedIndexChange = { index ->
                val token = AnkiTemplateToken.entries.getOrNull(index - 1)?.token ?: return@OverlaySpinnerPreference
                onIntent(AppIntent.InsertAnkiFieldToken(mapping.fieldName, token))
            },
        )
    }
}
