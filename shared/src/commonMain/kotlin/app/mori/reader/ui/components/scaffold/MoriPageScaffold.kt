package app.mori.reader.ui.components.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

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
    content: @Composable BoxScope.(PaddingValues, ScrollBehavior) -> Unit,
) {
    val backdrop = rememberMoriBlurBackdrop(blurEnabled)
    val barColor = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface
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
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                BlurredBar(backdrop, blurEnabled) {
                    Column(
                        modifier =
                            Modifier.padding(
                                start = fixedStartPadding,
                                end = fixedEndPadding,
                            ),
                    ) {
                        TopAppBar(
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
                        ).then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
            ) {
                content(pagePaddingValues, scrollBehavior)
            }
        }
    }
}

@Composable
internal fun BlurredBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    Box(
        modifier =
            modifier.then(
                if (blurEnabled && backdrop != null) {
                    Modifier.textureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        blurRadius = 25f * density.density,
                        colors =
                            BlurColors(
                                blendColors =
                                    listOf(
                                        BlendColorEntry(
                                            color =
                                                MiuixTheme.colorScheme.surface.copy(
                                                    0.8f,
                                                ),
                                        ),
                                    ),
                            ),
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        content()
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
private fun rememberMoriBlurBackdrop(blurEnabled: Boolean): LayerBackdrop? {
    if (!blurEnabled || !isRenderEffectSupported()) return null

    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}
