package app.mori.reader.ui.components.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.navigation.MaterialMoriNavigationBar
import app.mori.reader.ui.components.navigation.MaterialMoriNavigationRail
import app.mori.reader.ui.components.navigation.MiuixMoriNavigationBar
import app.mori.reader.ui.components.navigation.MiuixMoriNavigationRail
import app.mori.reader.ui.theme.MoriTheme
import app.mori.reader.ui.theme.moriSurfaceColor
import androidx.compose.material3.Scaffold as MaterialScaffold
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold

@Composable
internal fun MoriMainTabsScaffold(
    selectedTab: AppTab,
    isWideScreen: Boolean,
    blurEnabled: Boolean,
    onTabSelected: (AppTab) -> Unit,
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val effectiveBlurEnabled =
        blurEnabled &&
            !(MoriTheme.uiThemeEngine == UiThemeEngine.Material && MoriTheme.materialEInkMode)
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixMoriMainTabsScaffold(
                selectedTab = selectedTab,
                isWideScreen = isWideScreen,
                blurEnabled = effectiveBlurEnabled,
                onTabSelected = onTabSelected,
                content = content,
            )
        }

        UiThemeEngine.Material -> {
            MaterialMoriMainTabsScaffold(
                selectedTab = selectedTab,
                isWideScreen = isWideScreen,
                blurEnabled = effectiveBlurEnabled,
                onTabSelected = onTabSelected,
                content = content,
            )
        }
    }
}

@Composable
private fun MiuixMoriMainTabsScaffold(
    selectedTab: AppTab,
    isWideScreen: Boolean,
    blurEnabled: Boolean,
    onTabSelected: (AppTab) -> Unit,
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val navigationBackdrop = rememberMoriPageBackdrop(blurEnabled)
    val density = LocalDensity.current

    if (isWideScreen) {
        var navigationRailWidthPx by remember { mutableIntStateOf(0) }
        val navigationRailPadding =
            PaddingValues(
                start = with(density) { navigationRailWidthPx.toDp() },
            )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(moriSurfaceColor()),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .moriPageBackdrop(navigationBackdrop),
            ) {
                content(navigationRailPadding)
            }
            MiuixMoriNavigationRail(
                selectedTab = selectedTab,
                backdrop = navigationBackdrop,
                blurEnabled = blurEnabled,
                onTabSelected = onTabSelected,
                modifier = Modifier.onSizeChanged { navigationRailWidthPx = it.width },
            )
        }
    } else {
        MiuixScaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                MiuixMoriNavigationBar(
                    selectedTab = selectedTab,
                    backdrop = navigationBackdrop,
                    blurEnabled = blurEnabled,
                    onTabSelected = onTabSelected,
                )
            },
        ) { fixedPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .moriPageBackdrop(navigationBackdrop),
            ) {
                content(fixedPadding)
            }
        }
    }
}

@Composable
private fun MaterialMoriMainTabsScaffold(
    selectedTab: AppTab,
    isWideScreen: Boolean,
    blurEnabled: Boolean,
    onTabSelected: (AppTab) -> Unit,
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val navigationBackdrop = rememberMoriPageBackdrop(blurEnabled)
    val density = LocalDensity.current

    if (isWideScreen) {
        var navigationRailWidthPx by remember { mutableIntStateOf(0) }
        val navigationRailPadding =
            PaddingValues(
                start = with(density) { navigationRailWidthPx.toDp() },
            )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .moriPageBackdrop(navigationBackdrop),
            ) {
                content(navigationRailPadding)
            }
            MaterialMoriNavigationRail(
                selectedTab = selectedTab,
                backdrop = navigationBackdrop,
                blurEnabled = blurEnabled,
                onTabSelected = onTabSelected,
                modifier = Modifier.onSizeChanged { navigationRailWidthPx = it.width },
            )
        }
    } else {
        MaterialScaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                MaterialMoriNavigationBar(
                    selectedTab = selectedTab,
                    backdrop = navigationBackdrop,
                    blurEnabled = blurEnabled,
                    onTabSelected = onTabSelected,
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) { fixedPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .moriPageBackdrop(navigationBackdrop),
            ) {
                content(fixedPadding)
            }
        }
    }
}
