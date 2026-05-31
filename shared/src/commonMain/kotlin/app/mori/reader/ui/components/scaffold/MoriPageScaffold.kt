package app.mori.reader.ui.components.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLayoutDirection
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.ui.theme.MoriTheme
import app.mori.reader.ui.theme.moriSurfaceColor
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur

@Composable
internal fun MoriPageScaffold(
    title: String,
    subtitle: String = "",
    useSmallTopBar: Boolean = false,
    revealTopBarOnReverseScroll: Boolean = false,
    blurEnabled: Boolean,
    fixedPadding: PaddingValues = PaddingValues(),
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    header: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val effectiveBlurEnabled =
        blurEnabled &&
            !(MoriTheme.uiThemeEngine == UiThemeEngine.Material && MoriTheme.materialEInkMode)
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixMoriPageScaffold(
                title = title,
                subtitle = subtitle,
                useSmallTopBar = useSmallTopBar,
                revealTopBarOnReverseScroll = revealTopBarOnReverseScroll,
                blurEnabled = effectiveBlurEnabled,
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
                useSmallTopBar = useSmallTopBar,
                revealTopBarOnReverseScroll = revealTopBarOnReverseScroll,
                blurEnabled = effectiveBlurEnabled,
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
