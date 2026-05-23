package app.mori.reader.ui.components.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.ui.components.settings.MaterialSettingsSurface
import app.mori.reader.ui.theme.MoriTheme

data class MaterialDropdownMenuOption(
    val label: String,
    val selected: Boolean = false,
    val dividerBefore: Boolean = false,
    val onSelected: () -> Unit,
)

@Composable
fun MaterialDropdownSelectorRow(
    title: String,
    selectedLabel: String,
    options: List<MaterialDropdownMenuOption>,
    modifier: Modifier = Modifier,
    summary: String? = null,
    shape: Shape = MaterialTheme.shapes.large,
    groupedInSection: Boolean = false,
    showDivider: Boolean = false,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val clickable = enabled && options.isNotEmpty()

    MaterialSettingsSurface(
        shape = shape,
        groupedInSection = groupedInSection,
        showDivider = showDivider,
        modifier = modifier,
        onClick = if (clickable) ({ expanded = true }) else null,
    ) {
        Box {
            ListItem(
                headlineContent = { Text(text = title) },
                supportingContent = {
                    summary?.let {
                        Text(
                            text = it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = selectedLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            MaterialDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                options = options,
            )
        }
    }
}

@Composable
fun MaterialDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<MaterialDropdownMenuOption>,
    modifier: Modifier = Modifier,
) {
    val isEInkMode = MoriTheme.materialEInkMode

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        options.forEach { option ->
            if (option.dividerBefore) {
                HorizontalDivider()
            }
            val selectedBackgroundColor =
                if (isEInkMode) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            val selectedContentColor =
                if (isEInkMode) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                }

            DropdownMenuItem(
                modifier =
                    if (option.selected) {
                        Modifier.background(selectedBackgroundColor)
                    } else {
                        Modifier
                    },
                text = {
                    Text(
                        text = option.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color =
                            if (option.selected) {
                                selectedContentColor
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                },
                trailingIcon = {
                    if (option.selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = selectedContentColor,
                        )
                    }
                },
                onClick = {
                    option.onSelected()
                    onDismissRequest()
                },
            )
        }
    }
}
