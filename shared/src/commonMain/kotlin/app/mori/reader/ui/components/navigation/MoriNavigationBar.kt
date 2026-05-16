package app.mori.reader.ui.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.tab_bookshelf
import app.mori.reader.shared.generated.resources.tab_dictionary
import app.mori.reader.shared.generated.resources.tab_settings
import app.mori.reader.ui.AppTab
import app.mori.reader.ui.components.scaffold.moriPageBarBlur
import app.mori.reader.ui.components.scaffold.moriPageBarColor
import app.mori.reader.ui.theme.moriSurfaceColor
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Album
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Translate
import androidx.compose.material.icons.rounded.Settings as MaterialSettings
import androidx.compose.material.icons.rounded.Translate as MaterialTranslate
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem

@Composable
internal fun MiuixMoriNavigationBar(
    selectedTab: AppTab,
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    onTabSelected: (AppTab) -> Unit,
) {
    val activeBackdrop = backdrop.takeIf { blurEnabled }
    val barColor = activeBackdrop.moriPageBarColor(moriSurfaceColor())

    Box(
        modifier = Modifier.moriPageBarBlur(activeBackdrop),
    ) {
        MiuixNavigationBar(
            color = barColor,
            showDivider = true,
        ) {
            AppTab.entries.forEach { tab ->
                MiuixNavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    icon = tab.miuixIcon(),
                    label = tab.localizedLabel(),
                )
            }
        }
    }
}

@Composable
internal fun MaterialMoriNavigationBar(
    selectedTab: AppTab,
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    onTabSelected: (AppTab) -> Unit,
) {
    val activeBackdrop = backdrop.takeIf { blurEnabled }
    val barColor = activeBackdrop.moriPageBarColor(MaterialTheme.colorScheme.surfaceContainer)
    val itemColors =
        NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        )

    Box(
        modifier = Modifier.moriPageBarBlur(activeBackdrop),
    ) {
        NavigationBar(
            containerColor = barColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            AppTab.entries.forEach { tab ->
                NavigationBarItem(
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

@Composable
internal fun AppTab.localizedLabel(): String =
    when (this) {
        AppTab.Home -> stringResource(Res.string.tab_bookshelf)
        AppTab.Dictionary -> stringResource(Res.string.tab_dictionary)
        AppTab.Settings -> stringResource(Res.string.tab_settings)
    }

internal fun AppTab.miuixIcon(): ImageVector =
    when (this) {
        AppTab.Home -> MiuixIcons.Album
        AppTab.Dictionary -> MiuixIcons.Translate
        AppTab.Settings -> MiuixIcons.Settings
    }

internal fun AppTab.materialIcon(): ImageVector =
    when (this) {
        AppTab.Home -> Icons.AutoMirrored.Rounded.MenuBook
        AppTab.Dictionary -> Icons.Rounded.MaterialTranslate
        AppTab.Settings -> Icons.Rounded.MaterialSettings
    }
