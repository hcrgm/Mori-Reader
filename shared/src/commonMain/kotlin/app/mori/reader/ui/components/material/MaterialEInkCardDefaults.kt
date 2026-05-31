package app.mori.reader.ui.components.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.mori.reader.ui.theme.MoriTheme

@Composable
internal fun materialCardContainerColor(defaultColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)): Color =
    if (MoriTheme.materialEInkMode) {
        MaterialTheme.colorScheme.surface
    } else {
        defaultColor
    }

@Composable
internal fun materialCardBorder(): BorderStroke? =
    if (MoriTheme.materialEInkMode) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    } else {
        null
    }
