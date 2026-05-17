package app.mori.reader.ui.components.navigation

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun Modifier.eInkPagerSwipeModifier(
    enabled: Boolean,
    currentPage: Int,
    pageCount: Int,
    onPageChange: (Int) -> Unit,
): Modifier =
    composed {
        if (!enabled || pageCount <= 1) {
            return@composed this
        }

        val density = LocalDensity.current
        val dragThresholdPx = with(density) { 56.dp.toPx() }
        var widthPx by remember { mutableIntStateOf(0) }
        var dragOffsetPx by remember { mutableFloatStateOf(0f) }
        val draggableState = rememberDraggableState { delta -> dragOffsetPx += delta }

        this
            .onSizeChanged { widthPx = it.width }
            .draggable(
                orientation = Orientation.Horizontal,
                enabled = true,
                state = draggableState,
                onDragStopped = {
                    val pageThresholdPx = max(widthPx * 0.12f, dragThresholdPx)
                    val targetPage =
                        when {
                            dragOffsetPx <= -pageThresholdPx && currentPage < pageCount - 1 -> currentPage + 1
                            dragOffsetPx >= pageThresholdPx && currentPage > 0 -> currentPage - 1
                            else -> currentPage
                        }
                    dragOffsetPx = 0f
                    if (targetPage != currentPage) {
                        onPageChange(targetPage)
                    }
                },
            )
    }
