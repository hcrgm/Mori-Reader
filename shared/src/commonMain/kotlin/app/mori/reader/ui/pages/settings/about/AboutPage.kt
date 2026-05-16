package app.mori.reader.ui.pages.settings.about

import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.ui.theme.MoriTheme

@Composable
fun AboutPage(
    settings: AppSettings,
    onOpenLicenses: () -> Unit,
    onBack: () -> Unit,
) {
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixAboutPage(
                settings = settings,
                onOpenLicenses = onOpenLicenses,
                onBack = onBack,
            )
        }

        UiThemeEngine.Material -> {
            MaterialAboutPage(
                settings = settings,
                onOpenLicenses = onOpenLicenses,
                onBack = onBack,
            )
        }
    }
}
