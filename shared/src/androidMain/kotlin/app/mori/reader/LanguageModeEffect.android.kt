package app.mori.reader

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.os.LocaleListCompat
import app.mori.reader.data.settings.LanguageMode

@Composable
actual fun ApplyLanguageModeEffect(mode: LanguageMode) {
    SideEffect {
        val languageTags =
            when (mode) {
                LanguageMode.System -> ""
                LanguageMode.English -> "en"
                LanguageMode.Chinese -> "zh"
            }
        val locales = LocaleListCompat.forLanguageTags(languageTags)
        if (AppCompatDelegate.getApplicationLocales() != locales) {
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}
