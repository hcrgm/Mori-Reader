package app.mori.reader.ui.pages.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import app.mori.reader.data.settings.AppSettings
import app.mori.reader.shared.generated.resources.Res
import app.mori.reader.shared.generated.resources.cd_back
import app.mori.reader.shared.generated.resources.licenses_project_site
import app.mori.reader.shared.generated.resources.licenses_title
import app.mori.reader.ui.components.material.MaterialBackButton
import app.mori.reader.ui.components.scaffold.MoriPageScaffold
import app.mori.reader.ui.components.settings.MaterialSettingsGroup
import app.mori.reader.ui.components.settings.MaterialSettingsSurface
import app.mori.reader.ui.components.settings.materialSettingsSegmentedItemShape
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MaterialOpenSourceLicensesPage(
    settings: AppSettings,
    onBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    MoriPageScaffold(
        title = stringResource(Res.string.licenses_title),
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
                    top = paddingValues.calculateTopPadding() + 12.dp,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MaterialSettingsGroup(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                ) {
                    OpenSourceLicenses.forEachIndexed { index, item ->
                        MaterialSettingsSurface(
                            shape = materialSettingsSegmentedItemShape(index = index, count = OpenSourceLicenses.size),
                            groupedInSection = true,
                            showDivider = index > 0,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(materialSettingsSegmentedItemShape(index = index, count = OpenSourceLicenses.size)),
                            onClick = { uriHandler.openUri(item.url) },
                        ) {
                            ListItem(
                                headlineContent = { Text(text = item.name) },
                                supportingContent = { Text(text = item.license) },
                                trailingContent = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = stringResource(Res.string.licenses_project_site),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            )
                        }
                    }
                }
            }
        }
    }
}
