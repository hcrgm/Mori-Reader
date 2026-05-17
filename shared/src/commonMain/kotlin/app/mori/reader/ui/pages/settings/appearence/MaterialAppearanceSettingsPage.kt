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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
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
import app.mori.reader.shared.generated.resources.appearance_material_eink_mode_title
import app.mori.reader.shared.generated.resources.appearance_miuix_eink_dialog_message
import app.mori.reader.shared.generated.resources.appearance_miuix_eink_dialog_title
import app.mori.reader.shared.generated.resources.appearance_monet_summary
import app.mori.reader.shared.generated.resources.appearance_monet_title
import app.mori.reader.shared.generated.resources.appearance_theme_title
import app.mori.reader.shared.generated.resources.appearance_ui_engine_summary
import app.mori.reader.shared.generated.resources.appearance_ui_engine_title
import app.mori.reader.shared.generated.resources.appearance_ui_scale_summary
import app.mori.reader.shared.generated.resources.appearance_ui_scale_title
import app.mori.reader.shared.generated.resources.cd_appearance
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.cd_close
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
import app.mori.reader.shared.generated.resources.theme_follow_system_short
import app.mori.reader.shared.generated.resources.theme_light
import app.mori.reader.shared.generated.resources.theme_light_short
import app.mori.reader.shared.generated.resources.theme_dark_short
import app.mori.reader.ui.components.material.MaterialBackButton
import app.mori.reader.ui.components.material.MaterialDropdownMenuOption
import app.mori.reader.ui.components.material.MaterialDropdownSelectorRow
import app.mori.reader.ui.components.material.MaterialExpressiveSwitch
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MaterialSettingsGroup
import app.mori.reader.ui.components.settings.MaterialSettingsSurface
import app.mori.reader.ui.theme.MoriTheme
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
    var showMiuixEInkDialog by remember { mutableStateOf(false) }
    val hideMaterialColorTuning =
        settings.appearance.uiThemeEngine == UiThemeEngine.Material && settings.appearance.materialEInkMode

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
                    materialEInkMode = settings.appearance.materialEInkMode,
                    onMaterialEInkModeChanged = actions.onMaterialEInkModeChanged,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            if (!hideMaterialColorTuning) {
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
                                    selected = engine == settings.appearance.uiThemeEngine,
                                    onSelected = {
                                        if (
                                            settings.appearance.uiThemeEngine == UiThemeEngine.Material &&
                                            settings.appearance.materialEInkMode &&
                                            engine == UiThemeEngine.Miuix
                                        ) {
                                            showMiuixEInkDialog = true
                                        } else {
                                            actions.onUiThemeEngineSelected(engine)
                                        }
                                    },
                                )
                            },
                        shape = materialSegmentedItemShape(index = 0, count = 4),
                        showDivider = false,
                    )
                    MaterialChoiceRow(
                        title = stringResource(Res.string.appearance_language_title),
                        summary = stringResource(Res.string.appearance_language_summary),
                        selectedLabel = settings.appearance.languageMode.localizedLabel(),
                        options =
                            LanguageMode.entries.map { mode ->
                                MaterialDropdownOption(
                                    label = mode.localizedLabel(),
                                    selected = mode == settings.appearance.languageMode,
                                    onSelected = { actions.onLanguageModeSelected(mode) },
                                )
                            },
                        shape = materialSegmentedItemShape(index = 1, count = 4),
                        showDivider = true,
                    )
                    MaterialUiScaleRow(
                        uiScalePercent = settings.appearance.uiScalePercent,
                        onUiScalePercentChanged = actions.onUiScalePercentChanged,
                        shape = materialSegmentedItemShape(index = 2, count = if (hideMaterialColorTuning) 3 else 4),
                        showDivider = true,
                    )
                    if (!hideMaterialColorTuning) {
                        MaterialSwitchRow(
                            title = stringResource(Res.string.appearance_monet_title),
                            summary = stringResource(Res.string.appearance_monet_summary),
                            checked = settings.appearance.monetEnabled,
                            onCheckedChange = actions.onMonetEnabledChanged,
                            shape = materialSegmentedItemShape(index = 3, count = 4),
                            showDivider = true,
                        )
                    }
                }
            }
            if (isRenderEffectSupported() && !hideMaterialColorTuning) {
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
                            showDivider = false,
                        )
                    }
                }
            }
        }
    }

    if (showMiuixEInkDialog) {
        AlertDialog(
            onDismissRequest = { showMiuixEInkDialog = false },
            title = { Text(text = stringResource(Res.string.appearance_miuix_eink_dialog_title)) },
            text = { Text(text = stringResource(Res.string.appearance_miuix_eink_dialog_message)) },
            confirmButton = {
                TextButton(onClick = { showMiuixEInkDialog = false }) {
                    Text(text = stringResource(Res.string.cd_close))
                }
            },
        )
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
        MaterialSettingsGroup {
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
    showDivider: Boolean,
) {
    MaterialDropdownSelectorRow(
        title = title,
        summary = summary,
        selectedLabel = selectedLabel,
        options =
            options.map { option ->
                MaterialDropdownMenuOption(
                    label = option.label,
                    selected = option.selected,
                    onSelected = option.onSelected,
                )
            },
        shape = shape,
        groupedInSection = true,
        showDivider = showDivider,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MaterialThemeModeSection(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
    materialEInkMode: Boolean,
    onMaterialEInkModeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    MaterialAppearanceSection(
        title = stringResource(Res.string.appearance_theme_title),
        modifier = modifier,
    ) {
        val haptic = LocalHapticFeedback.current
        MaterialSettingsRowSurface(
            shape = materialSegmentedItemShape(index = 0, count = 1),
            groupedInSection = true,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = mode.icon(),
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.size(ToggleButtonDefaults.IconSpacing))
                                Text(
                                    text = mode.shortLabel(),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                )
                MaterialEInkModeCard(
                    checked = materialEInkMode,
                    onCheckedChange = onMaterialEInkModeChanged,
                )
            }
        }
    }
}

@Composable
private fun MaterialEInkModeCard(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        onClick = {
            onCheckedChange(!checked)
        },
        shape = MaterialTheme.shapes.medium,
        color =
            if (checked) {
                colorScheme.primaryContainer.copy(alpha = 0.62f)
            } else {
                Color.Transparent
            },
        contentColor =
            if (checked) {
                colorScheme.onPrimaryContainer
            } else {
                colorScheme.onSurface
            },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.appearance_material_eink_mode_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            MaterialExpressiveSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
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
    showDivider: Boolean,
) {
    var sliderValue by remember(uiScalePercent) { mutableFloatStateOf(uiScalePercent.toFloat()) }
    MaterialSettingsRowSurface(
        shape = shape,
        groupedInSection = true,
        showDivider = showDivider,
    ) {
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
    showDivider: Boolean,
) {
    MaterialSettingsRowSurface(
        shape = shape,
        groupedInSection = true,
        showDivider = showDivider,
    ) {
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
        MaterialSettingsRowSurface(
            shape = materialSegmentedItemShape(index = 0, count = 1),
            groupedInSection = true,
        ) {
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
                val reduceMotion = MoriTheme.materialEInkMode
                val targetScale = if (selected) 1.1f else 1.0f
                val animatedScale by animateFloatAsState(targetValue = targetScale)
                val scale = if (reduceMotion) targetScale else animatedScale
                Box(
                    modifier =
                        Modifier.graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val selectedIndicator: @Composable () -> Unit = {
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
                    val unselectedIndicator: @Composable () -> Unit = {
                        Box(
                            modifier =
                                Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(swatchColor),
                        )
                    }
                    if (reduceMotion) {
                        if (selected) {
                            selectedIndicator()
                        } else {
                            unselectedIndicator()
                        }
                    } else {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = selected,
                            enter = fadeIn() + scaleIn(initialScale = 0.8f),
                            exit = fadeOut() + scaleOut(targetScale = 0.8f),
                        ) {
                            selectedIndicator()
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !selected,
                            enter = fadeIn() + scaleIn(initialScale = 0.8f),
                            exit = fadeOut() + scaleOut(targetScale = 0.8f),
                        ) {
                            unselectedIndicator()
                        }
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
    groupedInSection: Boolean = false,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    MaterialSettingsSurface(
        shape = shape,
        groupedInSection = groupedInSection,
        showDivider = showDivider,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
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
    val selected: Boolean = false,
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
private fun ThemeMode.shortLabel(): String =
    when (this) {
        ThemeMode.System -> stringResource(Res.string.theme_follow_system_short)
        ThemeMode.Light -> stringResource(Res.string.theme_light_short)
        ThemeMode.Dark -> stringResource(Res.string.theme_dark_short)
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
