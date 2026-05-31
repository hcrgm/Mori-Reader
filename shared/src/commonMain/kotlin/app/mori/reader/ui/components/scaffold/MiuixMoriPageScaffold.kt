package app.mori.reader.ui.components.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import app.mori.reader.ui.theme.moriSurfaceColor
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberTopAppBarState
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
internal fun MiuixMoriPageScaffold(
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
    val useQuickReturnTopBar = useSmallTopBar && revealTopBarOnReverseScroll
    val scrollBehavior = MiuixScrollBehavior(rememberTopAppBarState())
    val quickReturnBehavior = rememberMiuixQuickReturnTopBarBehavior()
    val layoutDirection = LocalLayoutDirection.current
    val fixedStartPadding = fixedPadding.calculateStartPadding(layoutDirection)
    val fixedEndPadding = fixedPadding.calculateEndPadding(layoutDirection)

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .scrollEndHaptic(),
    ) {
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .nestedScroll(
                        if (useQuickReturnTopBar) {
                            quickReturnBehavior.nestedScrollConnection
                        } else {
                            scrollBehavior.nestedScrollConnection
                        },
                    ),
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
                        if (useQuickReturnTopBar) {
                            MiuixQuickReturnTopBar(
                                title = title,
                                subtitle = subtitle,
                                color = barColor,
                                behavior = quickReturnBehavior,
                                navigationIcon = navigationIcon,
                                actions = actions,
                                bottomContent = header,
                            )
                        } else if (useSmallTopBar) {
                            SmallTopAppBar(
                                title = title,
                                subtitle = subtitle,
                                color = barColor,
                                scrollBehavior = scrollBehavior,
                                navigationIcon = navigationIcon,
                                actions = actions,
                            )
                        } else {
                            TopAppBar(
                                title = title,
                                largeTitle = title,
                                subtitle = subtitle,
                                color = barColor,
                                scrollBehavior = scrollBehavior,
                                navigationIcon = navigationIcon,
                                actions = actions,
                            )
                        }
                        if (!useQuickReturnTopBar) {
                            header()
                        }
                    }
                }
            },
            floatingActionButton = floatingActionButton,
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
