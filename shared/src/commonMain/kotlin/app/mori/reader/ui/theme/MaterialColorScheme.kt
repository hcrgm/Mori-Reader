package app.mori.reader.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
internal expect fun rememberMaterialColorScheme(
    darkTheme: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
): ColorScheme
