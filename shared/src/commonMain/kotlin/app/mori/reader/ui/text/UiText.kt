package app.mori.reader.ui.text

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class Plain(
        val value: String,
    ) : UiText

    data class Key(
        val resource: StringResource,
        val args: List<Any> = emptyList(),
    ) : UiText
}

fun uiText(
    resource: StringResource,
    vararg args: Any,
): UiText = UiText.Key(resource, args.toList())

fun Throwable.uiTextOr(resource: StringResource): UiText = message?.let(UiText::Plain) ?: UiText.Key(resource)

@Composable
fun UiText.asString(): String =
    when (this) {
        is UiText.Plain -> value
        is UiText.Key -> stringResource(resource, *args.toTypedArray())
    }

suspend fun UiText.resolveString(): String =
    when (this) {
        is UiText.Plain -> value
        is UiText.Key -> getString(resource, *args.toTypedArray())
    }
