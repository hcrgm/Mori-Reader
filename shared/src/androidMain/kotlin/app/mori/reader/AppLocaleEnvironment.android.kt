package app.mori.reader

import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import app.mori.reader.data.settings.LanguageMode
import java.util.Locale

@Composable
actual fun AppLocaleEnvironment(
    mode: LanguageMode,
    content: @Composable () -> Unit,
) {
    val baseConfiguration = LocalConfiguration.current
    val locale =
        when (mode) {
            LanguageMode.System -> Resources.getSystem().configuration.locales[0]
            LanguageMode.English -> Locale.forLanguageTag("en")
            LanguageMode.Chinese -> Locale.forLanguageTag("zh")
        }

    val localizedConfiguration =
        Configuration(baseConfiguration).apply {
            Locale.setDefault(locale)
            setLocale(locale)
            setLocales(android.os.LocaleList(locale))
        }

    CompositionLocalProvider(
        LocalConfiguration provides localizedConfiguration,
    ) {
        content()
    }
}
