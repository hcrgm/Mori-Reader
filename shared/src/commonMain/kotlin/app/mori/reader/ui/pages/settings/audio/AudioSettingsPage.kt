package app.mori.reader.ui.pages.settings.audio

import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.features.settings.presentation.SettingsIntent
import app.mori.reader.features.settings.presentation.SettingsUiState
import app.mori.reader.ui.theme.MoriTheme

@Composable
fun AudioSettingsPage(
    settings: AppSettings,
    settingsUiState: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
) {
    val controller =
        rememberAudioSettingsController(
            settings = settings,
            settingsUiState = settingsUiState,
            onIntent = onIntent,
        )

    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixAudioSettingsPage(
                settings = settings,
                controller = controller,
                onIntent = onIntent,
                onBack = onBack,
            )
        }

        UiThemeEngine.Material -> {
            MaterialAudioSettingsPage(
                settings = settings,
                controller = controller,
                onIntent = onIntent,
                onBack = onBack,
            )
        }
    }
}
