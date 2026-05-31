package app.mori.reader.ui.components.loading

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.ui.theme.MoriTheme
import kotlinx.coroutines.delay

private const val LOADING_DIALOG_SHOW_DELAY_MILLIS = 500L

data class MoriLoadingDialogState(
    val title: String,
    val message: String? = null,
    val currentName: String? = null,
    val currentIndex: Int? = null,
    val totalCount: Int? = null,
    val fraction: Float? = null,
)

@Composable
fun MoriLoadingDialog(
    show: Boolean,
    state: MoriLoadingDialogState,
    onCancel: (() -> Unit)? = null,
) {
    var delayedShow by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            // Skip the dialog for fast loads to avoid a distracting flash.
            delay(LOADING_DIALOG_SHOW_DELAY_MILLIS)
            delayedShow = true
        } else {
            delayedShow = false
        }
    }

    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix -> {
            MiuixLoadingDialog(
                show = delayedShow,
                state = state,
                onCancel = onCancel,
            )
        }

        UiThemeEngine.Material -> {
            MaterialLoadingDialog(
                show = delayedShow,
                state = state,
                onCancel = onCancel,
            )
        }
    }
}

internal fun MoriLoadingDialogState.progressText(): String? {
    val countText =
        if (currentIndex != null && totalCount != null && totalCount > 0) {
            "${currentIndex.coerceIn(0, totalCount)} / $totalCount"
        } else {
            null
        }
    val percentText = fraction?.coerceIn(0f, 1f)?.let { "${(it * 100).toInt()}%" }
    return listOfNotNull(countText, percentText).joinToString(" · ").ifBlank { null }
}
