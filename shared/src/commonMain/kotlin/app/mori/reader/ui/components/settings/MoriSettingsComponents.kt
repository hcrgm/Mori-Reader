package app.mori.reader.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_close
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

val MoriSettingsHorizontalPadding: Dp = 12.dp

@Composable
fun MoriSettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        SmallTitle(text = title)
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MoriSettingsHorizontalPadding),
            content = content,
        )
    }
}

@Composable
fun MoriIconArrowPreference(
    title: String,
    summary: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    startAction: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    ArrowPreference(
        title = title,
        summary = summary,
        onClick = onClick,
        modifier = modifier,
        startAction = startAction,
        enabled = enabled,
    )
}

@Composable
fun MoriWarningCard(
    text: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    containerColor: Color? = null,
    textColor: Color? = null,
    dismissTint: Color? = null,
) {
    val colors = MiuixTheme.colorScheme
    val resolvedContainerColor = containerColor ?: colors.errorContainer
    val resolvedTextColor = textColor ?: colors.onErrorContainer
    val resolvedDismissTint = dismissTint ?: resolvedTextColor
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.defaultColors(
                color = resolvedContainerColor,
                contentColor = colors.onSurface,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        top = 12.dp,
                        end = if (onDismiss != null) 4.dp else 16.dp,
                        bottom = 12.dp,
                    ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = resolvedTextColor,
                fontSize = 14.sp,
            )
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(30.dp),
                ) {
                    Icon(
                        imageVector = MiuixIcons.Close,
                        contentDescription = stringResource(Res.string.cd_close),
                        modifier = Modifier.size(16.dp),
                        tint = resolvedDismissTint,
                    )
                }
            }
        }
    }
}

@Composable
fun MoriInfoCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = MoriSettingsHorizontalPadding),
        colors =
            CardDefaults.defaultColors(
                color = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                contentColor = MiuixTheme.colorScheme.onSurface,
            ),
    ) {
        Text(
            text = text,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            color = MiuixTheme.colorScheme.primary,
        )
    }
}
