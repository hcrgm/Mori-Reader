package app.mori.reader.ui.pages.settings.appearence

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.appearance_app_title
import app.mori.reader.shared.generated.resources.cd_appearance
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.AppThemeSettingsGroup
import app.mori.reader.ui.components.settings.MoriSettingsSection
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton

@Composable
internal fun MiuixAppearanceSettingsPage(
    settings: AppSettings,
    actions: AppearanceSettingsActions,
    onBack: () -> Unit,
) {
    MoriPageScaffold(
        title = stringResource(Res.string.cd_appearance),
        subtitle = "",
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            MiuixIconButton(onClick = onBack) {
                MiuixIcon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
        actions = {},
    ) { paddingValues ->
        MiuixAppearanceSettingsContent(
            settings = settings,
            actions = actions,
            modifier =
                Modifier
                    .fillMaxSize()
                    .overScrollVertical(),
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                ),
        )
    }
}

@Composable
private fun MiuixAppearanceSettingsContent(
    settings: AppSettings,
    actions: AppearanceSettingsActions,
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
                    uiThemeEngine = settings.appearance.uiThemeEngine,
                    languageMode = settings.appearance.languageMode,
                    uiScalePercent = settings.appearance.uiScalePercent,
                    monetEnabled = settings.appearance.monetEnabled,
                    monetKeyColor = settings.appearance.monetKeyColor,
                    blurEnabled = settings.appearance.blurEnabled,
                    onThemeModeSelected = actions.onThemeModeSelected,
                    onUiThemeEngineSelected = actions.onUiThemeEngineSelected,
                    onLanguageModeSelected = actions.onLanguageModeSelected,
                    onUiScalePercentChanged = actions.onUiScalePercentChanged,
                    onMonetEnabledChanged = actions.onMonetEnabledChanged,
                    onMonetKeyColorSelected = actions.onMonetKeyColorSelected,
                    onBlurEnabledChanged = actions.onBlurEnabledChanged,
                )
            }
        }
    }
}
