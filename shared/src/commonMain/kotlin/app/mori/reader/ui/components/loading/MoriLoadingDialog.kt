package app.mori.reader.ui.components.loading

import androidx.compose.runtime.Composable
import app.mori.reader.data.settings.UiThemeEngine
import app.mori.reader.ui.theme.MoriTheme

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
    when (MoriTheme.uiThemeEngine) {
        UiThemeEngine.Miuix ->
            MiuixLoadingDialog(
                show = show,
                state = state,
                onCancel = onCancel,
            )

        UiThemeEngine.Material ->
            MaterialLoadingDialog(
                show = show,
                state = state,
                onCancel = onCancel,
            )
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
