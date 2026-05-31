package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.ReaderPersonalizedScheme
import app.mori.reader.data.settings.ReaderSettings
import app.mori.reader.data.settings.effectiveReaderSettings
import app.mori.reader.data.settings.findReaderScheme
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.appearance_action_bar_pinned_title
import app.mori.reader.shared.generated.resources.appearance_avoid_page_break_title
import app.mori.reader.shared.generated.resources.appearance_character_spacing
import app.mori.reader.shared.generated.resources.appearance_font_size
import app.mori.reader.shared.generated.resources.appearance_fullscreen_title
import app.mori.reader.shared.generated.resources.appearance_hide_furigana_title
import app.mori.reader.shared.generated.resources.appearance_justify_title
import app.mori.reader.shared.generated.resources.appearance_popup_full_width_title
import app.mori.reader.shared.generated.resources.appearance_popup_height
import app.mori.reader.shared.generated.resources.appearance_popup_swipe_dismiss_title
import app.mori.reader.shared.generated.resources.appearance_popup_swipe_threshold
import app.mori.reader.shared.generated.resources.appearance_popup_width
import app.mori.reader.shared.generated.resources.appearance_show_reading_info_title
import app.mori.reader.shared.generated.resources.appearance_vertical_margin
import app.mori.reader.shared.generated.resources.appearance_writing_horizontal
import app.mori.reader.shared.generated.resources.appearance_writing_vertical
import app.mori.reader.shared.generated.resources.btn_add
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.shared.generated.resources.btn_close
import app.mori.reader.shared.generated.resources.btn_delete
import app.mori.reader.shared.generated.resources.btn_save
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.cd_delete
import app.mori.reader.shared.generated.resources.cd_rename
import app.mori.reader.shared.generated.resources.reader_reading_scheme_advanced_button
import app.mori.reader.shared.generated.resources.reader_reading_scheme_copy_suffix
import app.mori.reader.shared.generated.resources.reader_reading_scheme_create_based_on_global
import app.mori.reader.shared.generated.resources.reader_reading_scheme_create_based_on_selected
import app.mori.reader.shared.generated.resources.reader_reading_scheme_create_title
import app.mori.reader.shared.generated.resources.reader_reading_scheme_delete_confirm
import app.mori.reader.shared.generated.resources.reader_reading_scheme_delete_title
import app.mori.reader.shared.generated.resources.reader_reading_scheme_empty
import app.mori.reader.shared.generated.resources.reader_reading_scheme_font
import app.mori.reader.shared.generated.resources.reader_reading_scheme_font_system
import app.mori.reader.shared.generated.resources.reader_reading_scheme_font_system_tab
import app.mori.reader.shared.generated.resources.reader_reading_scheme_font_user_placeholder
import app.mori.reader.shared.generated.resources.reader_reading_scheme_font_user_tab
import app.mori.reader.shared.generated.resources.reader_reading_scheme_global_tab
import app.mori.reader.shared.generated.resources.reader_reading_scheme_in_use
import app.mori.reader.shared.generated.resources.reader_reading_scheme_line_height
import app.mori.reader.shared.generated.resources.reader_reading_scheme_manage
import app.mori.reader.shared.generated.resources.reader_reading_scheme_manage_title
import app.mori.reader.shared.generated.resources.reader_reading_scheme_margin
import app.mori.reader.shared.generated.resources.reader_reading_scheme_more
import app.mori.reader.shared.generated.resources.reader_reading_scheme_name_label
import app.mori.reader.shared.generated.resources.reader_reading_scheme_page_mode_continuous
import app.mori.reader.shared.generated.resources.reader_reading_scheme_page_mode_paginated
import app.mori.reader.shared.generated.resources.reader_reading_scheme_personalized_tab
import app.mori.reader.shared.generated.resources.reader_reading_scheme_rename_title
import app.mori.reader.shared.generated.resources.reader_reading_scheme_title
import app.mori.reader.shared.generated.resources.reader_settings_display_title
import app.mori.reader.shared.generated.resources.reader_settings_popup_title
import app.mori.reader.shared.generated.resources.reader_settings_typography_title
import app.mori.reader.ui.components.material.MaterialDropdownMenu
import app.mori.reader.ui.components.material.MaterialDropdownMenuOption
import app.mori.reader.ui.components.material.MaterialExpressiveSwitch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Clock

private enum class ReaderSchemePanelPage {
    Main,
    Font,
    Advanced,
    Manage,
}

private enum class ReaderSchemeDialogMode {
    CreateFromGlobal,
    CreateFromSelected,
    Duplicate,
    Rename,
}

private val SchemeHorizontalPadding = 12.dp
private val SchemeControlWidth = 230.dp
private val SchemeRowMinHeight = 42.dp
private val SchemeFontMin = 16
private val SchemeFontMax = 40
private val SchemePaddingRange = 0f..50f
private val SchemePaddingStep = 1f
private val SchemeLineHeightRange = 1.0f..2.5f
private val SchemeLineHeightStep = 0.05f
private val PopupWidthRange = 100f..700f
private val PopupHeightRange = 100f..500f
private val PopupThresholdRange = 20f..80f
private val SchemeActionButtonSpacing = 6.dp
private val SchemePillHorizontalPadding = 12.dp
private val SchemePillVerticalPadding = 6.dp
private val SchemePillIconSize = 16.dp
private val SchemePillIconSpacing = 4.dp
private val SchemeSectionSpacing = 6.dp
private val SchemeContentTopSpacing = 6.dp
private val SchemeItemGroupVerticalPadding = 6.dp
private val SchemeSectionTitleVerticalPadding = 6.dp
private val SchemeRowContentSpacing = 10.dp
private val SchemeAdjusterMinHeight = 32.dp
private val SchemeAdjusterContentSpacing = 8.dp
private val SchemeManageRowVerticalPadding = 8.dp

@Composable
internal fun ReaderReadingSchemePanel(
    settings: AppSettings,
    bookId: String,
    bookSchemeId: String?,
    bookLastSchemeId: String?,
    openAdvanced: Boolean,
    onClose: () -> Unit,
    onSwitchToGlobal: () -> Unit,
    onSwitchToScheme: (String) -> Unit,
    onCreateScheme: (ReaderPersonalizedScheme) -> Unit,
    onRenameScheme: (schemeId: String, name: String) -> Unit,
    onDeleteScheme: (schemeId: String) -> Unit,
    onUpdateGlobalSettings: (ReaderSettings) -> Unit,
    onUpdateSchemeSettings: (schemeId: String, settings: ReaderSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val schemes = settings.readerPersonalizedSchemes
    val appliedScheme = settings.findReaderScheme(bookSchemeId)
    val duplicateNameSuffix = stringResource(Res.string.reader_reading_scheme_copy_suffix)
    var selectedTab by rememberSaveable(bookId) { mutableIntStateOf(if (appliedScheme != null) 1 else 0) }
    var selectedSchemeId by rememberSaveable(bookId) { mutableStateOf(bookSchemeId ?: bookLastSchemeId) }
    var panelPage by rememberSaveable(bookId) { mutableStateOf(ReaderSchemePanelPage.Main) }
    var schemeMenuExpanded by remember { mutableStateOf(false) }
    var createMenuExpanded by remember { mutableStateOf(false) }
    var dialogMode by remember { mutableStateOf<ReaderSchemeDialogMode?>(null) }
    var dialogSchemeId by remember { mutableStateOf<String?>(null) }
    var dialogName by remember { mutableStateOf("") }
    var deleteSchemeId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(bookSchemeId, bookLastSchemeId, schemes) {
        selectedTab = if (appliedScheme != null) 1 else 0
        selectedSchemeId =
            appliedScheme?.id
                ?: selectedSchemeId?.takeIf { currentId -> schemes.any { it.id == currentId } }
                ?: bookLastSchemeId?.takeIf { candidateId -> schemes.any { it.id == candidateId } }
                ?: schemes.firstOrNull()?.id
    }

    LaunchedEffect(openAdvanced) {
        if (openAdvanced) {
            panelPage = ReaderSchemePanelPage.Advanced
        }
    }

    val selectedScheme = settings.findReaderScheme(selectedSchemeId) ?: schemes.firstOrNull()
    val activeSettings =
        if (selectedTab == 0) {
            settings.reader
        } else {
            settings.effectiveReaderSettings(selectedScheme?.id)
        }

    fun updateActiveSettings(next: ReaderSettings) {
        val activeSchemeId = selectedScheme?.id
        if (selectedTab == 1 && activeSchemeId != null) {
            onUpdateSchemeSettings(activeSchemeId, next)
        } else {
            onUpdateGlobalSettings(next)
        }
    }

    fun openCreateDialog(mode: ReaderSchemeDialogMode) {
        dialogMode = mode
        dialogSchemeId = null
        dialogName = ""
    }

    fun openRenameDialog(scheme: ReaderPersonalizedScheme) {
        dialogMode = ReaderSchemeDialogMode.Rename
        dialogSchemeId = scheme.id
        dialogName = scheme.name
    }

    fun openDuplicateDialog(scheme: ReaderPersonalizedScheme) {
        dialogMode = ReaderSchemeDialogMode.Duplicate
        dialogSchemeId = scheme.id
        dialogName =
            buildString {
                append(scheme.name)
                append(' ')
                append(duplicateNameSuffix)
            }
    }

    fun applyPersonalizedTab() {
        val target = selectedScheme ?: schemes.firstOrNull()
        if (target == null) {
            openCreateDialog(ReaderSchemeDialogMode.CreateFromGlobal)
            return
        }
        selectedSchemeId = target.id
        onSwitchToScheme(target.id)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        when (panelPage) {
            ReaderSchemePanelPage.Main -> {
                ReaderSchemeMainPage(
                    settings = settings,
                    selectedTab = selectedTab,
                    selectedScheme = selectedScheme,
                    activeSettings = activeSettings,
                    schemeMenuExpanded = schemeMenuExpanded,
                    createMenuExpanded = createMenuExpanded,
                    onClose = onClose,
                    onSelectTab = { index ->
                        selectedTab = index
                        if (index == 0) {
                            onSwitchToGlobal()
                        } else {
                            applyPersonalizedTab()
                        }
                    },
                    onSchemeMenuExpandedChange = { schemeMenuExpanded = it },
                    onCreateMenuExpandedChange = { createMenuExpanded = it },
                    onSelectScheme = { schemeId ->
                        selectedSchemeId = schemeId
                        onSwitchToScheme(schemeId)
                    },
                    onManageSchemes = { panelPage = ReaderSchemePanelPage.Manage },
                    onCreateFromGlobal = { openCreateDialog(ReaderSchemeDialogMode.CreateFromGlobal) },
                    onCreateFromSelected = { openCreateDialog(ReaderSchemeDialogMode.CreateFromSelected) },
                    onUpdateSettings = ::updateActiveSettings,
                    onOpenFontSettings = { panelPage = ReaderSchemePanelPage.Font },
                    onOpenAdvancedSettings = { panelPage = ReaderSchemePanelPage.Advanced },
                )
            }

            ReaderSchemePanelPage.Font -> {
                ReaderSchemeFontPage(
                    activeSettings = activeSettings,
                    onBack = { panelPage = ReaderSchemePanelPage.Main },
                    onClose = onClose,
                    onUpdateSettings = ::updateActiveSettings,
                )
            }

            ReaderSchemePanelPage.Advanced -> {
                ReaderSchemeAdvancedPage(
                    activeSettings = activeSettings,
                    onBack = { panelPage = ReaderSchemePanelPage.Main },
                    onClose = onClose,
                    onUpdateSettings = ::updateActiveSettings,
                )
            }

            ReaderSchemePanelPage.Manage -> {
                ReaderSchemeManagePage(
                    schemes = schemes,
                    currentSchemeId = bookSchemeId,
                    onBack = { panelPage = ReaderSchemePanelPage.Main },
                    onClose = onClose,
                    onDuplicateScheme = { openDuplicateDialog(it) },
                    onRenameScheme = { openRenameDialog(it) },
                    onDeleteScheme = { deleteSchemeId = it.id },
                )
            }
        }
    }

    val currentDialogMode = dialogMode
    if (currentDialogMode != null) {
        val title =
            when (currentDialogMode) {
                ReaderSchemeDialogMode.CreateFromGlobal,
                ReaderSchemeDialogMode.CreateFromSelected,
                ReaderSchemeDialogMode.Duplicate,
                -> stringResource(Res.string.reader_reading_scheme_create_title)

                ReaderSchemeDialogMode.Rename -> stringResource(Res.string.reader_reading_scheme_rename_title)
            }
        AlertDialog(
            onDismissRequest = { dialogMode = null },
            title = { Text(title) },
            text = {
                TextField(
                    value = dialogName,
                    onValueChange = { dialogName = it },
                    label = { Text(stringResource(Res.string.reader_reading_scheme_name_label)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val scheme =
                            when (currentDialogMode) {
                                ReaderSchemeDialogMode.CreateFromGlobal -> {
                                    ReaderPersonalizedScheme(
                                        id = generateReaderSchemeId(),
                                        name = dialogName,
                                        settings = settings.reader,
                                        createdAt = currentTimestampMillis(),
                                    )
                                }

                                ReaderSchemeDialogMode.CreateFromSelected -> {
                                    ReaderPersonalizedScheme(
                                        id = generateReaderSchemeId(),
                                        name = dialogName,
                                        settings = activeSettings,
                                        createdAt = currentTimestampMillis(),
                                    )
                                }

                                ReaderSchemeDialogMode.Duplicate -> {
                                    settings.findReaderScheme(dialogSchemeId)?.let { sourceScheme ->
                                        ReaderPersonalizedScheme(
                                            id = generateReaderSchemeId(),
                                            name = dialogName,
                                            settings = sourceScheme.settings,
                                            createdAt = currentTimestampMillis(),
                                        )
                                    }
                                }

                                ReaderSchemeDialogMode.Rename -> {
                                    null
                                }
                            }
                        when (currentDialogMode) {
                            ReaderSchemeDialogMode.CreateFromGlobal,
                            ReaderSchemeDialogMode.CreateFromSelected,
                            ReaderSchemeDialogMode.Duplicate,
                            -> {
                                scheme?.let {
                                    onCreateScheme(it)
                                    selectedSchemeId = it.id
                                    selectedTab = 1
                                    onSwitchToScheme(it.id)
                                }
                            }

                            ReaderSchemeDialogMode.Rename -> {
                                dialogSchemeId?.let { onRenameScheme(it, dialogName) }
                            }
                        }
                        dialogMode = null
                    },
                ) {
                    Text(
                        if (currentDialogMode == ReaderSchemeDialogMode.Rename) {
                            stringResource(Res.string.btn_save)
                        } else {
                            stringResource(Res.string.btn_add)
                        },
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogMode = null }) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            },
        )
    }

    val deletingScheme = settings.findReaderScheme(deleteSchemeId)
    if (deletingScheme != null) {
        AlertDialog(
            onDismissRequest = { deleteSchemeId = null },
            title = { Text(stringResource(Res.string.reader_reading_scheme_delete_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.reader_reading_scheme_delete_confirm,
                        deletingScheme.name,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteScheme(deletingScheme.id)
                        deleteSchemeId = null
                        if (selectedSchemeId == deletingScheme.id) {
                            selectedSchemeId = settings.readerPersonalizedSchemes.firstOrNull { it.id != deletingScheme.id }?.id
                        }
                        if (bookSchemeId == deletingScheme.id) {
                            selectedTab = 0
                        }
                    },
                ) {
                    Text(stringResource(Res.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteSchemeId = null }) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            },
        )
    }
}

@Composable
private fun ReaderSchemeMainPage(
    settings: AppSettings,
    selectedTab: Int,
    selectedScheme: ReaderPersonalizedScheme?,
    activeSettings: ReaderSettings,
    schemeMenuExpanded: Boolean,
    createMenuExpanded: Boolean,
    onClose: () -> Unit,
    onSelectTab: (Int) -> Unit,
    onSchemeMenuExpandedChange: (Boolean) -> Unit,
    onCreateMenuExpandedChange: (Boolean) -> Unit,
    onSelectScheme: (String) -> Unit,
    onManageSchemes: () -> Unit,
    onCreateFromGlobal: () -> Unit,
    onCreateFromSelected: () -> Unit,
    onUpdateSettings: (ReaderSettings) -> Unit,
    onOpenFontSettings: () -> Unit,
    onOpenAdvancedSettings: () -> Unit,
) {
    var pageModeMenuExpanded by remember { mutableStateOf(false) }
    var writingDirectionMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        ReaderSchemePanelHeader(
            title = stringResource(Res.string.reader_reading_scheme_title),
            onClose = onClose,
        )

        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(top = SchemeSectionSpacing),
            tabs = {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onSelectTab(0) },
                    text = { Text(stringResource(Res.string.reader_reading_scheme_global_tab)) },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onSelectTab(1) },
                    text = { Text(stringResource(Res.string.reader_reading_scheme_personalized_tab)) },
                )
            },
        )

        if (selectedTab == 1) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = SchemeContentTopSpacing)
                        .padding(horizontal = 4.dp),
            ) {
                Box {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { onSchemeMenuExpandedChange(!schemeMenuExpanded) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = selectedScheme?.name ?: stringResource(Res.string.reader_reading_scheme_empty),
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                            )
                        }
                        IconButton(onClick = { onCreateMenuExpandedChange(!createMenuExpanded) }) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(Res.string.btn_add),
                            )
                        }
                    }

                    MaterialDropdownMenu(
                        expanded = schemeMenuExpanded,
                        onDismissRequest = { onSchemeMenuExpandedChange(false) },
                        options =
                            settings.readerPersonalizedSchemes.map { scheme ->
                                MaterialDropdownMenuOption(
                                    label = scheme.name,
                                    selected = selectedScheme?.id == scheme.id,
                                    onSelected = { onSelectScheme(scheme.id) },
                                )
                            } +
                                MaterialDropdownMenuOption(
                                    label = stringResource(Res.string.reader_reading_scheme_manage),
                                    dividerBefore = true,
                                    onSelected = onManageSchemes,
                                ),
                    )

                    MaterialDropdownMenu(
                        expanded = createMenuExpanded,
                        onDismissRequest = { onCreateMenuExpandedChange(false) },
                        options =
                            listOf(
                                MaterialDropdownMenuOption(
                                    label = stringResource(Res.string.reader_reading_scheme_create_based_on_global),
                                    onSelected = onCreateFromGlobal,
                                ),
                                MaterialDropdownMenuOption(
                                    label = stringResource(Res.string.reader_reading_scheme_create_based_on_selected),
                                    onSelected = onCreateFromSelected,
                                ),
                            ),
                    )
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = SchemeSectionSpacing, bottom = 2.dp),
        ) {
            SchemeFontSizeAndFontRow(
                title = stringResource(Res.string.appearance_font_size),
                fontLabel = activeSettings.fontFamily ?: stringResource(Res.string.reader_reading_scheme_font_system),
                onDecrease = {
                    onUpdateSettings(activeSettings.copy(fontSize = (activeSettings.fontSize - 1).coerceAtLeast(SchemeFontMin)))
                },
                onIncrease = {
                    onUpdateSettings(activeSettings.copy(fontSize = (activeSettings.fontSize + 1).coerceAtMost(SchemeFontMax)))
                },
                decreaseEnabled = activeSettings.fontSize > SchemeFontMin,
                increaseEnabled = activeSettings.fontSize < SchemeFontMax,
                valueLabel = activeSettings.fontSize.toString(),
                onFontClick = onOpenFontSettings,
            )
            HorizontalDivider()
            SchemeValueControlRow(
                title = stringResource(Res.string.reader_reading_scheme_margin),
                valueLabel = activeSettings.horizontalPadding.toString(),
                onDecrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            horizontalPadding =
                                adjustStepValue(
                                    value = activeSettings.horizontalPadding.toFloat(),
                                    step = SchemePaddingStep,
                                    range = SchemePaddingRange,
                                    increase = false,
                                ).roundToInt(),
                        ),
                    )
                },
                onIncrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            horizontalPadding =
                                adjustStepValue(
                                    value = activeSettings.horizontalPadding.toFloat(),
                                    step = SchemePaddingStep,
                                    range = SchemePaddingRange,
                                    increase = true,
                                ).roundToInt(),
                        ),
                    )
                },
                decreaseEnabled = activeSettings.horizontalPadding > SchemePaddingRange.start.roundToInt(),
                increaseEnabled = activeSettings.horizontalPadding < SchemePaddingRange.endInclusive.roundToInt(),
            )
            HorizontalDivider()
            SchemeValueControlRow(
                title = stringResource(Res.string.reader_reading_scheme_line_height),
                valueLabel = formatLineHeight(activeSettings.lineHeight),
                onDecrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            lineHeight =
                                adjustStepValue(
                                    value = activeSettings.lineHeight.toFloat(),
                                    step = SchemeLineHeightStep,
                                    range = SchemeLineHeightRange,
                                    increase = false,
                                ).toDouble(),
                        ),
                    )
                },
                onIncrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            lineHeight =
                                adjustStepValue(
                                    value = activeSettings.lineHeight.toFloat(),
                                    step = SchemeLineHeightStep,
                                    range = SchemeLineHeightRange,
                                    increase = true,
                                ).toDouble(),
                        ),
                    )
                },
                decreaseEnabled = activeSettings.lineHeight > SchemeLineHeightRange.start,
                increaseEnabled = activeSettings.lineHeight < SchemeLineHeightRange.endInclusive,
            )
            BoxWithConstraints(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SchemeHorizontalPadding, vertical = SchemeItemGroupVerticalPadding),
            ) {
                val labels =
                    listOf(
                        if (activeSettings.continuousMode) {
                            stringResource(Res.string.reader_reading_scheme_page_mode_continuous)
                        } else {
                            stringResource(Res.string.reader_reading_scheme_page_mode_paginated)
                        },
                        if (activeSettings.verticalWriting) {
                            stringResource(Res.string.appearance_writing_vertical)
                        } else {
                            stringResource(Res.string.appearance_writing_horizontal)
                        },
                        stringResource(Res.string.reader_reading_scheme_advanced_button),
                    )
                val buttonTextStyle = schemePillButtonTextStyle()
                val density = LocalDensity.current
                val textMeasurer = rememberTextMeasurer()
                val threeColumnTextWidthPx =
                    with(density) {
                        val buttonWidth = ((maxWidth - SchemeActionButtonSpacing * 2) / 3).coerceAtLeast(0.dp)
                        (
                            buttonWidth -
                                SchemePillHorizontalPadding * 2 -
                                SchemePillIconSize -
                                SchemePillIconSpacing
                        ).coerceAtLeast(0.dp).roundToPx()
                    }
                val twoColumnTextWidthPx =
                    with(density) {
                        val buttonWidth = ((maxWidth - SchemeActionButtonSpacing) / 2).coerceAtLeast(0.dp)
                        (
                            buttonWidth -
                                SchemePillHorizontalPadding * 2 -
                                SchemePillIconSize -
                                SchemePillIconSpacing
                        ).coerceAtLeast(0.dp).roundToPx()
                    }

                fun fitsSingleLine(
                    label: String,
                    maxWidthPx: Int,
                ): Boolean =
                    !textMeasurer
                        .measure(
                            text = label,
                            style = buttonTextStyle,
                            maxLines = 1,
                            softWrap = false,
                            constraints = Constraints(maxWidth = maxWidthPx),
                        ).didOverflowWidth

                val shouldUseThreeColumns = labels.all { label -> fitsSingleLine(label, threeColumnTextWidthPx) }
                val shouldUseTwoPlusOne =
                    !shouldUseThreeColumns &&
                        labels.take(2).all { label -> fitsSingleLine(label, twoColumnTextWidthPx) }

                if (shouldUseThreeColumns) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SchemeActionButtonSpacing),
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SchemePillButton(
                                label = labels[0],
                                onClick = { pageModeMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = Icons.Rounded.ExpandMore,
                            )
                            MaterialDropdownMenu(
                                expanded = pageModeMenuExpanded,
                                onDismissRequest = { pageModeMenuExpanded = false },
                                options =
                                    listOf(
                                        MaterialDropdownMenuOption(
                                            label = stringResource(Res.string.reader_reading_scheme_page_mode_paginated),
                                            selected = !activeSettings.continuousMode,
                                            onSelected = {
                                                onUpdateSettings(activeSettings.copy(continuousMode = false))
                                            },
                                        ),
                                        MaterialDropdownMenuOption(
                                            label = stringResource(Res.string.reader_reading_scheme_page_mode_continuous),
                                            selected = activeSettings.continuousMode,
                                            onSelected = {
                                                onUpdateSettings(activeSettings.copy(continuousMode = true))
                                            },
                                        ),
                                    ),
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            SchemePillButton(
                                label = labels[1],
                                onClick = { writingDirectionMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = Icons.Rounded.ExpandMore,
                            )
                            MaterialDropdownMenu(
                                expanded = writingDirectionMenuExpanded,
                                onDismissRequest = { writingDirectionMenuExpanded = false },
                                options =
                                    listOf(
                                        MaterialDropdownMenuOption(
                                            label = stringResource(Res.string.appearance_writing_vertical),
                                            selected = activeSettings.verticalWriting,
                                            onSelected = {
                                                onUpdateSettings(activeSettings.copy(verticalWriting = true))
                                            },
                                        ),
                                        MaterialDropdownMenuOption(
                                            label = stringResource(Res.string.appearance_writing_horizontal),
                                            selected = !activeSettings.verticalWriting,
                                            onSelected = {
                                                onUpdateSettings(activeSettings.copy(verticalWriting = false))
                                            },
                                        ),
                                    ),
                            )
                        }
                        SchemePillButton(
                            label = labels[2],
                            onClick = onOpenAdvancedSettings,
                            modifier = Modifier.weight(1f),
                            trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        )
                    }
                } else if (shouldUseTwoPlusOne) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(SchemeActionButtonSpacing),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SchemeActionButtonSpacing),
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                SchemePillButton(
                                    label = labels[0],
                                    onClick = { pageModeMenuExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = Icons.Rounded.ExpandMore,
                                )
                                MaterialDropdownMenu(
                                    expanded = pageModeMenuExpanded,
                                    onDismissRequest = { pageModeMenuExpanded = false },
                                    options =
                                        listOf(
                                            MaterialDropdownMenuOption(
                                                label = stringResource(Res.string.reader_reading_scheme_page_mode_paginated),
                                                selected = !activeSettings.continuousMode,
                                                onSelected = {
                                                    onUpdateSettings(activeSettings.copy(continuousMode = false))
                                                },
                                            ),
                                            MaterialDropdownMenuOption(
                                                label = stringResource(Res.string.reader_reading_scheme_page_mode_continuous),
                                                selected = activeSettings.continuousMode,
                                                onSelected = {
                                                    onUpdateSettings(activeSettings.copy(continuousMode = true))
                                                },
                                            ),
                                        ),
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                SchemePillButton(
                                    label = labels[1],
                                    onClick = { writingDirectionMenuExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = Icons.Rounded.ExpandMore,
                                )
                                MaterialDropdownMenu(
                                    expanded = writingDirectionMenuExpanded,
                                    onDismissRequest = { writingDirectionMenuExpanded = false },
                                    options =
                                        listOf(
                                            MaterialDropdownMenuOption(
                                                label = stringResource(Res.string.appearance_writing_vertical),
                                                selected = activeSettings.verticalWriting,
                                                onSelected = {
                                                    onUpdateSettings(activeSettings.copy(verticalWriting = true))
                                                },
                                            ),
                                            MaterialDropdownMenuOption(
                                                label = stringResource(Res.string.appearance_writing_horizontal),
                                                selected = !activeSettings.verticalWriting,
                                                onSelected = {
                                                    onUpdateSettings(activeSettings.copy(verticalWriting = false))
                                                },
                                            ),
                                        ),
                                )
                            }
                        }
                        SchemePillButton(
                            label = labels[2],
                            onClick = onOpenAdvancedSettings,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(SchemeActionButtonSpacing),
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            SchemePillButton(
                                label = labels[0],
                                onClick = { pageModeMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = Icons.Rounded.ExpandMore,
                            )
                            MaterialDropdownMenu(
                                expanded = pageModeMenuExpanded,
                                onDismissRequest = { pageModeMenuExpanded = false },
                                options =
                                    listOf(
                                        MaterialDropdownMenuOption(
                                            label = stringResource(Res.string.reader_reading_scheme_page_mode_paginated),
                                            selected = !activeSettings.continuousMode,
                                            onSelected = {
                                                onUpdateSettings(activeSettings.copy(continuousMode = false))
                                            },
                                        ),
                                        MaterialDropdownMenuOption(
                                            label = stringResource(Res.string.reader_reading_scheme_page_mode_continuous),
                                            selected = activeSettings.continuousMode,
                                            onSelected = {
                                                onUpdateSettings(activeSettings.copy(continuousMode = true))
                                            },
                                        ),
                                    ),
                            )
                        }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            SchemePillButton(
                                label = labels[1],
                                onClick = { writingDirectionMenuExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = Icons.Rounded.ExpandMore,
                            )
                            MaterialDropdownMenu(
                                expanded = writingDirectionMenuExpanded,
                                onDismissRequest = { writingDirectionMenuExpanded = false },
                                options =
                                    listOf(
                                        MaterialDropdownMenuOption(
                                            label = stringResource(Res.string.appearance_writing_vertical),
                                            selected = activeSettings.verticalWriting,
                                            onSelected = {
                                                onUpdateSettings(activeSettings.copy(verticalWriting = true))
                                            },
                                        ),
                                        MaterialDropdownMenuOption(
                                            label = stringResource(Res.string.appearance_writing_horizontal),
                                            selected = !activeSettings.verticalWriting,
                                            onSelected = {
                                                onUpdateSettings(activeSettings.copy(verticalWriting = false))
                                            },
                                        ),
                                    ),
                            )
                        }
                        SchemePillButton(
                            label = labels[2],
                            onClick = onOpenAdvancedSettings,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun schemePillButtonTextStyle(): TextStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)

@Composable
private fun SchemePillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 36.dp),
        shape = RoundedCornerShape(999.dp),
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(horizontal = SchemePillHorizontalPadding, vertical = SchemePillVerticalPadding),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                maxLines = 2,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                style = schemePillButtonTextStyle(),
            )
            Spacer(modifier = Modifier.size(SchemePillIconSpacing))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(SchemePillIconSize),
            )
        }
    }
}

@Composable
private fun ReaderSchemeFontPage(
    activeSettings: ReaderSettings,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onUpdateSettings: (ReaderSettings) -> Unit,
) {
    val systemFonts = rememberReaderSystemFonts()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        ReaderSchemePanelHeader(
            title = stringResource(Res.string.reader_reading_scheme_font),
            onBack = onBack,
            onClose = onClose,
        )

        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(top = SchemeSectionSpacing),
            tabs = {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(Res.string.reader_reading_scheme_font_system_tab)) },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(Res.string.reader_reading_scheme_font_user_tab)) },
                )
            },
        )

        when (selectedTab) {
            0 -> {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = SchemeContentTopSpacing)
                            .verticalScroll(rememberScrollState()),
                ) {
                    ReaderFontOptionRow(
                        label = stringResource(Res.string.reader_reading_scheme_font_system),
                        selected = activeSettings.fontFamily == null,
                        onClick = { onUpdateSettings(activeSettings.copy(fontFamily = null)) },
                    )
                    systemFonts.forEach { font ->
                        HorizontalDivider()
                        ReaderFontOptionRow(
                            label = font.label,
                            selected = activeSettings.fontFamily == font.family,
                            onClick = { onUpdateSettings(activeSettings.copy(fontFamily = font.family)) },
                        )
                    }
                }
            }

            else -> {
                Text(
                    text = stringResource(Res.string.reader_reading_scheme_font_user_placeholder),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = SchemeHorizontalPadding, vertical = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ReaderFontOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = SchemeRowMinHeight)
                .clickable(onClick = onClick)
                .padding(horizontal = SchemeHorizontalPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(SchemeRowContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Done,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ReaderSchemeAdvancedPage(
    activeSettings: ReaderSettings,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onUpdateSettings: (ReaderSettings) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ReaderSchemePanelHeader(
            title = stringResource(Res.string.reader_reading_scheme_more),
            onBack = onBack,
            onClose = onClose,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = SchemeContentTopSpacing)
                    .verticalScroll(rememberScrollState()),
        ) {
            SchemeSectionTitle(stringResource(Res.string.reader_settings_display_title))
            SchemeToggleRow(
                title = stringResource(Res.string.appearance_fullscreen_title),
                enabled = activeSettings.fullscreen,
                onSelected = {
                    onUpdateSettings(activeSettings.copy(fullscreen = it))
                },
            )
            HorizontalDivider()
            SchemeToggleRow(
                title = stringResource(Res.string.appearance_action_bar_pinned_title),
                enabled = activeSettings.actionBarPinned,
                onSelected = {
                    onUpdateSettings(activeSettings.copy(actionBarPinned = it))
                },
            )
            HorizontalDivider()
            SchemeToggleRow(
                title = stringResource(Res.string.appearance_show_reading_info_title),
                enabled = activeSettings.showReadingInfo,
                onSelected = {
                    onUpdateSettings(activeSettings.copy(showReadingInfo = it))
                },
            )
            HorizontalDivider()
            SchemeToggleRow(
                title = stringResource(Res.string.appearance_hide_furigana_title),
                enabled = activeSettings.hideFurigana,
                onSelected = {
                    onUpdateSettings(activeSettings.copy(hideFurigana = it))
                },
            )

            SchemeSectionTitle(
                title = stringResource(Res.string.reader_settings_typography_title),
                modifier = Modifier.padding(top = SchemeSectionSpacing),
            )
            SchemeValueControlRow(
                title = stringResource(Res.string.appearance_character_spacing),
                valueLabel = activeSettings.characterSpacing.roundToInt().toString(),
                onDecrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            characterSpacing = (activeSettings.characterSpacing - 1.0).coerceAtLeast(-10.0),
                        ),
                    )
                },
                onIncrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            characterSpacing = (activeSettings.characterSpacing + 1.0).coerceAtMost(10.0),
                        ),
                    )
                },
                decreaseEnabled = activeSettings.characterSpacing > -10.0,
                increaseEnabled = activeSettings.characterSpacing < 10.0,
            )
            HorizontalDivider()
            SchemeValueControlRow(
                title = stringResource(Res.string.appearance_vertical_margin),
                valueLabel = activeSettings.verticalPadding.toString(),
                onDecrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            verticalPadding =
                                adjustStepValue(
                                    value = activeSettings.verticalPadding.toFloat(),
                                    step = SchemePaddingStep,
                                    range = SchemePaddingRange,
                                    increase = false,
                                ).roundToInt(),
                        ),
                    )
                },
                onIncrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            verticalPadding =
                                adjustStepValue(
                                    value = activeSettings.verticalPadding.toFloat(),
                                    step = SchemePaddingStep,
                                    range = SchemePaddingRange,
                                    increase = true,
                                ).roundToInt(),
                        ),
                    )
                },
                decreaseEnabled = activeSettings.verticalPadding > SchemePaddingRange.start.roundToInt(),
                increaseEnabled = activeSettings.verticalPadding < SchemePaddingRange.endInclusive.roundToInt(),
            )
            HorizontalDivider()
            SchemeToggleRow(
                title = stringResource(Res.string.appearance_avoid_page_break_title),
                enabled = activeSettings.avoidPageBreak,
                onSelected = {
                    onUpdateSettings(activeSettings.copy(avoidPageBreak = it))
                },
            )
            HorizontalDivider()
            SchemeToggleRow(
                title = stringResource(Res.string.appearance_justify_title),
                enabled = activeSettings.justifyText,
                onSelected = {
                    onUpdateSettings(activeSettings.copy(justifyText = it))
                },
            )

            SchemeSectionTitle(
                title = stringResource(Res.string.reader_settings_popup_title),
                modifier = Modifier.padding(top = SchemeSectionSpacing),
            )
            SchemeToggleRow(
                title = stringResource(Res.string.appearance_popup_full_width_title),
                enabled = activeSettings.popupFullWidth,
                onSelected = {
                    onUpdateSettings(activeSettings.copy(popupFullWidth = it))
                },
            )
            HorizontalDivider()
            SchemeToggleRow(
                title = stringResource(Res.string.appearance_popup_swipe_dismiss_title),
                enabled = activeSettings.popupSwipeToDismiss,
                onSelected = {
                    onUpdateSettings(activeSettings.copy(popupSwipeToDismiss = it))
                },
            )
            HorizontalDivider()
            SchemeValueControlRow(
                title = stringResource(Res.string.appearance_popup_width),
                valueLabel = activeSettings.popupWidth.toString(),
                onDecrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            popupWidth =
                                adjustStepValue(
                                    value = activeSettings.popupWidth.toFloat(),
                                    step = 10f,
                                    range = PopupWidthRange,
                                    increase = false,
                                ).roundToInt(),
                        ),
                    )
                },
                onIncrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            popupWidth =
                                adjustStepValue(
                                    value = activeSettings.popupWidth.toFloat(),
                                    step = 10f,
                                    range = PopupWidthRange,
                                    increase = true,
                                ).roundToInt(),
                        ),
                    )
                },
                decreaseEnabled = activeSettings.popupWidth > PopupWidthRange.start.roundToInt(),
                increaseEnabled = activeSettings.popupWidth < PopupWidthRange.endInclusive.roundToInt(),
            )
            HorizontalDivider()
            SchemeValueControlRow(
                title = stringResource(Res.string.appearance_popup_height),
                valueLabel = activeSettings.popupHeight.toString(),
                onDecrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            popupHeight =
                                adjustStepValue(
                                    value = activeSettings.popupHeight.toFloat(),
                                    step = 10f,
                                    range = PopupHeightRange,
                                    increase = false,
                                ).roundToInt(),
                        ),
                    )
                },
                onIncrease = {
                    onUpdateSettings(
                        activeSettings.copy(
                            popupHeight =
                                adjustStepValue(
                                    value = activeSettings.popupHeight.toFloat(),
                                    step = 10f,
                                    range = PopupHeightRange,
                                    increase = true,
                                ).roundToInt(),
                        ),
                    )
                },
                decreaseEnabled = activeSettings.popupHeight > PopupHeightRange.start.roundToInt(),
                increaseEnabled = activeSettings.popupHeight < PopupHeightRange.endInclusive.roundToInt(),
            )
            if (activeSettings.popupSwipeToDismiss) {
                HorizontalDivider()
                SchemeValueControlRow(
                    title = stringResource(Res.string.appearance_popup_swipe_threshold),
                    valueLabel = activeSettings.popupSwipeThreshold.toString(),
                    onDecrease = {
                        onUpdateSettings(
                            activeSettings.copy(
                                popupSwipeThreshold =
                                    adjustStepValue(
                                        value = activeSettings.popupSwipeThreshold.toFloat(),
                                        step = 5f,
                                        range = PopupThresholdRange,
                                        increase = false,
                                    ).roundToInt(),
                            ),
                        )
                    },
                    onIncrease = {
                        onUpdateSettings(
                            activeSettings.copy(
                                popupSwipeThreshold =
                                    adjustStepValue(
                                        value = activeSettings.popupSwipeThreshold.toFloat(),
                                        step = 5f,
                                        range = PopupThresholdRange,
                                        increase = true,
                                    ).roundToInt(),
                            ),
                        )
                    },
                    decreaseEnabled = activeSettings.popupSwipeThreshold > PopupThresholdRange.start.roundToInt(),
                    increaseEnabled = activeSettings.popupSwipeThreshold < PopupThresholdRange.endInclusive.roundToInt(),
                )
            }
        }
    }
}

@Composable
private fun ReaderSchemeManagePage(
    schemes: List<ReaderPersonalizedScheme>,
    currentSchemeId: String?,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onDuplicateScheme: (ReaderPersonalizedScheme) -> Unit,
    onRenameScheme: (ReaderPersonalizedScheme) -> Unit,
    onDeleteScheme: (ReaderPersonalizedScheme) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ReaderSchemePanelHeader(
            title = stringResource(Res.string.reader_reading_scheme_manage_title),
            onBack = onBack,
            onClose = onClose,
        )

        if (schemes.isEmpty()) {
            Text(
                text = stringResource(Res.string.reader_reading_scheme_empty),
                modifier = Modifier.padding(horizontal = SchemeHorizontalPadding, vertical = 20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = SchemeSectionSpacing)
                    .verticalScroll(rememberScrollState()),
        ) {
            schemes.forEachIndexed { index, scheme ->
                if (index > 0) {
                    HorizontalDivider()
                }
                val isCurrentScheme = scheme.id == currentSchemeId
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                if (isCurrentScheme) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                } else {
                                    Color.Transparent
                                },
                            ).padding(horizontal = SchemeHorizontalPadding, vertical = SchemeManageRowVerticalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SchemeRowContentSpacing),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scheme.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (isCurrentScheme) {
                        Icon(
                            imageVector = Icons.Rounded.Done,
                            contentDescription = stringResource(Res.string.reader_reading_scheme_in_use),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = { onDuplicateScheme(scheme) }) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(Res.string.reader_reading_scheme_manage),
                        )
                    }
                    IconButton(onClick = { onRenameScheme(scheme) }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(Res.string.cd_rename),
                        )
                    }
                    IconButton(onClick = { onDeleteScheme(scheme) }) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(Res.string.cd_delete),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderSchemePanelHeader(
    title: String,
    onClose: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(Res.string.cd_back),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(Res.string.btn_close),
            )
        }
    }
}

@Composable
private fun SchemeSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.padding(horizontal = SchemeHorizontalPadding, vertical = SchemeSectionTitleVerticalPadding),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SchemeSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = SchemeRowMinHeight)
                .padding(horizontal = SchemeHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(SchemeRowContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                modifier = Modifier.width(SchemeControlWidth),
                contentAlignment = Alignment.CenterEnd,
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SchemeValueControlRow(
    title: String,
    valueLabel: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseEnabled: Boolean,
    increaseEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    SchemeSettingRow(
        title = title,
        modifier = modifier,
    ) {
        SchemeAdjusterControl(
            valueLabel = valueLabel,
            onDecrease = onDecrease,
            onIncrease = onIncrease,
            decreaseEnabled = decreaseEnabled,
            increaseEnabled = increaseEnabled,
        )
    }
}

@Composable
private fun SchemeFontSizeAndFontRow(
    title: String,
    fontLabel: String,
    valueLabel: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseEnabled: Boolean,
    increaseEnabled: Boolean,
    onFontClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = SchemeRowMinHeight)
                .padding(horizontal = SchemeHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(SchemeRowContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.spacedBy(SchemeAdjusterContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SchemeSelectionPillButton(
                label = fontLabel,
                onClick = onFontClick,
                modifier = Modifier.weight(1f),
                trailingIcon = Icons.Rounded.ChevronRight,
            )
            SchemeInlineAdjusterControl(
                valueLabel = valueLabel,
                onDecrease = onDecrease,
                onIncrease = onIncrease,
                decreaseEnabled = decreaseEnabled,
                increaseEnabled = increaseEnabled,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SchemeToggleRow(
    title: String,
    enabled: Boolean,
    onSelected: (Boolean) -> Unit,
) {
    SchemeSettingRow(
        title = title,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MaterialExpressiveSwitch(
                checked = enabled,
                onCheckedChange = onSelected,
            )
        }
    }
}

@Composable
private fun SchemeInlineAdjusterControl(
    valueLabel: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseEnabled: Boolean,
    increaseEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .defaultMinSize(minHeight = SchemeAdjusterMinHeight)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(18.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SchemeAdjusterButton(
            imageVector = Icons.Rounded.Remove,
            onClick = onDecrease,
            enabled = decreaseEnabled,
        )
        Text(
            text = valueLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        SchemeAdjusterButton(
            imageVector = Icons.Rounded.Add,
            onClick = onIncrease,
            enabled = increaseEnabled,
        )
    }
}

@Composable
private fun SchemeAdjusterControl(
    valueLabel: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseEnabled: Boolean,
    increaseEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().defaultMinSize(minHeight = SchemeAdjusterMinHeight),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SchemeAdjusterContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = valueLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Row(
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(18.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SchemeAdjusterButton(
                    imageVector = Icons.Rounded.Remove,
                    onClick = onDecrease,
                    enabled = decreaseEnabled,
                )
                Box(
                    modifier =
                        Modifier
                            .height(24.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                )
                SchemeAdjusterButton(
                    imageVector = Icons.Rounded.Add,
                    onClick = onIncrease,
                    enabled = increaseEnabled,
                )
            }
        }
    }
}

@Composable
private fun SchemeSelectionPillButton(
    label: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier =
            modifier
                .heightIn(min = 36.dp)
                .widthIn(min = 0.dp),
        shape = RoundedCornerShape(999.dp),
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(horizontal = SchemePillHorizontalPadding, vertical = SchemePillVerticalPadding),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = schemePillButtonTextStyle(),
            )
            Spacer(modifier = Modifier.size(SchemePillIconSpacing))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier.size(SchemePillIconSize),
            )
        }
    }
}

@Composable
private fun SchemeAdjusterButton(
    imageVector: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(width = 34.dp, height = 30.dp),
        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            ),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SchemeNavigationRow(
    title: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SchemeSettingRow(
        title = title,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            value?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun adjustStepValue(
    value: Float,
    step: Float,
    range: ClosedFloatingPointRange<Float>,
    increase: Boolean,
): Float {
    val next = if (increase) value + step else value - step
    return ((next / step).roundToInt() * step).coerceIn(range.start, range.endInclusive)
}

private fun formatLineHeight(value: Double): String = ((value * 100).roundToInt() / 100.0).toString()

private fun currentTimestampMillis(): Long = Clock.System.now().toEpochMilliseconds()

private fun generateReaderSchemeId(): String = "reader-scheme-${currentTimestampMillis()}-${Random.nextInt(1000, 9999)}"
