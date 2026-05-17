package app.mori.reader.ui.pages.reader

import androidx.compose.ui.graphics.Color
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.AppearanceSettings
import app.mori.reader.data.settings.UiThemeEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReaderEInkModeTest {
    @Test
    fun `material reader e-ink mode only enables for material engine`() {
        val materialSettings =
            AppSettings(
                appearance =
                    AppearanceSettings(
                        uiThemeEngine = UiThemeEngine.Material,
                        materialEInkMode = true,
                    ),
            )
        val miuixSettings =
            AppSettings(
                appearance =
                    AppearanceSettings(
                        uiThemeEngine = UiThemeEngine.Miuix,
                        materialEInkMode = true,
                    ),
            )

        assertTrue(materialReaderEInkMode(materialSettings))
        assertFalse(materialReaderEInkMode(miuixSettings))
    }

    @Test
    fun `reader background uses e-ink light token when enabled`() {
        assertEquals(Color(0xFFFFFFFF), readerBackgroundColor(isDark = false, materialEInkMode = true))
        assertEquals(Color(0xFFFBFAF7), readerBackgroundColor(isDark = false, materialEInkMode = false))
    }

    @Test
    fun `reader background uses dark token consistently`() {
        assertEquals(Color(0xFF000000), readerBackgroundColor(isDark = true, materialEInkMode = true))
        assertEquals(Color(0xFF000000), readerBackgroundColor(isDark = true, materialEInkMode = false))
    }
}
