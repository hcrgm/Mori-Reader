package app.mori.reader.ui.components.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import app.mori.reader.ui.theme.moriSurfaceColor
import androidx.compose.material3.Scaffold as MaterialScaffold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MaterialMoriPageScaffold(
    title: String,
    subtitle: String = "",
    useSmallTopBar: Boolean = false,
    revealTopBarOnReverseScroll: Boolean = false,
    blurEnabled: Boolean,
    fixedPadding: PaddingValues = PaddingValues(),
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    header: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val backdrop = rememberMoriPageBackdrop(blurEnabled)
    val barColor = backdrop.moriPageBarColor(moriSurfaceColor())
    val scrollBehavior =
        if (revealTopBarOnReverseScroll) {
            TopAppBarDefaults.enterAlwaysScrollBehavior()
        } else {
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        }
    val layoutDirection = LocalLayoutDirection.current
    val fixedStartPadding = fixedPadding.calculateStartPadding(layoutDirection)
    val fixedEndPadding = fixedPadding.calculateEndPadding(layoutDirection)

    Box(modifier = Modifier.fillMaxSize()) {
        MaterialScaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                Box(
                    modifier = Modifier.moriPageBarBlur(backdrop),
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                start = fixedStartPadding,
                                end = fixedEndPadding,
                            ),
                    ) {
                        if (useSmallTopBar) {
                            TopAppBar(
                                title = { Text(text = title) },
                                navigationIcon = navigationIcon,
                                actions = actions,
                                windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 12.dp)),
                                colors =
                                    TopAppBarDefaults.topAppBarColors(
                                        containerColor = barColor,
                                        scrolledContainerColor = barColor,
                                    ),
                                scrollBehavior = scrollBehavior,
                            )
                        } else {
                            LargeFlexibleTopAppBar(
                                title = { Text(text = title) },
                                navigationIcon = navigationIcon,
                                actions = actions,
                                windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 12.dp)),
                                colors =
                                    TopAppBarDefaults.topAppBarColors(
                                        containerColor = barColor,
                                        scrolledContainerColor = barColor,
                                    ),
                                scrollBehavior = scrollBehavior,
                            )
                        }
                        if (subtitle.isNotEmpty()) {
                            Text(
                                text = subtitle,
                                modifier =
                                    Modifier.padding(
                                        start = 24.dp,
                                        end = 24.dp,
                                        bottom = 8.dp,
                                    ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        header()
                    }
                }
            },
            floatingActionButton = floatingActionButton,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        ) { paddingValues ->
            val pagePaddingValues =
                PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + fixedPadding.calculateBottomPadding(),
                )
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = fixedStartPadding,
                            end = fixedEndPadding,
                        ).moriPageBackdrop(backdrop),
            ) {
                content(pagePaddingValues)
            }
        }
    }
}
