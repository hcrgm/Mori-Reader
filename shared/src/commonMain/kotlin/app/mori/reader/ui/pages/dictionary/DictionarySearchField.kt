package app.mori.reader.ui.pages.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_clear_search
import app.mori.reader.shared.generated.resources.dict_search_hint
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.basic.SearchCleanup
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val DictionarySearchFieldHeight = 56.dp
internal val DictionarySearchFieldContentGap = 14.dp

@Composable
internal fun DictionarySearchField(
    query: String,
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(DictionarySearchFieldHeight)
                .then(
                    if (blurEnabled && backdrop != null) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = RoundedCornerShape(percent = 50),
                            blurRadius = 25f,
                            colors =
                                BlurColors(
                                    blendColors =
                                        listOf(
                                            BlendColorEntry(
                                                color =
                                                    MiuixTheme.colorScheme.surfaceContainer.copy(
                                                        0.8f,
                                                    ),
                                            ),
                                        ),
                                ),
                        )
                    } else {
                        Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f))
                    },
                ).padding(start = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = MiuixIcons.Basic.Search,
            contentDescription = null,
            modifier =
                Modifier
                    .size(44.dp)
                    .padding(start = 16.dp, end = 8.dp),
            tint = MiuixTheme.colorScheme.onSurface,
        )

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions =
                KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus(force = true)
                        onSearch()
                    },
                ),
            textStyle =
                MiuixTheme.textStyles.main.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    color = MiuixTheme.colorScheme.onSurface,
                ),
            cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.dict_search_hint),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
                innerTextField()
            },
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = onClear,
            ) {
                Icon(
                    imageVector = MiuixIcons.Basic.SearchCleanup,
                    contentDescription = stringResource(Res.string.cd_clear_search),
                )
            }
        }
    }
}
