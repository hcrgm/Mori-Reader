package app.mori.reader.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.mori.reader.ui.components.material.materialCardBorder
import app.mori.reader.ui.components.material.materialCardContainerColor
import app.mori.reader.ui.theme.MoriTheme

@Composable
fun MaterialSettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )
        content()
    }
}

@Composable
fun MaterialSettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (MoriTheme.materialEInkMode) {
        Surface(
            color = materialCardContainerColor(),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = MaterialTheme.shapes.large,
            border = materialCardBorder(),
            modifier = modifier.fillMaxWidth(),
        ) {
            Column(content = content)
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content,
        )
    }
}

@Composable
fun MaterialSettingsSurface(
    shape: Shape = MaterialTheme.shapes.large,
    modifier: Modifier = Modifier,
    color: Color = materialCardContainerColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    groupedInSection: Boolean = false,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val useEInkGroupedStyle = groupedInSection && MoriTheme.materialEInkMode
    val surfaceShape = if (useEInkGroupedStyle) RectangleShape else shape
    Surface(
        color = color,
        contentColor = contentColor,
        shape = surfaceShape,
        border = if (useEInkGroupedStyle) null else materialCardBorder(),
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier
                            .clip(surfaceShape)
                            .clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ),
    ) {
        Column {
            if (useEInkGroupedStyle && showDivider) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            Column(content = content)
        }
    }
}

@Composable
fun materialSettingsSegmentedItemShape(
    index: Int,
    count: Int,
): Shape =
    when {
        count == 1 -> {
            MaterialTheme.shapes.large
        }

        index == 0 -> {
            MaterialTheme.shapes.large.copy(
                bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
                bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd,
            )
        }

        index == count - 1 -> {
            MaterialTheme.shapes.large.copy(
                topStart = MaterialTheme.shapes.extraSmall.topStart,
                topEnd = MaterialTheme.shapes.extraSmall.topEnd,
            )
        }

        else -> {
            MaterialTheme.shapes.extraSmall
        }
    }
