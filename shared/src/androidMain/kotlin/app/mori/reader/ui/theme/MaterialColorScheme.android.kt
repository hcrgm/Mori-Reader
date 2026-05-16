package app.mori.reader.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

@Composable
internal actual fun rememberMaterialColorScheme(
    darkTheme: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
): ColorScheme {
    val context = LocalContext.current
    if (monetEnabled && monetKeyColor == 0L && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    val seedColor =
        when {
            monetEnabled && monetKeyColor != 0L -> Color(monetKeyColor)
            else -> Color(0xFF6750A4)
        }

    return rememberDynamicColorScheme(
        seedColor = seedColor,
        isDark = darkTheme,
        style = PaletteStyle.TonalSpot,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
    )
}
