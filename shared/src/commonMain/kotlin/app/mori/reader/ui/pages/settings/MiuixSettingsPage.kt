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
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.tab_settings
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MoriIconArrowPreference
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon

@Composable
internal fun MiuixSettingsPage(
    settings: AppSettings,
    fixedPadding: PaddingValues,
    onOpenAppearanceSettings: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onOpenAnkiSettings: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val entries =
        settingsEntries(
            onOpenAppearanceSettings = onOpenAppearanceSettings,
            onOpenReaderSettings = onOpenReaderSettings,
            onOpenDictionarySettings = onOpenDictionarySettings,
            onOpenAudioSettings = onOpenAudioSettings,
            onOpenAnkiSettings = onOpenAnkiSettings,
        )
    val aboutEntries = settingsAboutEntries(onOpenAbout = onOpenAbout)

    MoriPageScaffold(
        title = stringResource(Res.string.tab_settings),
        blurEnabled = settings.appearance.blurEnabled,
        fixedPadding = fixedPadding,
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .overScrollVertical(),
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
                    entries.forEach { entry ->
                        MoriIconArrowPreference(
                            title = entry.title,
                            onClick = entry.onClick,
                            startAction = {
                                MiuixIcon(
                                    imageVector = entry.icon,
                                    contentDescription = entry.title,
                                    tint = MiuixTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(end = 6.dp),
                                )
                            },
                        )
                    }
                }
            }
            item {
                Card(
                    modifier =
                        Modifier.padding(
                            start = 12.dp,
                            end = 12.dp,
                        ),
                ) {
                    aboutEntries.forEach { entry ->
                        MoriIconArrowPreference(
                            title = entry.title,
                            onClick = entry.onClick,
                            startAction = {
                                MiuixIcon(
                                    imageVector = entry.icon,
                                    contentDescription = entry.title,
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
}
