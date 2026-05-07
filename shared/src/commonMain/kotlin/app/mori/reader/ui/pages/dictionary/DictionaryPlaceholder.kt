package app.mori.reader.ui.pages.dictionary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.dict_placeholder
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun DictionaryPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(Res.string.dict_placeholder),
            style = MiuixTheme.textStyles.title3,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
    }
}
