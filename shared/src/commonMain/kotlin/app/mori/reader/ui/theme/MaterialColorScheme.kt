package app.mori.reader.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal expect fun rememberMaterialThemeConfig(
    darkTheme: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
): MaterialThemeConfig

internal data class MaterialThemeConfig(
    val seedColor: Color,
    val colorSchemeOverride: ColorScheme? = null,
)
