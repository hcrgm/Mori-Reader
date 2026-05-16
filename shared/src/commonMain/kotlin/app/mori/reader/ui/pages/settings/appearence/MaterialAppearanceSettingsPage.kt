package app.mori.reader.ui.pages.settings.appearence

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.LanguageMode
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.appearance_app_title
import app.mori.reader.shared.generated.resources.appearance_blur_summary
import app.mori.reader.shared.generated.resources.appearance_blur_title
import app.mori.reader.shared.generated.resources.appearance_color_scheme_title
import app.mori.reader.shared.generated.resources.appearance_language_summary
import app.mori.reader.shared.generated.resources.appearance_language_title
import app.mori.reader.shared.generated.resources.appearance_monet_summary
import app.mori.reader.shared.generated.resources.appearance_monet_title
import app.mori.reader.shared.generated.resources.appearance_theme_title
import app.mori.reader.shared.generated.resources.appearance_ui_engine_summary
import app.mori.reader.shared.generated.resources.appearance_ui_engine_title
import app.mori.reader.shared.generated.resources.appearance_ui_scale_summary
import app.mori.reader.shared.generated.resources.appearance_ui_scale_title
import app.mori.reader.shared.generated.resources.cd_appearance
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.color_amber
import app.mori.reader.shared.generated.resources.color_blue
import app.mori.reader.shared.generated.resources.color_blue_grey
import app.mori.reader.shared.generated.resources.color_brown
import app.mori.reader.shared.generated.resources.color_cyan
import app.mori.reader.shared.generated.resources.color_deep_purple
import app.mori.reader.shared.generated.resources.color_green
import app.mori.reader.shared.generated.resources.color_indigo
import app.mori.reader.shared.generated.resources.color_orange
import app.mori.reader.shared.generated.resources.color_pink
import app.mori.reader.shared.generated.resources.color_purple
import app.mori.reader.shared.generated.resources.color_red
import app.mori.reader.shared.generated.resources.color_sakura
import app.mori.reader.shared.generated.resources.color_teal
import app.mori.reader.shared.generated.resources.color_yellow
import app.mori.reader.shared.generated.resources.language_chinese
import app.mori.reader.shared.generated.resources.language_english
import app.mori.reader.shared.generated.resources.language_follow_system
import app.mori.reader.shared.generated.resources.monet_key_color_default
import app.mori.reader.shared.generated.resources.theme_dark
import app.mori.reader.shared.generated.resources.theme_engine_material
import app.mori.reader.shared.generated.resources.theme_engine_miuix
import app.mori.reader.shared.generated.resources.theme_follow_system
import app.mori.reader.shared.generated.resources.theme_light
import app.mori.reader.ui.components.material.MaterialBackButton
import app.mori.reader.ui.components.material.MaterialExpressiveSwitch
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MaterialAppearanceSettingsPage(
    settings: AppSettings,
    actions: AppearanceSettingsActions,
    onBack: () -> Unit,
) {
    MoriPageScaffold(
        title = stringResource(Res.string.cd_appearance),
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            Row {
                MaterialBackButton(onClick = onBack, contentDescription = stringResource(Res.string.cd_back))
                Spacer(modifier = Modifier.size(16.dp))
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                MaterialThemeModeSection(
                    selected = settings.appearance.themeMode,
                    onSelected = actions.onThemeModeSelected,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                MaterialColorSchemeSection(
                    selectedColor =
                        if (settings.appearance.monetEnabled) {
                            settings.appearance.monetKeyColor
                        } else {
                            0L
                        },
                    onColorSelected = { color ->
                        if (!settings.appearance.monetEnabled) {
                            actions.onMonetEnabledChanged(true)
                        }
                        actions.onMonetKeyColorSelected(color)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                MaterialAppearanceSection(
                    title = stringResource(Res.string.appearance_app_title),
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    MaterialChoiceRow(
                        title = stringResource(Res.string.appearance_ui_engine_title),
                        summary = stringResource(Res.string.appearance_ui_engine_summary),
                        selectedLabel = settings.appearance.uiThemeEngine.localizedLabel(),
                        options =
                            UiThemeEngine.entries.map { engine ->
                                MaterialDropdownOption(
                                    label = engine.localizedLabel(),
                                    onSelected = { actions.onUiThemeEngineSelected(engine) },
                                )
                        },
                        shape = materialSegmentedItemShape(index = 0, count = 4),
                    )
                    MaterialChoiceRow(
                        title = stringResource(Res.string.appearance_language_title),
                        summary = stringResource(Res.string.appearance_language_summary),
                        selectedLabel = settings.appearance.languageMode.localizedLabel(),
                        options =
                            LanguageMode.entries.map { mode ->
                                MaterialDropdownOption(
                                    label = mode.localizedLabel(),
                                    onSelected = { actions.onLanguageModeSelected(mode) },
                                )
                            },
                        shape = materialSegmentedItemShape(index = 1, count = 4),
                    )
                    MaterialUiScaleRow(
                        uiScalePercent = settings.appearance.uiScalePercent,
                        onUiScalePercentChanged = actions.onUiScalePercentChanged,
                        shape = materialSegmentedItemShape(index = 2, count = 4),
                    )
                    MaterialSwitchRow(
                        title = stringResource(Res.string.appearance_monet_title),
                        summary = stringResource(Res.string.appearance_monet_summary),
                        checked = settings.appearance.monetEnabled,
                        onCheckedChange = actions.onMonetEnabledChanged,
                        shape = materialSegmentedItemShape(index = 3, count = 4),
                    )
                }
            }
            if (isRenderEffectSupported()) {
                item {
                    MaterialAppearanceSection(
                        title = "",
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        MaterialSwitchRow(
                            title = stringResource(Res.string.appearance_blur_title),
                            summary = stringResource(Res.string.appearance_blur_summary),
                            checked = settings.appearance.blurEnabled,
                            onCheckedChange = actions.onBlurEnabledChanged,
                            shape = materialSegmentedItemShape(index = 0, count = 1),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialAppearanceSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            content()
        }
    }
}

@Composable
private fun MaterialChoiceRow(
    title: String,
    summary: String?,
    selectedLabel: String,
    options: List<MaterialDropdownOption>,
    shape: Shape,
) {
    var expanded by remember { mutableStateOf(false) }
    MaterialSettingsRowSurface(shape = shape, onClick = { expanded = true }) {
        Box {
            ListItem(
                headlineContent = { Text(text = title) },
                supportingContent = { summary?.let { Text(text = it) } },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
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
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option.label) },
                        onClick = {
                            option.onSelected()
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MaterialThemeModeSection(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    MaterialAppearanceSection(
        title = stringResource(Res.string.appearance_theme_title),
        modifier = modifier,
    ) {
        val haptic = LocalHapticFeedback.current
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                ThemeMode.entries.forEachIndexed { index, mode ->
                    ToggleButton(
                        checked = mode == selected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                onSelected(mode)
                            }
                        },
                        modifier =
                            Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                        shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                ThemeMode.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                    ) {
                        Icon(
                            imageVector = mode.icon(),
                            contentDescription = mode.localizedLabel(),
                        )
                    }
                }
            }
        }
    }
}

private fun ThemeMode.icon(): ImageVector =
    when (this) {
        ThemeMode.System -> Icons.Filled.Brightness4
        ThemeMode.Light -> Icons.Filled.Brightness7
        ThemeMode.Dark -> Icons.Filled.Brightness3
    }

@Composable
private fun MaterialUiScaleRow(
    uiScalePercent: Int,
    onUiScalePercentChanged: (Int) -> Unit,
    shape: Shape,
) {
    var sliderValue by remember(uiScalePercent) { mutableFloatStateOf(uiScalePercent.toFloat()) }
    MaterialSettingsRowSurface(shape = shape) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(text = stringResource(Res.string.appearance_ui_scale_title))
                    Text(
                        text = stringResource(Res.string.appearance_ui_scale_summary),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = "${sliderValue.roundToScaleStep()}%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 80f..150f,
                steps = 6,
                onValueChangeFinished = { onUiScalePercentChanged(sliderValue.roundToScaleStep()) },
            )
        }
    }
}

@Composable
private fun MaterialSwitchRow(
    title: String,
    summary: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape,
) {
    MaterialSettingsRowSurface(shape = shape) {
        ListItem(
            headlineContent = { Text(text = title) },
            supportingContent = { summary?.let { Text(text = it) } },
            trailingContent = {
                MaterialExpressiveSwitch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

@Composable
private fun MaterialColorSchemeSection(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = materialKeyColorOptions()
    MaterialAppearanceSection(
        title = stringResource(Res.string.appearance_color_scheme_title),
        modifier = modifier,
    ) {
        MaterialSettingsRowSurface(shape = materialSegmentedItemShape(index = 0, count = 1)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(options, key = { it.value }) { option ->
                    MaterialColorOptionButton(
                        option = option,
                        selected = option.value == selectedColor,
                        onClick = { onColorSelected(option.value) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialColorOptionButton(
    option: MaterialKeyColorOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val colorScheme = MaterialTheme.colorScheme
    val swatchColor = option.value.takeIf { it != 0L }?.let(::Color) ?: colorScheme.primary
    val containerColor =
        if (option.value == 0L) {
            colorScheme.surfaceContainer
        } else {
            swatchColor.copy(alpha = 0.18f)
        }
    val primaryArc =
        if (option.value == 0L) {
            colorScheme.primaryContainer
        } else {
            swatchColor.copy(alpha = 0.38f)
        }
    val tertiaryArc =
        if (option.value == 0L) {
            colorScheme.tertiaryContainer
        } else {
            swatchColor.copy(alpha = 0.72f)
        }
    Column(
        modifier = Modifier.size(width = 72.dp, height = 104.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClick()
            },
            shape = RoundedCornerShape(20.dp),
            color = containerColor,
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(48.dp)) {
                    drawArc(
                        color = primaryArc,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                    )
                    drawArc(
                        color = tertiaryArc,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                    )
                }
                val scale by animateFloatAsState(targetValue = if (selected) 1.1f else 1.0f)
                Box(
                    modifier =
                        Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 0.8f),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(56.dp)
                                    .border(2.dp, colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colorScheme.primary),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = colorScheme.onPrimary,
                                    modifier =
                                        Modifier
                                            .align(Alignment.Center)
                                            .size(16.dp),
                                )
                            }
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !selected,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut() + scaleOut(targetScale = 0.8f),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(swatchColor),
                        )
                    }
                }
            }
        }
        Text(
            text = option.label,
            color = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun MaterialSettingsRowSurface(
    shape: Shape,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = shape,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier
                            .clip(shape)
                            .clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ),
    ) {
        content()
    }
}

@Composable
private fun materialSegmentedItemShape(
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

private data class MaterialDropdownOption(
    val label: String,
    val onSelected: () -> Unit,
)

private data class MaterialKeyColorOption(
    val value: Long,
    val label: String,
)

@Composable
private fun ThemeMode.localizedLabel(): String =
    when (this) {
        ThemeMode.System -> stringResource(Res.string.theme_follow_system)
        ThemeMode.Light -> stringResource(Res.string.theme_light)
        ThemeMode.Dark -> stringResource(Res.string.theme_dark)
    }

@Composable
private fun UiThemeEngine.localizedLabel(): String =
    when (this) {
        UiThemeEngine.Miuix -> stringResource(Res.string.theme_engine_miuix)
        UiThemeEngine.Material -> stringResource(Res.string.theme_engine_material)
    }

@Composable
private fun LanguageMode.localizedLabel(): String =
    when (this) {
        LanguageMode.System -> stringResource(Res.string.language_follow_system)
        LanguageMode.English -> stringResource(Res.string.language_english)
        LanguageMode.Chinese -> stringResource(Res.string.language_chinese)
    }

@Composable
private fun materialKeyColorOptions(): List<MaterialKeyColorOption> =
    listOf(
        MaterialKeyColorOption(0L, stringResource(Res.string.monet_key_color_default)),
        MaterialKeyColorOption(0xFFF44336L, stringResource(Res.string.color_red)),
        MaterialKeyColorOption(0xFFE91E63L, stringResource(Res.string.color_pink)),
        MaterialKeyColorOption(0xFF9C27B0L, stringResource(Res.string.color_purple)),
        MaterialKeyColorOption(0xFF673AB7L, stringResource(Res.string.color_deep_purple)),
        MaterialKeyColorOption(0xFF3F51B5L, stringResource(Res.string.color_indigo)),
        MaterialKeyColorOption(0xFF2196F3L, stringResource(Res.string.color_blue)),
        MaterialKeyColorOption(0xFF00BCD4L, stringResource(Res.string.color_cyan)),
        MaterialKeyColorOption(0xFF009688L, stringResource(Res.string.color_teal)),
        MaterialKeyColorOption(0xFF4FAF50L, stringResource(Res.string.color_green)),
        MaterialKeyColorOption(0xFFFFEB3BL, stringResource(Res.string.color_yellow)),
        MaterialKeyColorOption(0xFFFFC107L, stringResource(Res.string.color_amber)),
        MaterialKeyColorOption(0xFFFF9800L, stringResource(Res.string.color_orange)),
        MaterialKeyColorOption(0xFF795548L, stringResource(Res.string.color_brown)),
        MaterialKeyColorOption(0xFF607D8FL, stringResource(Res.string.color_blue_grey)),
        MaterialKeyColorOption(0xFFFF9CA8L, stringResource(Res.string.color_sakura)),
    )

private fun Float.roundToScaleStep(): Int = ((this / 10f).roundToInt() * 10).coerceIn(80, 150)
