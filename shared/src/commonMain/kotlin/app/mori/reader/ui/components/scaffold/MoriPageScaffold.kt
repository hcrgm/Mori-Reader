package app.mori.reader.ui.components.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
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
    message: String? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    header: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable BoxScope.(PaddingValues, ScrollBehavior) -> Unit,
) {
    val backdrop = rememberMoriBlurBackdrop(blurEnabled)
    val barColor = if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surface
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scrollEndHaptic(),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                BlurredBar(backdrop, blurEnabled) {
                    Column {
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
            val pagePaddingValues = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + fixedPadding.calculateBottomPadding(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
            ) {
                content(pagePaddingValues, scrollBehavior)

                if (message != null) {
                    EffectBanner(
                        message = message,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = paddingValues.calculateTopPadding() + 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun BlurredBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    Box(
        modifier = if (blurEnabled && backdrop != null) {
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = 25f * density.density,
                colors = BlurColors(
                    blendColors = listOf(
                        BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(0.8f)),
                    ),
                ),
            )
        } else {
            Modifier
        },
    ) {
        content()
    }
}

@Composable
private fun EffectBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
        ),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun rememberMoriBlurBackdrop(
    blurEnabled: Boolean,
): LayerBackdrop? {
    if (!blurEnabled || !isRenderEffectSupported()) return null

    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}
