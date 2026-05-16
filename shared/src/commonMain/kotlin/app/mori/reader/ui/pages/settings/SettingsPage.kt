package app.mori.reader.ui.pages.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ChromeReaderMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.settings_about_title
import app.mori.reader.shared.generated.resources.settings_anki_title
import app.mori.reader.shared.generated.resources.settings_appearance_title
import app.mori.reader.shared.generated.resources.settings_audio_title
import app.mori.reader.shared.generated.resources.settings_dictionary_title
import app.mori.reader.shared.generated.resources.settings_reader_title
import app.mori.reader.ui.theme.MoriTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsPage(
    settings: AppSettings,
    fixedPadding: PaddingValues,
    onOpenAppearanceSettings: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onOpenAnkiSettings: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixSettingsPage(
                settings = settings,
                fixedPadding = fixedPadding,
                onOpenAppearanceSettings = onOpenAppearanceSettings,
                onOpenReaderSettings = onOpenReaderSettings,
                onOpenDictionarySettings = onOpenDictionarySettings,
                onOpenAudioSettings = onOpenAudioSettings,
                onOpenAnkiSettings = onOpenAnkiSettings,
                onOpenAbout = onOpenAbout,
            )
        }

        UiThemeEngine.Material -> {
            MaterialSettingsPage(
                settings = settings,
                fixedPadding = fixedPadding,
                onOpenAppearanceSettings = onOpenAppearanceSettings,
                onOpenReaderSettings = onOpenReaderSettings,
                onOpenDictionarySettings = onOpenDictionarySettings,
                onOpenAudioSettings = onOpenAudioSettings,
                onOpenAnkiSettings = onOpenAnkiSettings,
                onOpenAbout = onOpenAbout,
            )
        }
    }
}

@Composable
internal fun settingsEntries(
    onOpenAppearanceSettings: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onOpenDictionarySettings: () -> Unit,
    onOpenAudioSettings: () -> Unit,
    onOpenAnkiSettings: () -> Unit,
): List<SettingsEntry> =
    listOf(
        SettingsEntry(
            title = stringResource(Res.string.settings_appearance_title),
            icon = Icons.Rounded.Palette,
            onClick = onOpenAppearanceSettings,
        ),
        SettingsEntry(
            title = stringResource(Res.string.settings_reader_title),
            icon = Icons.AutoMirrored.Rounded.ChromeReaderMode,
            onClick = onOpenReaderSettings,
        ),
        SettingsEntry(
            title = stringResource(Res.string.settings_dictionary_title),
            icon = Icons.Rounded.Translate,
            onClick = onOpenDictionarySettings,
        ),
        SettingsEntry(
            title = stringResource(Res.string.settings_audio_title),
            icon = Icons.Rounded.MusicNote,
            onClick = onOpenAudioSettings,
        ),
        SettingsEntry(
            title = stringResource(Res.string.settings_anki_title),
            icon = Icons.Rounded.School,
            onClick = onOpenAnkiSettings,
        ),
    )

@Composable
internal fun settingsAboutEntries(onOpenAbout: () -> Unit): List<SettingsEntry> =
    listOf(
        SettingsEntry(
            title = stringResource(Res.string.settings_about_title),
            icon = Icons.Rounded.Info,
            onClick = onOpenAbout,
        ),
    )

internal data class SettingsEntry(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)
