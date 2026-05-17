package app.mori.reader.ui.theme

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun rememberMaterialThemeConfig(
    darkTheme: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
): MaterialThemeConfig {
    val context = LocalContext.current
    if (monetEnabled && monetKeyColor == 0L && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val systemColorScheme =
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        return MaterialThemeConfig(
            seedColor = systemColorScheme.primary,
            colorSchemeOverride = systemColorScheme,
        )
    }

    val seedColor =
        when {
            monetEnabled && monetKeyColor != 0L -> Color(monetKeyColor)
            else -> Color(0xFF6750A4)
        }

    return MaterialThemeConfig(
        seedColor = seedColor,
    )
}
