package app.mori.reader.ui.theme

import androidx.compose.ui.graphics.Color
import app.mori.reader.data.settings.AppearanceSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaterialEInkThemeTest {
    @Test
    fun `toMoriThemeState includes material eink mode`() {
        val themeState =
            AppearanceSettings(
                materialEInkMode = true,
                blurEnabled = false,
            ).toMoriThemeState()

        assertTrue(themeState.materialEInkMode)
        assertFalse(themeState.blurEnabled)
    }

    @Test
    fun `material e-ink light color scheme uses grayscale tokens`() {
        val colorScheme = materialEInkColorScheme(darkTheme = false)

        assertEquals(Color(0xFFFFFFFF), colorScheme.background)
        assertEquals(Color(0xFF000000), colorScheme.onBackground)
        assertEquals(Color(0xFFFFFFFF), colorScheme.surfaceVariant)
        assertEquals(Color(0xFF000000), colorScheme.primary)
        assertEquals(Color(0xFFFFFFFF), colorScheme.primaryContainer)
    }

    @Test
    fun `material e-ink dark color scheme uses grayscale tokens`() {
        val colorScheme = materialEInkColorScheme(darkTheme = true)

        assertEquals(Color(0xFF000000), colorScheme.background)
        assertEquals(Color(0xFFFFFFFF), colorScheme.onBackground)
        assertEquals(Color(0xFF000000), colorScheme.surfaceVariant)
        assertEquals(Color(0xFFFFFFFF), colorScheme.primary)
        assertEquals(Color(0xFF000000), colorScheme.primaryContainer)
    }
}
