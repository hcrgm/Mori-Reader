package app.mori.reader.ui.components.loading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.btn_cancel
import app.mori.reader.ui.theme.MoriTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MaterialLoadingDialog(
    show: Boolean,
    state: MoriLoadingDialogState,
    onCancel: (() -> Unit)?,
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = { onCancel?.invoke() },
        title = { Text(text = state.title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                state.message?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                state.currentName?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                state.progressText()?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                val fraction = state.fraction?.coerceIn(0f, 1f)
                if (fraction == null && MoriTheme.materialEInkMode) {
                    MaterialEInkIndeterminateBar()
                } else if (fraction == null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            if (onCancel != null) {
                Button(onClick = onCancel) {
                    Text(text = stringResource(Res.string.btn_cancel))
                }
            }
        },
    )
}

@Composable
private fun MaterialEInkIndeterminateBar() {
    val shape = RoundedCornerShape(999.dp)
    val trackColor = MaterialTheme.colorScheme.surface
    val segmentColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .background(trackColor, shape),
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            val gap = 4.dp.toPx()
            val availableWidth = (size.width - gap * 2).coerceAtLeast(0f)
            val unit = availableWidth / 3.3f
            val firstWidth = unit
            val secondWidth = unit * 1.3f
            val thirdWidth = unit
            val radius = CornerRadius(size.height / 2)

            drawRoundRect(
                color = segmentColor,
                topLeft = Offset.Zero,
                size = Size(firstWidth, size.height),
                cornerRadius = radius,
            )
            drawRoundRect(
                color = segmentColor,
                topLeft = Offset(firstWidth + gap, 0f),
                size = Size(secondWidth, size.height),
                cornerRadius = radius,
            )
            drawRoundRect(
                color = segmentColor,
                topLeft = Offset(firstWidth + secondWidth + gap * 2, 0f),
                size = Size(thirdWidth, size.height),
                cornerRadius = radius,
            )
        }
    }
}
