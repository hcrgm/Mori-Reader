package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.anki.presentation.AnkiState
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.settings_anki_status_connected
import app.mori.reader.shared.generated.resources.settings_anki_status_disconnected
import app.mori.reader.shared.generated.resources.settings_anki_summary
import app.mori.reader.shared.generated.resources.settings_anki_title
import app.mori.reader.shared.generated.resources.settings_appearance_summary
import app.mori.reader.shared.generated.resources.settings_appearance_title
import app.mori.reader.shared.generated.resources.settings_audio_summary
import app.mori.reader.shared.generated.resources.settings_audio_title
import app.mori.reader.shared.generated.resources.settings_dictionary_summary
import app.mori.reader.shared.generated.resources.settings_dictionary_title
import app.mori.reader.shared.generated.resources.settings_reader_summary
import app.mori.reader.shared.generated.resources.settings_reader_title
import app.mori.reader.shared.generated.resources.tab_settings
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MoriIconArrowPreference
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun SettingsPage(
    settings: AppSettings,
    ankiState: AnkiState,
    fixedPadding: PaddingValues,
    onOpenAppearanceSettings: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onOpenAnkiSettings: () -> Unit,
) {
    MoriPageScaffold(
        title = stringResource(Res.string.tab_settings),
        blurEnabled = settings.appearance.blurEnabled,
        fixedPadding = fixedPadding,
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
                Card(
                    modifier =
                        Modifier.padding(
                            start = 12.dp,
                            top = 12.dp,
                            end = 12.dp,
                        ),
                ) {
                    MoriIconArrowPreference(
                        title = stringResource(Res.string.settings_appearance_title),
                        summary = stringResource(Res.string.settings_appearance_summary),
                        onClick = onOpenAppearanceSettings,
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Background,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                    MoriIconArrowPreference(
                        title = stringResource(Res.string.settings_reader_title),
                        summary = stringResource(Res.string.settings_reader_summary),
                        onClick = onOpenReaderSettings,
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Notes,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                    MoriIconArrowPreference(
                        title = stringResource(Res.string.settings_dictionary_title),
                        summary = stringResource(Res.string.settings_dictionary_summary),
                        onClick = onOpenDictionarySettings,
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Translate,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                    MoriIconArrowPreference(
                        title = stringResource(Res.string.settings_audio_title),
                        summary = stringResource(Res.string.settings_audio_summary),
                        onClick = onOpenAudioSettings,
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Music,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                    MoriIconArrowPreference(
                        title = stringResource(Res.string.settings_anki_title),
                        summary =
                            buildString {
                                append(stringResource(Res.string.settings_anki_summary))
                                append(" · ")
                                append(
                                    stringResource(
                                        if (ankiState.isConnected) {
                                            Res.string.settings_anki_status_connected
                                        } else {
                                            Res.string.settings_anki_status_disconnected
                                        },
                                    ),
                                )
                            },
                        onClick = onOpenAnkiSettings,
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Settings,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}
