package app.mori.reader.ui.components.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.ui.theme.MoriTheme
import app.mori.reader.ui.theme.moriSurfaceColor
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import androidx.compose.material3.Scaffold as MaterialScaffold
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar

@Composable
internal fun MoriPageScaffold(
    title: String,
    subtitle: String = "",
    blurEnabled: Boolean,
    fixedPadding: PaddingValues = PaddingValues(),
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    header: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixMoriPageScaffold(
                title = title,
                subtitle = subtitle,
                blurEnabled = blurEnabled,
                fixedPadding = fixedPadding,
                navigationIcon = navigationIcon,
                actions = actions,
                header = header,
                floatingActionButton = floatingActionButton,
                content = content,
            )
        }

        UiThemeEngine.Material -> {
            MaterialMoriPageScaffold(
                title = title,
                subtitle = subtitle,
                blurEnabled = blurEnabled,
                fixedPadding = fixedPadding,
                navigationIcon = navigationIcon,
                actions = actions,
                header = header,
                floatingActionButton = floatingActionButton,
                content = content,
            )
        }
    }
}

@Composable
internal fun MiuixMoriPageScaffold(
    title: String,
    subtitle: String = "",
    blurEnabled: Boolean,
    fixedPadding: PaddingValues = PaddingValues(),
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    header: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val backdrop = rememberMoriPageBackdrop(blurEnabled)
    val barColor = backdrop.moriPageBarColor(moriSurfaceColor())
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    val layoutDirection = LocalLayoutDirection.current
    val fixedStartPadding = fixedPadding.calculateStartPadding(layoutDirection)
    val fixedEndPadding = fixedPadding.calculateEndPadding(layoutDirection)

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .scrollEndHaptic(),
    ) {
        MiuixScaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Box(
                    modifier = Modifier.moriPageBarBlur(backdrop),
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                start = fixedStartPadding,
                                end = fixedEndPadding,
                            ),
                    ) {
                        MiuixTopAppBar(
                            title = title,
                            largeTitle = title,
                            subtitle = subtitle,
                            color = barColor,
                            scrollBehavior = scrollBehavior,
                            navigationIcon = navigationIcon,
                            actions = actions,
                        )
                        header()
                    }
                }
            },
            floatingActionButton = floatingActionButton,
        ) { paddingValues ->
            val pagePaddingValues =
                PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + fixedPadding.calculateBottomPadding(),
                )
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = fixedStartPadding,
                            end = fixedEndPadding,
                        ).moriPageBackdrop(backdrop),
            ) {
                content(pagePaddingValues)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MaterialMoriPageScaffold(
    title: String,
    subtitle: String = "",
    blurEnabled: Boolean,
    fixedPadding: PaddingValues = PaddingValues(),
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    header: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val backdrop = rememberMoriPageBackdrop(blurEnabled)
    val barColor = backdrop.moriPageBarColor(moriSurfaceColor())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val layoutDirection = LocalLayoutDirection.current
    val fixedStartPadding = fixedPadding.calculateStartPadding(layoutDirection)
    val fixedEndPadding = fixedPadding.calculateEndPadding(layoutDirection)

    Box(modifier = Modifier.fillMaxSize()) {
        MaterialScaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Box(
                    modifier = Modifier.moriPageBarBlur(backdrop),
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                start = fixedStartPadding,
                                end = fixedEndPadding,
                            ),
                    ) {
                        LargeFlexibleTopAppBar(
                            title = { Text(text = title) },
                            navigationIcon = navigationIcon,
                            actions = actions,
                            windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 12.dp)),
                            colors =
                                TopAppBarDefaults.topAppBarColors(
                                    containerColor = barColor,
                                    scrolledContainerColor = barColor,
                                ),
                            scrollBehavior = scrollBehavior,
                        )
                        if (subtitle.isNotEmpty()) {
                            Text(
                                text = subtitle,
                                modifier =
                                    Modifier.padding(
                                        start = 24.dp,
                                        end = 24.dp,
                                        bottom = 8.dp,
                                    ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        header()
                    }
                }
            },
            floatingActionButton = floatingActionButton,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        ) { paddingValues ->
            val pagePaddingValues =
                PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + fixedPadding.calculateBottomPadding(),
                )
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = fixedStartPadding,
                            end = fixedEndPadding,
                        ).moriPageBackdrop(backdrop),
            ) {
                content(pagePaddingValues)
            }
        }
    }
}

@Composable
internal fun Modifier.moriFixedHorizontalPadding(fixedPadding: PaddingValues): Modifier {
    val layoutDirection = LocalLayoutDirection.current
    return padding(
        start = fixedPadding.calculateStartPadding(layoutDirection),
        end = fixedPadding.calculateEndPadding(layoutDirection),
    )
}

@Composable
internal fun rememberMoriPageBackdrop(blurEnabled: Boolean): LayerBackdrop? {
    if (!blurEnabled || !isRenderEffectSupported()) return null

    val surfaceColor = moriSurfaceColor()
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

internal fun LayerBackdrop?.moriPageBarColor(opaqueColor: Color): Color = if (this != null) Color.Transparent else opaqueColor

@Composable
internal fun Modifier.moriPageBarBlur(backdrop: LayerBackdrop?): Modifier =
    then(
        if (backdrop != null) {
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = 25f,
                colors =
                    BlurColors(
                        blendColors =
                            listOf(
                                BlendColorEntry(
                                    color = moriSurfaceColor().copy(alpha = 0.8f),
                                ),
                            ),
                    ),
            )
        } else {
            Modifier.background(moriSurfaceColor())
        },
    )

internal fun Modifier.moriPageBackdrop(backdrop: LayerBackdrop?): Modifier =
    if (backdrop != null) {
        then(Modifier.layerBackdrop(backdrop))
    } else {
        this
    }
