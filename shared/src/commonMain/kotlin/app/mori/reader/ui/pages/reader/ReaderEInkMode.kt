package app.mori.reader.ui.pages.reader

import androidx.compose.ui.graphics.Color
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.UiThemeEngine

internal fun materialReaderEInkMode(settings: AppSettings): Boolean =
    settings.appearance.uiThemeEngine == UiThemeEngine.Material &&
        settings.appearance.materialEInkMode

internal fun readerBackgroundColor(
    isDark: Boolean,
    materialEInkMode: Boolean,
): Color =
    when {
        isDark -> Color(0xFF000000)
        materialEInkMode -> Color(0xFFFFFFFF)
        else -> Color(0xFFFBFAF7)
    }

internal fun readerEInkSeedColor(isDark: Boolean): Color =
    if (isDark) {
        Color(0xFFFFFFFF)
    } else {
        Color(0xFF000000)
    }
