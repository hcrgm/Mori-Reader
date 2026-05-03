package app.mori.reader.ui.components.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.scaffold.BlurredBar
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MoriNavigationBar(
    selectedTab: AppTab,
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    onTabSelected: (AppTab) -> Unit,
) {
    val blurActive = blurEnabled && backdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface

    BlurredBar(backdrop, blurEnabled) {
        NavigationBar(
            color = barColor,
            showDivider = false,
        ) {
            AppTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    icon = tab.icon,
                    label = tab.label,
                )
            }
        }
    }
}
