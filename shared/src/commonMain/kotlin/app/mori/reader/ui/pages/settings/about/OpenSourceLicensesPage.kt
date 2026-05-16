package app.mori.reader.ui.pages.settings.about

import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.ui.theme.MoriTheme

@Composable
fun OpenSourceLicensesPage(
    settings: AppSettings,
    onBack: () -> Unit,
) {
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> MiuixOpenSourceLicensesPage(settings = settings, onBack = onBack)
        UiThemeEngine.Material -> MaterialOpenSourceLicensesPage(settings = settings, onBack = onBack)
    }
}
