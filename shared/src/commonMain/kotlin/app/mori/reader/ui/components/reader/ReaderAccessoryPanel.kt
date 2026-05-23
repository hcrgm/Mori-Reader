package app.mori.reader.ui.components.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val ReaderPanelExpandEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private val ReaderPanelCollapseEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)

@Composable
internal fun ReaderAccessoryPanel(
    visible: Boolean,
    maxHeight: Dp? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier.fillMaxWidth(),
        enter =
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 280, easing = ReaderPanelExpandEasing),
            ),
        exit =
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 220, easing = ReaderPanelCollapseEasing),
            ),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (maxHeight != null) {
                            Modifier.heightIn(max = maxHeight)
                        } else {
                            Modifier
                        },
                    ),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 1.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            top = 12.dp,
                            end = 20.dp,
                            bottom = 5.dp,
                        ),
                content = content,
            )
        }
    }
}
