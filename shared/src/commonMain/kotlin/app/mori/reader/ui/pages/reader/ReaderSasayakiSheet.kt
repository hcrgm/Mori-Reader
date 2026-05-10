package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.audiobook.SasayakiPlayerSnapshot
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_pause
import app.mori.reader.shared.generated.resources.cd_play
import app.mori.reader.shared.generated.resources.sasayaki_auto_pause_lookup
import app.mori.reader.shared.generated.resources.sasayaki_auto_scroll
import app.mori.reader.shared.generated.resources.sasayaki_delay
import app.mori.reader.shared.generated.resources.sasayaki_highlight_color
import app.mori.reader.shared.generated.resources.sasayaki_import_required
import app.mori.reader.shared.generated.resources.sasayaki_next
import app.mori.reader.shared.generated.resources.sasayaki_previous
import app.mori.reader.shared.generated.resources.sasayaki_ready
import app.mori.reader.shared.generated.resources.sasayaki_show_highlight
import app.mori.reader.shared.generated.resources.sasayaki_speed
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.icon.extended.Play
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet

@Composable
internal fun ReaderSasayakiSheet(
    show: Boolean,
    isDark: Boolean,
    monetEnabled: Boolean,
    monetKeyColor: Long,
    player: SasayakiPlayerSnapshot,
    currentCueText: String?,
    enabled: Boolean,
    autoScroll: Boolean,
    autoPauseOnLookup: Boolean,
    highlightEnabled: Boolean,
    highlightColor: String,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onDelay: (Long) -> Unit,
    onRate: (Float) -> Unit,
    onAutoScroll: (Boolean) -> Unit,
    onAutoPauseOnLookup: (Boolean) -> Unit,
    onHighlightEnabled: (Boolean) -> Unit,
    onHighlightColor: (String) -> Unit,
) {
    var seekValue by remember(show, player.durationMs) { mutableStateOf(player.positionMs.toFloat()) }
    var isSeeking by remember(show) { mutableStateOf(false) }
    var delayValue by remember(player.delayMs) { mutableStateOf(player.delayMs.toFloat()) }
    var rateValue by remember(player.rate) { mutableStateOf(player.rate) }
    val playerEnabled = enabled && player.isReady
    val statusText =
        currentCueText
            ?: if (enabled) {
                stringResource(Res.string.sasayaki_ready)
            } else {
                stringResource(Res.string.sasayaki_import_required)
            }
    LaunchedEffect(player.positionMs, player.durationMs, isSeeking) {
        if (!isSeeking) {
            seekValue =
                player.positionMs
                    .coerceIn(0L, player.durationMs.coerceAtLeast(1L))
                    .toFloat()
        }
    }
    ReaderSheetTheme(
        isDark = isDark,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        WindowBottomSheet(
            show = show,
            title = "Sasayaki",
            onDismissRequest = onDismiss,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SasayakiSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = statusText,
                            color = if (enabled) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            fontWeight = FontWeight.Medium,
                            minLines = 2,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = formatPlaybackTime(player.positionMs),
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                                Text(
                                    text = formatPlaybackTime(player.durationMs),
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                            Slider(
                                value =
                                    seekValue.coerceIn(
                                        0f,
                                        player.durationMs.coerceAtLeast(1L).toFloat(),
                                    ),
                                onValueChange = {
                                    isSeeking = true
                                    seekValue = it
                                },
                                valueRange = 0f..player.durationMs.coerceAtLeast(1L).toFloat(),
                                enabled = playerEnabled,
                                onValueChangeFinished = {
                                    isSeeking = false
                                    onSeek(seekValue.toLong())
                                },
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(
                                text = stringResource(Res.string.sasayaki_previous),
                                enabled = playerEnabled,
                                onClick = onPrevious,
                            )
                            Spacer(Modifier.width(14.dp))
                            FloatingReaderButton(
                                isDark = isDark,
                                onClick = onPlayPause,
                                enabled = playerEnabled,
                            ) {
                                Icon(
                                    imageVector = if (player.isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
                                    contentDescription =
                                        if (player.isPlaying) {
                                            stringResource(Res.string.cd_pause)
                                        } else {
                                            stringResource(Res.string.cd_play)
                                        },
                                    tint =
                                        if (playerEnabled) {
                                            MiuixTheme.colorScheme.onSurface
                                        } else {
                                            MiuixTheme.colorScheme.onSurfaceVariantSummary
                                        },
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            TextButton(
                                text = stringResource(Res.string.sasayaki_next),
                                enabled = playerEnabled,
                                onClick = onNext,
                            )
                        }
                    }
                }
                SasayakiSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SasayakiSliderControl(
                            title = stringResource(Res.string.sasayaki_delay),
                            valueText = "${delayValue.toInt()} ms",
                            enabled = enabled,
                        ) {
                            Slider(
                                value = delayValue,
                                onValueChange = { delayValue = it },
                                valueRange = -2000f..2000f,
                                enabled = enabled,
                                onValueChangeFinished = { onDelay(delayValue.toLong()) },
                            )
                        }
                        SasayakiSliderControl(
                            title = stringResource(Res.string.sasayaki_speed),
                            valueText = "${(rateValue * 100).toInt()}%",
                            enabled = enabled,
                        ) {
                            Slider(
                                value = rateValue,
                                onValueChange = { rateValue = it },
                                valueRange = 0.5f..1.5f,
                                enabled = enabled,
                                onValueChangeFinished = { onRate(rateValue) },
                            )
                        }
                    }
                }
                SasayakiSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SasayakiSwitchRow(
                            title = stringResource(Res.string.sasayaki_auto_scroll),
                            checked = autoScroll,
                            enabled = enabled,
                            onCheckedChange = onAutoScroll,
                        )
                        SasayakiSwitchRow(
                            title = stringResource(Res.string.sasayaki_auto_pause_lookup),
                            checked = autoPauseOnLookup,
                            enabled = enabled,
                            onCheckedChange = onAutoPauseOnLookup,
                        )
                        SasayakiSwitchRow(
                            title = stringResource(Res.string.sasayaki_show_highlight),
                            checked = highlightEnabled,
                            enabled = enabled,
                            onCheckedChange = onHighlightEnabled,
                        )
                        SasayakiColorRow(
                            selected = highlightColor,
                            enabled = enabled && highlightEnabled,
                            onSelect = onHighlightColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SasayakiSectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(16.dp),
    ) {
        content()
    }
}

@Composable
private fun SasayakiSliderControl(
    title: String,
    valueText: String,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Text(
                text = valueText,
                color =
                    if (enabled) {
                        MiuixTheme.colorScheme.onSurfaceVariantSummary
                    } else {
                        MiuixTheme.colorScheme.disabledOnSecondaryVariant
                    },
            )
        }
        content()
    }
}

@Composable
private fun SasayakiSwitchRow(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, color = MiuixTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SasayakiColorRow(
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    val colors =
        listOf(
            "#FFC0485C",
            "#FF3AA675",
            "#FF4979F5",
            "#FFE09F3E",
        )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.sasayaki_highlight_color),
            color = MiuixTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            colors.forEach { value ->
                val isSelected = value == selected
                SasayakiColorSwatch(
                    value = value,
                    selected = isSelected,
                    enabled = enabled,
                    onClick = { onSelect(value) },
                )
            }
        }
    }
}

@Composable
private fun SasayakiColorSwatch(
    value: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val color = Color(parseCssHexColor(value))
    val ringColor =
        if (selected) {
            MiuixTheme.colorScheme.primary
        } else {
            Color.Transparent
        }
    Box(
        modifier =
            Modifier
                .size(30.dp)
                .clip(CircleShape)
                .border(width = 1.5.dp, color = ringColor, shape = CircleShape)
                .then(
                    if (enabled) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ).padding(3.dp)
                .alpha(if (enabled) 1f else 0.42f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(color),
        )
    }
}

private fun parseCssHexColor(value: String): Long {
    val hex = value.removePrefix("#")
    if (hex.length != 8) return parseHexColor(value)
    val raw = hex.toLongOrNull(16) ?: return parseHexColor(value)
    val red = raw shr 24 and 0xFF
    val green = raw shr 16 and 0xFF
    val blue = raw shr 8 and 0xFF
    val alpha = raw and 0xFF
    return alpha shl 24 or (red shl 16) or (green shl 8) or blue
}
