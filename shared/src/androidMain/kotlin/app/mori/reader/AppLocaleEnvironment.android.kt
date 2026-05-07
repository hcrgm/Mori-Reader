package app.mori.reader

import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import app.mori.reader.data.settings.LanguageMode
import java.util.Locale

@Composable
actual fun AppLocaleEnvironment(
    mode: LanguageMode,
    content: @Composable () -> Unit,
) {
    val baseConfiguration = LocalConfiguration.current
    val context = LocalContext.current
    val localeTag =
        when (mode) {
            LanguageMode.System -> null
            LanguageMode.English -> "en"
            LanguageMode.Chinese -> "zh"
        }

    val localizedConfiguration =
        Configuration(baseConfiguration).apply {
            if (localeTag != null) {
                val locale = Locale.forLanguageTag(localeTag)
                Locale.setDefault(locale)
                setLocale(locale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setLocales(android.os.LocaleList(locale))
                }
            }
        }

    CompositionLocalProvider(
        LocalConfiguration provides localizedConfiguration,
    ) {
        key(
            localeTag ?: context.resources.configuration.locales[0]
                ?.toLanguageTag()
                .orEmpty(),
        ) {
            content()
        }
    }
}
