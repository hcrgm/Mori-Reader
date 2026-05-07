package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import app.mori.reader.shared.generated.resources.sasayaki_sync_position
import org.jetbrains.compose.resources.stringResource
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
    player: SasayakiPlayerSnapshot,
    currentCueText: String?,
    enabled: Boolean,
    syncEnabled: Boolean,
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
    onSyncEnabled: (Boolean) -> Unit,
    onAutoScroll: (Boolean) -> Unit,
    onAutoPauseOnLookup: (Boolean) -> Unit,
    onHighlightEnabled: (Boolean) -> Unit,
    onHighlightColor: (String) -> Unit,
) {
    var seekValue by remember(player.positionMs, player.durationMs) {
        mutableStateOf(player.positionMs.toFloat())
    }
    var delayValue by remember(player.delayMs) { mutableStateOf(player.delayMs.toFloat()) }
    var rateValue by remember(player.rate) { mutableStateOf(player.rate) }
    ReaderSheetTheme(isDark = isDark) {
        WindowBottomSheet(
            show = show,
            title = "Sasayaki",
            onDismissRequest = onDismiss,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text =
                        currentCueText
                            ?: if (enabled) {
                                stringResource(Res.string.sasayaki_ready)
                            } else {
                                stringResource(Res.string.sasayaki_import_required)
                            },
                    color = if (enabled) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 3,
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
                        onValueChange = { seekValue = it },
                        valueRange = 0f..player.durationMs.coerceAtLeast(1L).toFloat(),
                        enabled = player.isReady,
                        onValueChangeFinished = { onSeek(seekValue.toLong()) },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        text = stringResource(Res.string.sasayaki_previous),
                        enabled = enabled && player.isReady,
                        onClick = onPrevious,
                    )
                    Spacer(Modifier.width(14.dp))
                    FloatingReaderButton(
                        isDark = isDark,
                        onClick = onPlayPause,
                    ) {
                        Icon(
                            imageVector = if (player.isPlaying) MiuixIcons.Pause else MiuixIcons.Play,
                            contentDescription =
                                if (player.isPlaying) {
                                    stringResource(Res.string.cd_pause)
                                } else {
                                    stringResource(Res.string.cd_play)
                                },
                            tint = if (isDark) Color(0xFFF3F1EA) else Color(0xFF1C1B18),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    TextButton(
                        text = stringResource(Res.string.sasayaki_next),
                        enabled = enabled && player.isReady,
                        onClick = onNext,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(Res.string.sasayaki_delay),
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${delayValue.toInt()} ms",
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Slider(
                        value = delayValue,
                        onValueChange = { delayValue = it },
                        valueRange = -2000f..2000f,
                        enabled = enabled,
                        onValueChangeFinished = { onDelay(delayValue.toLong()) },
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = stringResource(Res.string.sasayaki_speed),
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${(rateValue * 100).toInt()}%",
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Slider(
                        value = rateValue,
                        onValueChange = { rateValue = it },
                        valueRange = 0.5f..1.5f,
                        enabled = enabled,
                        onValueChangeFinished = { onRate(rateValue) },
                    )
                }
                SasayakiSwitchRow(
                    title = stringResource(Res.string.sasayaki_sync_position),
                    checked = syncEnabled,
                    enabled = false,
                    onCheckedChange = onSyncEnabled,
                )
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
                Box(
                    modifier =
                        Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(parseHexColor(value)))
                            .then(
                                if (enabled) {
                                    Modifier.clickable { onSelect(value) }
                                } else {
                                    Modifier
                                },
                            ).shadow(
                                elevation = if (isSelected) 6.dp else 0.dp,
                                shape = CircleShape,
                                spotColor = Color(parseHexColor(value)),
                            ),
                )
            }
        }
    }
}
