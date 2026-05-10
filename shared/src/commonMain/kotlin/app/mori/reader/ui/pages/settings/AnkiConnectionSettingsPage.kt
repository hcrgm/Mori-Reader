package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.mori.reader.data.anki.AnkiConnectionMode
import app.mori.reader.data.anki.AnkiDuplicateScope
import app.mori.reader.data.anki.ankiPlatformCapabilities
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.anki.presentation.AnkiIntent
import app.mori.reader.features.anki.presentation.AnkiState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.anki_ankidroid_guidance
import app.mori.reader.shared.generated.resources.anki_check_all_models_summary
import app.mori.reader.shared.generated.resources.anki_check_all_models_title
import app.mori.reader.shared.generated.resources.anki_connection_mode_title
import app.mori.reader.shared.generated.resources.anki_connection_title
import app.mori.reader.shared.generated.resources.anki_duplicate_scope_collection
import app.mori.reader.shared.generated.resources.anki_duplicate_scope_deck
import app.mori.reader.shared.generated.resources.anki_duplicate_scope_note_type
import app.mori.reader.shared.generated.resources.anki_duplicate_scope_title
import app.mori.reader.shared.generated.resources.anki_force_sync_summary
import app.mori.reader.shared.generated.resources.anki_force_sync_title
import app.mori.reader.shared.generated.resources.anki_http_timeout_label
import app.mori.reader.shared.generated.resources.anki_http_title
import app.mori.reader.shared.generated.resources.anki_http_url_label
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowSpinnerPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun AnkiConnectionSettingsPage(
    settings: AppSettings,
    ankiState: AnkiState,
    onIntent: (AnkiIntent) -> Unit,
    onBack: () -> Unit,
) {
    val capabilities = remember { ankiPlatformCapabilities() }
    MoriPageScaffold(
        title = stringResource(Res.string.anki_connection_title),
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
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
                SmallTitle(text = stringResource(Res.string.anki_connection_mode_title))
                Card(modifier = Modifier.padding(horizontal = AnkiConnectionHorizontalPadding)) {
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
                    if (ankiState.settings.connectionMode == AnkiConnectionMode.AnkiDroid) {
                        Text(
                            text = stringResource(Res.string.anki_ankidroid_guidance),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }

            if (ankiState.settings.connectionMode == AnkiConnectionMode.AnkiConnect) {
                item {
                    SmallTitle(text = stringResource(Res.string.anki_http_title))
                    Card(modifier = Modifier.padding(horizontal = AnkiConnectionHorizontalPadding)) {
                        TextField(
                            value = ankiState.settings.ankiConnect.url,
                            onValueChange = { onIntent(AnkiIntent.SetAnkiConnectUrl(it)) },
                            label = stringResource(Res.string.anki_http_url_label),
                            useLabelAsPlaceholder = true,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                            singleLine = true,
                        )
                        TextField(
                            value =
                                ankiState.settings.ankiConnect.timeoutMillis
                                    .toString(),
                            onValueChange = { value ->
                                value.toIntOrNull()?.let {
                                    onIntent(AnkiIntent.SetAnkiConnectTimeoutMillis(it))
                                }
                            },
                            label = stringResource(Res.string.anki_http_timeout_label),
                            useLabelAsPlaceholder = true,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                            singleLine = true,
                        )
                    }
                }
            }

            item {
                SmallTitle(text = stringResource(Res.string.anki_duplicate_scope_title))
                Card(modifier = Modifier.padding(horizontal = AnkiConnectionHorizontalPadding)) {
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
}

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
internal fun AnkiConnectionMode.label(): String =
    when (this) {
        AnkiConnectionMode.AnkiDroid -> "AnkiDroid"
        AnkiConnectionMode.AnkiConnect -> "AnkiConnect"
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

private val AnkiConnectionHorizontalPadding = 12.dp
