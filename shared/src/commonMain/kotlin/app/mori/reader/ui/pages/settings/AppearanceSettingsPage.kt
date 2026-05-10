package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.appearance_app_title
import app.mori.reader.shared.generated.resources.cd_appearance
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.AppThemeSettingsGroup
import app.mori.reader.ui.components.settings.MoriSettingsSection
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun AppearanceSettingsPage(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    MoriPageScaffold(
        title = stringResource(Res.string.cd_appearance),
        subtitle = "",
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
        actions = {},
    ) { paddingValues, scrollBehavior ->
        AppearanceSettingsContent(
            settings = settings,
            onSettingsIntent = onSettingsIntent,
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
        )
    }
}

@Composable
private fun AppearanceSettingsContent(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
    modifier: Modifier,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            MoriSettingsSection(title = stringResource(Res.string.appearance_app_title)) {
                AppThemeSettingsGroup(
                    themeMode = settings.appearance.themeMode,
                    languageMode = settings.appearance.languageMode,
                    monetEnabled = settings.appearance.monetEnabled,
                    monetKeyColor = settings.appearance.monetKeyColor,
                    blurEnabled = settings.appearance.blurEnabled,
                    onThemeModeSelected = { onSettingsIntent(SettingsIntent.SetThemeMode(it)) },
                    onLanguageModeSelected = { onSettingsIntent(SettingsIntent.SetLanguageMode(it)) },
                    onMonetEnabledChanged = { onSettingsIntent(SettingsIntent.SetMonetEnabled(it)) },
                    onMonetKeyColorSelected = { onSettingsIntent(SettingsIntent.SetMonetKeyColor(it)) },
                    onBlurEnabledChanged = { onSettingsIntent(SettingsIntent.SetBlurEnabled(it)) },
                )
            }
        }
    }
}
