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
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_appearance
import app.mori.reader.shared.generated.resources.settings_appearance_summary
import app.mori.reader.shared.generated.resources.settings_audio_summary
import app.mori.reader.shared.generated.resources.settings_audio_title
import app.mori.reader.shared.generated.resources.settings_dictionary_summary
import app.mori.reader.shared.generated.resources.tab_dictionary
import app.mori.reader.shared.generated.resources.tab_settings
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun SettingsPage(
    state: AppState,
    message: String?,
    fixedPadding: PaddingValues,
    onIntent: (AppIntent) -> Unit,
    onOpenAppearanceSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onOpenAnkiSettings: () -> Unit,
) {
    MoriPageScaffold(
        title = AppTab.Settings.title,
        subtitle = AppTab.Settings.subtitle,
        blurEnabled = state.settings.blurEnabled,
        fixedPadding = fixedPadding,
        message = message,
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
            item {
                SmallTitle(text = stringResource(Res.string.tab_settings))
                Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                    ArrowPreference(
                        title = stringResource(Res.string.cd_appearance),
                        summary = stringResource(Res.string.settings_appearance_summary),
                        onClick = onOpenAppearanceSettings,
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.tab_dictionary),
                        summary = stringResource(Res.string.settings_dictionary_summary),
                        onClick = onOpenDictionarySettings,
                    )
                    ArrowPreference(
                        title = stringResource(Res.string.settings_audio_title),
                        summary = stringResource(Res.string.settings_audio_summary),
                        onClick = onOpenAudioSettings,
                    )
                    // TODO: Under construction
//                    ArrowPreference(
//                        title = "Anki",
//                        summary = "配置 AnkiConnect、牌组模板和字段映射",
//                        onClick = onOpenAnkiSettings,
//                    )
                }
            }
        }
    }
}
