package app.mori.reader.ui.pages.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.data.audiobook.SasayakiPlayerSnapshot
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.audiobook_reading_title
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
import app.mori.reader.ui.components.material.MaterialModalBottomSheet
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderSasayakiSheet(
    show: Boolean,
    isDark: Boolean,
    materialEInkMode: Boolean,
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
    if (!show) return

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

    ReaderMaterialTheme(
        isDark = isDark,
        materialEInkMode = materialEInkMode,
        monetEnabled = monetEnabled,
        monetKeyColor = monetKeyColor,
    ) {
        MaterialModalBottomSheet(onDismissRequest = onDismiss) {
            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.7f)
                            .verticalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.audiobook_reading_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    MaterialAudiobookSectionCard {
                        Text(
                            text = statusText,
                            color =
                                if (enabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        MaterialAudiobookPlaybackSlider(
                            durationMs = player.durationMs,
                            seekValue = seekValue,
                            enabled = playerEnabled,
                            onValueChange = {
                                isSeeking = true
                                seekValue = it
                            },
                            onValueChangeFinished = {
                                isSeeking = false
                                onSeek(seekValue.toLong())
                            },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                enabled = playerEnabled,
                                onClick = onPrevious,
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FastRewind,
                                    contentDescription = stringResource(Res.string.sasayaki_previous),
                                )
                            }
                            FilledTonalIconButton(
                                enabled = playerEnabled,
                                onClick = onPlayPause,
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(
                                    imageVector = if (player.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription =
                                        if (player.isPlaying) {
                                            stringResource(Res.string.cd_pause)
                                        } else {
                                            stringResource(Res.string.cd_play)
                                    },
                                )
                            }
                            IconButton(
                                enabled = playerEnabled,
                                onClick = onNext,
                                modifier = Modifier.size(44.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FastForward,
                                    contentDescription = stringResource(Res.string.sasayaki_next),
                                )
                            }
                        }
                    }
                    MaterialAudiobookSectionCard {
                        MaterialAudiobookSliderControl(
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
                        MaterialAudiobookSliderControl(
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
                    MaterialAudiobookSectionCard {
                        MaterialAudiobookSwitchRow(
                            title = stringResource(Res.string.sasayaki_auto_scroll),
                            checked = autoScroll,
                            enabled = enabled,
                            onCheckedChange = onAutoScroll,
                        )
                        MaterialAudiobookSwitchRow(
                            title = stringResource(Res.string.sasayaki_auto_pause_lookup),
                            checked = autoPauseOnLookup,
                            enabled = enabled,
                            onCheckedChange = onAutoPauseOnLookup,
                        )
                        MaterialAudiobookSwitchRow(
                            title = stringResource(Res.string.sasayaki_show_highlight),
                            checked = highlightEnabled,
                            enabled = enabled,
                            onCheckedChange = onHighlightEnabled,
                        )
                        MaterialAudiobookColorRow(
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
@OptIn(ExperimentalMaterial3Api::class)
private fun MaterialAudiobookPlaybackSlider(
    durationMs: Long,
    seekValue: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    val maxValue = durationMs.coerceAtLeast(1L).toFloat()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MaterialAudiobookSupportingText(formatPlaybackTime(seekValue.toLong()))
        Slider(
            value = seekValue.coerceIn(0f, maxValue),
            onValueChange = onValueChange,
            valueRange = 0f..maxValue,
            enabled = enabled,
            onValueChangeFinished = onValueChangeFinished,
            thumb = {
                Box(
                    modifier =
                        Modifier
                            .size(width = 3.dp, height = 20.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = if (enabled) 1f else 0.38f,
                                ),
                            ),
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(4.dp),
                    enabled = enabled,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 0.dp,
                    trackInsideCornerSize = 2.dp,
                )
            },
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
        )
        MaterialAudiobookSupportingText(formatPlaybackTime(durationMs))
    }
}

@Composable
private fun MaterialAudiobookSectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun MaterialAudiobookSliderControl(
    title: String,
    valueText: String,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            MaterialAudiobookSupportingText(
                text = valueText,
                enabled = enabled,
            )
        }
        content()
    }
}

@Composable
private fun MaterialAudiobookSwitchRow(
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
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun MaterialAudiobookColorRow(
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
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.forEach { value ->
                MaterialAudiobookColorSwatch(
                    value = value,
                    selected = value == selected,
                    enabled = enabled,
                    onClick = { onSelect(value) },
                )
            }
        }
    }
}

@Composable
private fun MaterialAudiobookColorSwatch(
    value: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val color = Color(parseMaterialAudiobookCssHexColor(value))
    val ringColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        }
    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(width = 2.dp, color = ringColor, shape = CircleShape)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(4.dp)
                .alpha(if (enabled) 1f else 0.38f),
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

@Composable
private fun MaterialAudiobookSupportingText(
    text: String,
    enabled: Boolean = true,
) {
    Text(
        text = text,
        color =
            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (enabled) 1f else 0.38f,
            ),
        style = MaterialTheme.typography.bodySmall,
    )
}

private fun parseMaterialAudiobookCssHexColor(value: String): Long {
    val hex = value.removePrefix("#")
    if (hex.length != 8) return parseHexColor(value)
    val raw = hex.toLongOrNull(16) ?: return parseHexColor(value)
    val red = raw shr 24 and 0xFF
    val green = raw shr 16 and 0xFF
    val blue = raw shr 8 and 0xFF
    val alpha = raw and 0xFF
    return alpha shl 24 or (red shl 16) or (green shl 8) or blue
}
