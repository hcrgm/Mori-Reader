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
import app.mori.reader.ui.AppIntent
import app.mori.reader.ui.AppState
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.AppThemeSettingsCard
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.utils.overScrollVertical
import app.mori.reader.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppThemeSettingsPage(
    state: AppState,
    message: String?,
    onIntent: (AppIntent) -> Unit,
    onBack: () -> Unit,
) {
    MoriPageScaffold(
        title = stringResource(Res.string.appearance_app_title),
        subtitle = "",
        blurEnabled = state.settings.blurEnabled,
        message = message,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
            }
        },
        actions = {},
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
                SmallTitle(text = stringResource(Res.string.appearance_app_title))
                AppThemeSettingsCard(
                    themeMode = state.settings.themeMode,
                    languageMode = state.settings.languageMode,
                    blurEnabled = state.settings.blurEnabled,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    onThemeModeSelected = { onIntent(AppIntent.SetThemeMode(it)) },
                    onLanguageModeSelected = { onIntent(AppIntent.SetLanguageMode(it)) },
                    onBlurEnabledChanged = { onIntent(AppIntent.SetBlurEnabled(it)) },
                )
            }
        }
    }
}
