package app.mori.reader.ui.pages.settings.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.rememberMoriAppInfo
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.about_check_updates
import app.mori.reader.shared.generated.resources.about_open_source_licenses
import app.mori.reader.shared.generated.resources.about_title
import app.mori.reader.shared.generated.resources.about_update_summary
import app.mori.reader.shared.generated.resources.about_version
import app.mori.reader.shared.generated.resources.about_view_source
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.mori_app_icon
import app.mori.reader.ui.components.material.MaterialBackButton
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MaterialSettingsGroup
import app.mori.reader.ui.components.settings.MaterialSettingsSurface
import app.mori.reader.ui.components.settings.materialSettingsSegmentedItemShape
import app.mori.reader.ui.theme.MoriTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MaterialAboutPage(
    settings: AppSettings,
    onOpenLicenses: () -> Unit,
    onBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val appInfo = rememberMoriAppInfo()
    val actions =
        listOf(
            AboutAction(
                title = stringResource(Res.string.about_view_source),
                icon = Icons.Rounded.Code,
                onClick = { uriHandler.openUri(SOURCE_URL) },
            ),
            AboutAction(
                title = stringResource(Res.string.about_check_updates),
                summary = stringResource(Res.string.about_update_summary),
                icon = Icons.Rounded.SystemUpdate,
                onClick = { uriHandler.openUri(RELEASES_URL) },
            ),
            AboutAction(
                title = stringResource(Res.string.about_open_source_licenses),
                icon = Icons.AutoMirrored.Rounded.Article,
                onClick = onOpenLicenses,
            ),
        )

    MoriPageScaffold(
        title = stringResource(Res.string.about_title),
        blurEnabled = settings.appearance.blurEnabled,
        navigationIcon = {
            Row {
                MaterialBackButton(onClick = onBack, contentDescription = stringResource(Res.string.cd_back))
                Spacer(modifier = Modifier.size(16.dp))
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = paddingValues.calculateTopPadding() + 20.dp,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                MaterialAppIdentityHeader(
                    appName = appInfo.appName,
                    versionText =
                        stringResource(
                            Res.string.about_version,
                            appInfo.versionName,
                            appInfo.versionCode,
                        ),
                )
            }
            item {
                MaterialAboutActionCard(
                    actions = actions,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun MaterialAppIdentityHeader(
    appName: String,
    versionText: String,
) {
    val iconBackgroundColor =
        if (MoriTheme.materialEInkMode) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            Color(0xFF3DDC84)
        }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(iconBackgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.mori_app_icon),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = appName,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = versionText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MaterialAboutActionCard(
    actions: List<AboutAction>,
    modifier: Modifier = Modifier,
) {
    MaterialSettingsGroup(modifier = modifier) {
        actions.forEachIndexed { index, action ->
            MaterialAboutActionRow(
                action = action,
                shape = materialSettingsSegmentedItemShape(index = index, count = actions.size),
                showDivider = index > 0,
            )
        }
    }
}

@Composable
private fun MaterialAboutActionRow(
    action: AboutAction,
    shape: Shape,
    showDivider: Boolean,
) {
    MaterialSettingsSurface(
        shape = shape,
        groupedInSection = true,
        showDivider = showDivider,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape),
        onClick = action.onClick,
    ) {
        ListItem(
            headlineContent = { Text(text = action.title) },
            supportingContent = action.summary?.let { { Text(text = it) } },
            leadingContent = {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}

private data class AboutAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val summary: String? = null,
)
