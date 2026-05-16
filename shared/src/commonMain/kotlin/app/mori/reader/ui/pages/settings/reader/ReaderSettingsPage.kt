package app.mori.reader.ui.pages.settings.reader

import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.ui.theme.MoriTheme

@Composable
fun ReaderSettingsPage(
    settings: AppSettings,
    onSettingsIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixReaderSettingsPage(
                settings = settings,
                onSettingsIntent = onSettingsIntent,
                onBack = onBack,
            )
        }

        UiThemeEngine.Material -> {
            MaterialReaderSettingsPage(
                settings = settings,
                onSettingsIntent = onSettingsIntent,
                onBack = onBack,
            )
        }
    }
}
