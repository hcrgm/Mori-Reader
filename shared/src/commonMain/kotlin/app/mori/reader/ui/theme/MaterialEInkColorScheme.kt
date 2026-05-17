package app.mori.reader.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val EInkBlack = Color(0xFF000000)
private val EInkWhite = Color(0xFFFFFFFF)

internal fun materialEInkColorScheme(darkTheme: Boolean): ColorScheme =
    if (darkTheme) {
        darkMaterialEInkColorScheme()
    } else {
        lightMaterialEInkColorScheme()
    }

private fun lightMaterialEInkColorScheme(): ColorScheme =
    lightColorScheme(
        primary = EInkBlack,
        onPrimary = EInkWhite,
        primaryContainer = EInkWhite,
        onPrimaryContainer = EInkBlack,
        secondary = EInkBlack,
        onSecondary = EInkWhite,
        secondaryContainer = EInkWhite,
        onSecondaryContainer = EInkBlack,
        tertiary = EInkBlack,
        onTertiary = EInkWhite,
        tertiaryContainer = EInkWhite,
        onTertiaryContainer = EInkBlack,
        background = EInkWhite,
        onBackground = EInkBlack,
        surface = EInkWhite,
        onSurface = EInkBlack,
        surfaceVariant = EInkWhite,
        onSurfaceVariant = EInkBlack,
        surfaceContainer = EInkWhite,
        surfaceContainerHigh = EInkWhite,
        surfaceContainerHighest = EInkWhite,
        outline = EInkBlack,
        outlineVariant = EInkBlack,
        error = EInkBlack,
        onError = EInkWhite,
        errorContainer = EInkWhite,
        onErrorContainer = EInkBlack,
        scrim = EInkBlack,
    )

private fun darkMaterialEInkColorScheme(): ColorScheme =
    darkColorScheme(
        primary = EInkWhite,
        onPrimary = EInkBlack,
        primaryContainer = EInkBlack,
        onPrimaryContainer = EInkWhite,
        secondary = EInkWhite,
        onSecondary = EInkBlack,
        secondaryContainer = EInkBlack,
        onSecondaryContainer = EInkWhite,
        tertiary = EInkWhite,
        onTertiary = EInkBlack,
        tertiaryContainer = EInkBlack,
        onTertiaryContainer = EInkWhite,
        background = EInkBlack,
        onBackground = EInkWhite,
        surface = EInkBlack,
        onSurface = EInkWhite,
        surfaceVariant = EInkBlack,
        onSurfaceVariant = EInkWhite,
        surfaceContainer = EInkBlack,
        surfaceContainerHigh = EInkBlack,
        surfaceContainerHighest = EInkBlack,
        outline = EInkWhite,
        outlineVariant = EInkWhite,
        error = EInkWhite,
        onError = EInkBlack,
        errorContainer = EInkBlack,
        onErrorContainer = EInkWhite,
        scrim = EInkBlack,
    )
