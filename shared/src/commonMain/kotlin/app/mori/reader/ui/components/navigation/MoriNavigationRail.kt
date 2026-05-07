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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.scaffold.BlurredBar
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailDisplayMode
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MoriNavigationRail(
    selectedTab: AppTab,
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    modifier: Modifier = Modifier,
    onTabSelected: (AppTab) -> Unit,
) {
    val blurActive = blurEnabled && backdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface
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

    BlurredBar(backdrop, blurEnabled, modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .background(barColor)
                    .padding(
                        start = startPadding,
                        top = statusBarPadding,
                    ),
        ) {
            NavigationRail(
                color = barColor,
                defaultWindowInsetsPadding = false,
                mode = NavigationRailDisplayMode.IconAndText,
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationRailItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = tab.icon,
                        label = tab.localizedLabel(),
                    )
                }
            }
        }
    }
}
