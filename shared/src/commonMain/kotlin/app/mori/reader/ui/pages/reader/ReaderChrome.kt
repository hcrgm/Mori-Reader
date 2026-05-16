package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_appearance
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.cd_open_audiobook
import app.mori.reader.shared.generated.resources.cd_table_of_contents
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.FloatingToolbar
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Background
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Music
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

@Composable
internal fun ReaderStatus(
    text: String?,
    isDark: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
) {
    ReaderSheetTheme(
        isDark = isDark,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text.orEmpty(),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
internal fun ReaderHeaderInfo(
    title: String,
    progress: String?,
    isDark: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    modifier: Modifier = Modifier,
) {
    if (title.isBlank() && progress.isNullOrBlank()) return
    ReaderSheetTheme(
        isDark = isDark,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.44f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            progress?.let {
                Text(
                    text = it,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.44f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun ReaderBottomChrome(
    isDark: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    bottomPadding: Dp,
    onBack: () -> Unit,
    onMenu: () -> Unit,
    onAppearance: () -> Unit,
    onSasayaki: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ReaderSheetTheme(
        isDark = isDark,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        val buttonContentColor = MiuixTheme.colorScheme.onSurface
        val floatingBackground = floatingReaderContainerColor()
        Box(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(
                        start = 22.dp,
                        end = 22.dp,
                        bottom = bottomPadding + 12.dp,
                    ),
        ) {
            FloatingReaderButton(
                isDark = isDark,
                onClick = onBack,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart),
            ) {
                Icon(
                    MiuixIcons.Back,
                    tint = buttonContentColor,
                    contentDescription = stringResource(Res.string.cd_back),
                )
            }
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                FloatingToolbar(
                    color = floatingBackground,
                    shadowElevation = 10.dp,
                    outSidePadding = PaddingValues(0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ReaderToolbarButton(
                            onClick = onSasayaki,
                        ) {
                            Icon(
                                MiuixIcons.Music,
                                tint = buttonContentColor,
                                contentDescription = stringResource(Res.string.cd_open_audiobook),
                            )
                        }
                        ReaderToolbarButton(
                            onClick = onAppearance,
                        ) {
                            Icon(
                                MiuixIcons.Background,
                                tint = buttonContentColor,
                                contentDescription = stringResource(Res.string.cd_appearance),
                            )
                        }
                        ReaderToolbarButton(
                            onClick = onMenu,
                        ) {
                            Icon(
                                MiuixIcons.ListView,
                                tint = buttonContentColor,
                                contentDescription = stringResource(Res.string.cd_table_of_contents),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderToolbarButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        minWidth = 40.dp,
        minHeight = 40.dp,
        backgroundColor = Color.Transparent,
        content = content,
    )
}

@Composable
private fun floatingReaderContainerColor(): Color = MiuixTheme.colorScheme.surface.copy(alpha = 0.94f)

@Composable
internal fun FloatingReaderButton(
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val buttonBackground = floatingReaderContainerColor()
    val buttonShadow =
        if (isDark) {
            Color.Black.copy(alpha = 0.34f)
        } else {
            Color(0xFF1C1B18).copy(alpha = 0.16f)
        }
    Box(
        modifier =
            modifier
                .size(52.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    spotColor = buttonShadow,
                ).clip(CircleShape)
                .background(buttonBackground)
                .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
internal fun ReaderSheetTheme(
    isDark: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    content: @Composable () -> Unit,
) {
    val controller =
        remember(isDark, monetEnabled, monetKeyColor) {
            ThemeController(
                colorSchemeMode =
                    if (monetEnabled) {
                        if (isDark) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetLight
                    } else {
                        if (isDark) ColorSchemeMode.Dark else ColorSchemeMode.Light
                    },
                keyColor = monetKeyColor.takeIf { it != 0L }?.let(::Color),
                colorSpec = ThemeColorSpec.Spec2025,
                paletteStyle = ThemePaletteStyle.TonalSpot,
            )
        }
    MiuixTheme(
        controller = controller,
        content = content,
    )
}
