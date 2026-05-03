package app.mori.reader.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    onTabSelected: (AppTab) -> Unit,
) {
    val blurActive = blurEnabled && backdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface

    BlurredBar(backdrop, blurEnabled) {
        NavigationRail(
            modifier = androidx.compose.ui.Modifier.background(barColor),
            color = barColor,
            mode = NavigationRailDisplayMode.IconAndText,
        ) {
            AppTab.entries.forEach { tab ->
                NavigationRailItem(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    icon = tab.icon,
                    label = tab.label,
                )
            }
        }
    }
}
