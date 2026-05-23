package app.mori.reader.ui.pages.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.mori.reader.data.book.ReaderSavedBookmark
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.data.settings.ReaderPersonalizedScheme
import app.mori.reader.data.settings.ReaderSettings
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_bookmarks
import app.mori.reader.shared.generated.resources.cd_close
import app.mori.reader.shared.generated.resources.cd_open_audiobook
import app.mori.reader.shared.generated.resources.cd_reading_scheme
import app.mori.reader.shared.generated.resources.cd_table_of_contents
import app.mori.reader.ui.components.reader.ReaderAccessoryPanel
import app.mori.reader.ui.theme.MaterialThemeConfig
import app.mori.reader.ui.theme.materialEInkColorScheme
import app.mori.reader.ui.theme.rememberMaterialThemeConfig
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

private enum class ReaderAccessoryPanelKind {
    ReadingScheme,
    Bookmarks,
}

internal val ReaderSideRailWidth = 72.dp
internal val ReaderSidePanelWidth = 420.dp
private val ReaderPanelExpandEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private val ReaderPanelCollapseEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)

@Composable
internal fun ReaderStatus(
    text: String?,
    isDark: Boolean,
    materialEInkMode: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
) {
    ReaderMaterialTheme(
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text.orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun ReaderTopChrome(
    title: String,
    chapter: String,
    progress: String?,
    isDark: Boolean,
    materialEInkMode: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    compactTopPadding: Dp = 0.dp,
    compactStartPadding: Dp = 28.dp,
    compactEndPadding: Dp = 28.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (title.isBlank() && chapter.isBlank() && progress.isNullOrBlank()) return
    ReaderMaterialTheme(
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        Box(modifier = modifier.fillMaxWidth()) {
            ReaderCompactTopInfo(
                title = title.ifBlank { chapter },
                progress = progress,
                topPadding = compactTopPadding,
                startPadding = compactStartPadding,
                endPadding = compactEndPadding,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun ReaderCompactTopInfo(
    title: String,
    progress: String?,
    topPadding: Dp,
    startPadding: Dp,
    endPadding: Dp,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = topPadding)
                .height(50.dp)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                ).padding(start = startPadding, end = endPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        progress?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun ReaderBottomChrome(
    visible: Boolean,
    settings: AppSettings,
    bookId: String,
    bookSchemeId: String?,
    bookLastSchemeId: String?,
    isDark: Boolean,
    materialEInkMode: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    bottomPadding: Dp,
    onMenu: () -> Unit,
    onSasayaki: () -> Unit,
    readingSchemePanelVisible: Boolean,
    onReadingScheme: () -> Unit,
    bookmarkPanelVisible: Boolean,
    bookmarks: List<ReaderSavedBookmark>,
    currentPositionBookmarked: Boolean,
    chapterTitleForBookmark: (Int) -> String,
    onBookmark: () -> Unit,
    onToggleCurrentBookmark: () -> Unit,
    onSelectBookmark: (ReaderSavedBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onSwitchToGlobalScheme: () -> Unit,
    onSwitchToReaderScheme: (String) -> Unit,
    onCreateReaderScheme: (ReaderPersonalizedScheme) -> Unit,
    onRenameReaderScheme: (schemeId: String, name: String) -> Unit,
    onDeleteReaderScheme: (schemeId: String) -> Unit,
    onUpdateGlobalReaderSettings: (ReaderSettings) -> Unit,
    onUpdateReaderSchemeSettings: (schemeId: String, settings: ReaderSettings) -> Unit,
    openAdvanced: Boolean,
    modifier: Modifier = Modifier,
) {
    val bottomBarHeight = 48.dp + bottomPadding
    val minimumReaderPeekHeight = 220.dp
    val activeAccessoryPanel =
        when {
            readingSchemePanelVisible -> ReaderAccessoryPanelKind.ReadingScheme
            bookmarkPanelVisible -> ReaderAccessoryPanelKind.Bookmarks
            else -> null
        }
    var lastAccessoryPanel by remember { mutableStateOf<ReaderAccessoryPanelKind?>(null) }
    if (activeAccessoryPanel != null) {
        lastAccessoryPanel = activeAccessoryPanel
    }

    ReaderMaterialTheme(
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        AnimatedVisibility(
            visible = visible,
            modifier = modifier.fillMaxWidth(),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            BoxWithConstraints(
                modifier =
                    Modifier
                        .fillMaxWidth(),
            ) {
                val maxPanelHeight = (maxHeight - bottomBarHeight - minimumReaderPeekHeight).coerceAtLeast(0.dp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    ReaderAccessoryPanel(
                        visible = activeAccessoryPanel != null,
                        maxHeight = maxPanelHeight,
                    ) {
                        when (activeAccessoryPanel ?: lastAccessoryPanel) {
                            ReaderAccessoryPanelKind.ReadingScheme -> {
                                ReaderReadingSchemePanel(
                                    settings = settings,
                                    bookId = bookId,
                                    bookSchemeId = bookSchemeId,
                                    bookLastSchemeId = bookLastSchemeId,
                                    openAdvanced = openAdvanced,
                                    onClose = onReadingScheme,
                                    onSwitchToGlobal = onSwitchToGlobalScheme,
                                    onSwitchToScheme = onSwitchToReaderScheme,
                                    onCreateScheme = onCreateReaderScheme,
                                    onRenameScheme = onRenameReaderScheme,
                                    onDeleteScheme = onDeleteReaderScheme,
                                    onUpdateGlobalSettings = onUpdateGlobalReaderSettings,
                                    onUpdateSchemeSettings = onUpdateReaderSchemeSettings,
                                )
                            }

                            ReaderAccessoryPanelKind.Bookmarks -> {
                                ReaderBookmarksPanel(
                                    bookmarks = bookmarks,
                                    chapterTitleFor = chapterTitleForBookmark,
                                    currentPositionBookmarked = currentPositionBookmarked,
                                    onToggleCurrentBookmark = onToggleCurrentBookmark,
                                    onSelectBookmark = onSelectBookmark,
                                    onDeleteBookmark = onDeleteBookmark,
                                    onClose = onBookmark,
                                )
                            }

                            null -> {
                                Unit
                            }
                        }
                    }
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 3.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp + bottomPadding),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .align(Alignment.TopCenter)
                                        .padding(horizontal = 26.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = onMenu,
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                        contentDescription = stringResource(Res.string.cd_table_of_contents),
                                    )
                                }
                                IconButton(
                                    onClick = onSasayaki,
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = stringResource(Res.string.cd_open_audiobook),
                                    )
                                }
                                if (readingSchemePanelVisible) {
                                    FilledTonalIconButton(
                                        onClick = onReadingScheme,
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Tune,
                                            contentDescription = stringResource(Res.string.cd_reading_scheme),
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = onReadingScheme,
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Tune,
                                            contentDescription = stringResource(Res.string.cd_reading_scheme),
                                        )
                                    }
                                }
                                if (bookmarkPanelVisible) {
                                    FilledTonalIconButton(
                                        onClick = onBookmark,
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Bookmark,
                                            contentDescription = stringResource(Res.string.cd_bookmarks),
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = onBookmark,
                                        modifier = Modifier.size(40.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Bookmark,
                                            contentDescription = stringResource(Res.string.cd_bookmarks),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ReaderSideChrome(
    visible: Boolean,
    settings: AppSettings,
    bookId: String,
    bookSchemeId: String?,
    bookLastSchemeId: String?,
    isDark: Boolean,
    materialEInkMode: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    topPadding: Dp,
    bottomPadding: Dp,
    onShow: () -> Unit,
    onHide: () -> Unit,
    onMenu: () -> Unit,
    onSasayaki: () -> Unit,
    readingSchemePanelVisible: Boolean,
    onReadingScheme: () -> Unit,
    bookmarkPanelVisible: Boolean,
    bookmarks: List<ReaderSavedBookmark>,
    currentPositionBookmarked: Boolean,
    chapterTitleForBookmark: (Int) -> String,
    onBookmark: () -> Unit,
    onToggleCurrentBookmark: () -> Unit,
    onSelectBookmark: (ReaderSavedBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onSwitchToGlobalScheme: () -> Unit,
    onSwitchToReaderScheme: (String) -> Unit,
    onCreateReaderScheme: (ReaderPersonalizedScheme) -> Unit,
    onRenameReaderScheme: (schemeId: String, name: String) -> Unit,
    onDeleteReaderScheme: (schemeId: String) -> Unit,
    onUpdateGlobalReaderSettings: (ReaderSettings) -> Unit,
    onUpdateReaderSchemeSettings: (schemeId: String, settings: ReaderSettings) -> Unit,
    openAdvanced: Boolean,
    modifier: Modifier = Modifier,
) {
    val activeAccessoryPanel =
        when {
            readingSchemePanelVisible -> ReaderAccessoryPanelKind.ReadingScheme
            bookmarkPanelVisible -> ReaderAccessoryPanelKind.Bookmarks
            else -> null
        }
    var lastAccessoryPanel by remember { mutableStateOf<ReaderAccessoryPanelKind?>(null) }
    if (activeAccessoryPanel != null) {
        lastAccessoryPanel = activeAccessoryPanel
    }
    val railWidth = ReaderSideRailWidth
    val panelWidth = ReaderSidePanelWidth
    val railEnter =
        if (materialEInkMode) {
            EnterTransition.None
        } else {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(durationMillis = 280, easing = ReaderPanelExpandEasing),
            ) + fadeIn(animationSpec = tween(durationMillis = 180))
        }
    val railExit =
        if (materialEInkMode) {
            ExitTransition.None
        } else {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(durationMillis = 220, easing = ReaderPanelCollapseEasing),
            ) + fadeOut(animationSpec = tween(durationMillis = 140))
        }

    ReaderMaterialTheme(
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        Box(modifier = modifier.fillMaxHeight()) {
            Box(
                modifier =
                    Modifier
                        .width(railWidth)
                        .fillMaxHeight()
                        .zIndex(1f)
                        .clickable(enabled = !visible, onClick = onShow),
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = railEnter,
                    exit = railExit,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 3.dp,
                        shadowElevation = 0.dp,
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(
                                        top = topPadding + 8.dp,
                                        bottom = bottomPadding + 8.dp,
                                    ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            IconButton(
                                onClick = onHide,
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChevronLeft,
                                    contentDescription = stringResource(Res.string.cd_close),
                                )
                            }
                            IconButton(
                                onClick = onMenu,
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = stringResource(Res.string.cd_table_of_contents),
                                )
                            }
                            IconButton(
                                onClick = onSasayaki,
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = stringResource(Res.string.cd_open_audiobook),
                                )
                            }
                            ReaderSideChromeIconButton(
                                selected = readingSchemePanelVisible,
                                onClick = onReadingScheme,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = stringResource(Res.string.cd_reading_scheme),
                                )
                            }
                            ReaderSideChromeIconButton(
                                selected = bookmarkPanelVisible,
                                onClick = onBookmark,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bookmark,
                                    contentDescription = stringResource(Res.string.cd_bookmarks),
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                AnimatedVisibility(
                    visible = !visible,
                    modifier = Modifier.align(Alignment.TopCenter),
                    enter = railEnter,
                    exit = railExit,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .padding(top = topPadding + 12.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onShow),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = stringResource(Res.string.cd_table_of_contents),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                        )
                    }
                }
            }
            ReaderSideAccessoryPanel(
                visible = visible && activeAccessoryPanel != null,
                materialEInkMode = materialEInkMode,
                topPadding = topPadding,
                modifier =
                    Modifier
                        .padding(start = railWidth)
                        .width(panelWidth)
                        .fillMaxHeight()
                        .zIndex(0f),
            ) {
                ReaderSideAccessoryPanelContent(
                    panel = activeAccessoryPanel ?: lastAccessoryPanel,
                    settings = settings,
                    bookId = bookId,
                    bookSchemeId = bookSchemeId,
                    bookLastSchemeId = bookLastSchemeId,
                    openAdvanced = openAdvanced,
                    bookmarks = bookmarks,
                    currentPositionBookmarked = currentPositionBookmarked,
                    chapterTitleForBookmark = chapterTitleForBookmark,
                    onReadingScheme = onReadingScheme,
                    onBookmark = onBookmark,
                    onToggleCurrentBookmark = onToggleCurrentBookmark,
                    onSelectBookmark = onSelectBookmark,
                    onDeleteBookmark = onDeleteBookmark,
                    onSwitchToGlobalScheme = onSwitchToGlobalScheme,
                    onSwitchToReaderScheme = onSwitchToReaderScheme,
                    onCreateReaderScheme = onCreateReaderScheme,
                    onRenameReaderScheme = onRenameReaderScheme,
                    onDeleteReaderScheme = onDeleteReaderScheme,
                    onUpdateGlobalReaderSettings = onUpdateGlobalReaderSettings,
                    onUpdateReaderSchemeSettings = onUpdateReaderSchemeSettings,
                )
            }
        }
    }
}

@Composable
private fun ReaderSideChromeIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (selected) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp),
            content = content,
        )
    } else {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp),
            content = content,
        )
    }
}

@Composable
private fun ReaderSideAccessoryPanel(
    visible: Boolean,
    materialEInkMode: Boolean,
    topPadding: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val enter =
        if (materialEInkMode) {
            EnterTransition.None
        } else {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = 280, easing = ReaderPanelExpandEasing),
            )
        }
    val exit =
        if (materialEInkMode) {
            ExitTransition.None
        } else {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(durationMillis = 220, easing = ReaderPanelCollapseEasing),
            )
        }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.98f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 2.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = 20.dp,
                            top = topPadding + 20.dp,
                            end = 20.dp,
                            bottom = 20.dp,
                        ),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ReaderSideAccessoryPanelContent(
    panel: ReaderAccessoryPanelKind?,
    settings: AppSettings,
    bookId: String,
    bookSchemeId: String?,
    bookLastSchemeId: String?,
    openAdvanced: Boolean,
    bookmarks: List<ReaderSavedBookmark>,
    currentPositionBookmarked: Boolean,
    chapterTitleForBookmark: (Int) -> String,
    onReadingScheme: () -> Unit,
    onBookmark: () -> Unit,
    onToggleCurrentBookmark: () -> Unit,
    onSelectBookmark: (ReaderSavedBookmark) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onSwitchToGlobalScheme: () -> Unit,
    onSwitchToReaderScheme: (String) -> Unit,
    onCreateReaderScheme: (ReaderPersonalizedScheme) -> Unit,
    onRenameReaderScheme: (schemeId: String, name: String) -> Unit,
    onDeleteReaderScheme: (schemeId: String) -> Unit,
    onUpdateGlobalReaderSettings: (ReaderSettings) -> Unit,
    onUpdateReaderSchemeSettings: (schemeId: String, settings: ReaderSettings) -> Unit,
) {
    when (panel) {
        ReaderAccessoryPanelKind.ReadingScheme -> {
            ReaderReadingSchemePanel(
                settings = settings,
                bookId = bookId,
                bookSchemeId = bookSchemeId,
                bookLastSchemeId = bookLastSchemeId,
                openAdvanced = openAdvanced,
                onClose = onReadingScheme,
                onSwitchToGlobal = onSwitchToGlobalScheme,
                onSwitchToScheme = onSwitchToReaderScheme,
                onCreateScheme = onCreateReaderScheme,
                onRenameScheme = onRenameReaderScheme,
                onDeleteScheme = onDeleteReaderScheme,
                onUpdateGlobalSettings = onUpdateGlobalReaderSettings,
                onUpdateSchemeSettings = onUpdateReaderSchemeSettings,
            )
        }

        ReaderAccessoryPanelKind.Bookmarks -> {
            ReaderBookmarksPanel(
                bookmarks = bookmarks,
                chapterTitleFor = chapterTitleForBookmark,
                currentPositionBookmarked = currentPositionBookmarked,
                onToggleCurrentBookmark = onToggleCurrentBookmark,
                onSelectBookmark = onSelectBookmark,
                onDeleteBookmark = onDeleteBookmark,
                onClose = onBookmark,
            )
        }

        null -> {
            Unit
        }
    }
}

@Composable
internal fun FloatingReaderButton(
    isDark: Boolean,
    materialEInkMode: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    ReaderMaterialTheme(
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = false,
        monetKeyColor = 0L,
    ) {
        Surface(
            onClick = onClick,
            modifier =
                modifier
                    .size(52.dp)
                    .clip(CircleShape),
            enabled = enabled,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 3.dp,
            shadowElevation = if (materialEInkMode) 0.dp else 10.dp,
            content = {
                Box(contentAlignment = Alignment.Center) {
                    content()
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ReaderMaterialTheme(
    isDark: Boolean,
    materialEInkMode: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    content: @Composable () -> Unit,
) {
    val materialThemeConfig =
        if (materialEInkMode) {
            val colorScheme = remember(isDark) { materialEInkColorScheme(isDark) }
            remember(colorScheme) {
                MaterialThemeConfig(
                    seedColor = colorScheme.primary,
                    colorSchemeOverride = colorScheme,
                )
            }
        } else {
            rememberMaterialThemeConfig(
                darkTheme = isDark,
                monetEnabled = monetEnabled,
                monetKeyColor = monetKeyColor,
            )
        }
    val dynamicThemeState =
        rememberDynamicMaterialThemeState(
            isDark = isDark,
            style = PaletteStyle.TonalSpot,
            contrastLevel = -1.0,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            seedColor = materialThemeConfig.seedColor,
            modifyColorScheme =
                materialThemeConfig.colorSchemeOverride?.let { colorSchemeOverride ->
                    { colorSchemeOverride }
                },
        )
    DynamicMaterialExpressiveTheme(
        state = dynamicThemeState,
        motionScheme = MotionScheme.expressive(),
        animate = !materialEInkMode,
        content = content,
    )
}

@Composable
internal fun ReaderSheetTheme(
    isDark: Boolean,
    materialEInkMode: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    content: @Composable () -> Unit,
) {
    val controller =
        remember(isDark, materialEInkMode, monetEnabled, monetKeyColor) {
            ThemeController(
                colorSchemeMode =
                    if (materialEInkMode) {
                        if (isDark) ColorSchemeMode.Dark else ColorSchemeMode.Light
                    } else if (monetEnabled) {
                        if (isDark) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetLight
                    } else {
                        if (isDark) ColorSchemeMode.Dark else ColorSchemeMode.Light
                    },
                keyColor =
                    if (materialEInkMode) {
                        readerEInkSeedColor(isDark)
                    } else {
                        monetKeyColor.takeIf { it != 0L }?.let(::Color)
                    },
                colorSpec = ThemeColorSpec.Spec2025,
                paletteStyle = ThemePaletteStyle.TonalSpot,
            )
        }
    MiuixTheme(
        controller = controller,
        content = content,
    )
}
