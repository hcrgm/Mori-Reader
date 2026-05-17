package app.mori.reader.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.scaffold.moriPageBarBlur
import app.mori.reader.ui.components.scaffold.moriPageBarColor
import app.mori.reader.ui.theme.MoriTheme
import app.mori.reader.ui.theme.moriSurfaceColor
import top.yukonga.miuix.kmp.basic.NavigationRailDisplayMode
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.basic.NavigationRail as MiuixNavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem as MiuixNavigationRailItem

@Composable
internal fun MiuixMoriNavigationRail(
    selectedTab: AppTab,
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    modifier: Modifier = Modifier,
    onTabSelected: (AppTab) -> Unit,
) {
    val activeBackdrop = backdrop.takeIf { blurEnabled }
    val barColor = activeBackdrop.moriPageBarColor(moriSurfaceColor())
    val layoutDirection = LocalLayoutDirection.current
    val startPadding =
        maxOf(
            WindowInsets.displayCutout
                .only(WindowInsetsSides.Start)
                .asPaddingValues()
                .calculateStartPadding(layoutDirection),
            WindowInsets.navigationBars
                .only(WindowInsetsSides.Start)
                .asPaddingValues()
                .calculateStartPadding(layoutDirection),
        )
    val statusBarPadding =
        WindowInsets.statusBars
            .only(WindowInsetsSides.Vertical)
            .asPaddingValues()
            .calculateTopPadding()

    Box(
        modifier = modifier.moriPageBarBlur(activeBackdrop),
    ) {
        Box(
            modifier =
                Modifier
                    .background(barColor)
                    .padding(
                        start = startPadding,
                        top = statusBarPadding,
                    ),
        ) {
            MiuixNavigationRail(
                color = barColor,
                defaultWindowInsetsPadding = false,
                mode = NavigationRailDisplayMode.IconAndText,
            ) {
                AppTab.entries.forEach { tab ->
                    MiuixNavigationRailItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = tab.miuixIcon(),
                        label = tab.localizedLabel(),
                    )
                }
            }
        }
    }
}

@Composable
internal fun MaterialMoriNavigationRail(
    selectedTab: AppTab,
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    modifier: Modifier = Modifier,
    onTabSelected: (AppTab) -> Unit,
) {
    val activeBackdrop = backdrop.takeIf { blurEnabled }
    val barColor = activeBackdrop.moriPageBarColor(MaterialTheme.colorScheme.surfaceContainer)
    val isEInkMode = MoriTheme.materialEInkMode
    val itemColors =
        NavigationRailItemDefaults.colors(
            selectedIconColor =
                if (isEInkMode) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                },
            selectedTextColor =
                if (isEInkMode) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.primary
                },
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor =
                if (isEInkMode) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
        )

    Box(
        modifier = modifier.moriPageBarBlur(activeBackdrop),
    ) {
        NavigationRail(
            containerColor = barColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            AppTab.entries.forEach { tab ->
                NavigationRailItem(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    colors = itemColors,
                    icon = {
                        Icon(
                            imageVector = tab.materialIcon(),
                            contentDescription = tab.localizedLabel(),
                        )
                    },
                    label = { Text(text = tab.localizedLabel()) },
                )
            }
        }
    }
}
