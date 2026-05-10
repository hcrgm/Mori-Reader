package app.mori.reader.ui.pages.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_close
import app.mori.reader.shared.generated.resources.cd_next
import app.mori.reader.shared.generated.resources.cd_previous
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun DictionaryPopupActionBar(
    canNavigateBack: Boolean,
    canNavigateForward: Boolean,
    blurEnabled: Boolean,
    backdrop: LayerBackdrop?,
    isDark: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit,
    onClose: () -> Unit,
) {
    if (!canNavigateBack && !canNavigateForward) return

    val density = LocalDensity.current
    val topBarShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (blurEnabled && backdrop != null) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = topBarShape,
                            blurRadius = 25f * density.density,
                            noiseCoefficient = 0f,
                            colors =
                                BlurColors(
                                    blendColors =
                                        listOf(
                                            BlendColorEntry(
                                                color =
                                                    MiuixTheme.colorScheme.surface.copy(
                                                        alpha = if (isDark) 0.82f else 0.74f,
                                                    ),
                                            ),
                                        ),
                                ),
                        )
                    } else {
                        Modifier.background(
                            MiuixTheme.colorScheme.surfaceContainerHighest.copy(
                                alpha = if (isDark) 0.78f else 0.92f,
                            ),
                            shape = topBarShape,
                        )
                    },
                ),
    ) {
        CompositionLocalProvider(LocalContentColor provides MiuixTheme.colorScheme.onSurface) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavigationButton(
                        enabled = canNavigateBack,
                        onClick = onNavigateBack,
                        icon = {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = MiuixIcons.ChevronBackward,
                                contentDescription = stringResource(Res.string.cd_previous),
                            )
                        },
                    )
                    NavigationButton(
                        enabled = canNavigateForward,
                        onClick = onNavigateForward,
                        icon = {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = MiuixIcons.ChevronForward,
                                contentDescription = stringResource(Res.string.cd_next),
                            )
                        },
                    )
                }
                NavigationButton(
                    enabled = true,
                    onClick = onClose,
                    icon = {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = MiuixIcons.Close,
                            contentDescription = stringResource(Res.string.cd_close),
                        )
                    },
                )
            }
        }
        HorizontalDivider(color = MiuixTheme.colorScheme.dividerLine)
    }
}

@Composable
private fun NavigationButton(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        minWidth = 32.dp,
        minHeight = 32.dp,
        modifier = Modifier.alpha(if (enabled) 1f else 0.3f),
    ) {
        icon()
    }
}
