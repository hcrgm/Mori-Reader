package app.mori.reader.ui.components.scaffold

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CancellationException
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun rememberMiuixQuickReturnTopBarState(
    initialHeightOffsetLimit: Float = 0f,
    initialHeightOffset: Float = 0f,
    initialContentOffset: Float = 0f,
): MiuixQuickReturnTopBarState =
    rememberSaveable(saver = MiuixQuickReturnTopBarState.Saver) {
        MiuixQuickReturnTopBarState(
            initialHeightOffsetLimit = initialHeightOffsetLimit,
            initialHeightOffset = initialHeightOffset,
            initialContentOffset = initialContentOffset,
        )
    }

@Stable
internal class MiuixQuickReturnTopBarState(
    initialHeightOffsetLimit: Float,
    initialHeightOffset: Float,
    initialContentOffset: Float,
) {
    var heightOffsetLimit by mutableFloatStateOf(initialHeightOffsetLimit)

    var heightOffset: Float
        get() = _heightOffset.floatValue
        set(newOffset) {
            val minValue = if (heightOffsetLimit <= 0f) heightOffsetLimit else 0f
            _heightOffset.floatValue = newOffset.coerceIn(minValue, 0f)
        }

    var contentOffset by mutableFloatStateOf(initialContentOffset)

    val collapsedFraction: Float
        get() =
            if (heightOffsetLimit != 0f) {
                heightOffset / heightOffsetLimit
            } else {
                0f
            }

    private var _heightOffset = mutableFloatStateOf(initialHeightOffset)

    companion object {
        val Saver =
            listSaver<MiuixQuickReturnTopBarState, Float>(
                save = { listOf(it.heightOffsetLimit, it.heightOffset, it.contentOffset) },
                restore = {
                    MiuixQuickReturnTopBarState(
                        initialHeightOffsetLimit = it[0],
                        initialHeightOffset = it[1],
                        initialContentOffset = it[2],
                    )
                },
            )
    }
}

@Stable
internal class MiuixQuickReturnTopBarBehavior(
    val state: MiuixQuickReturnTopBarState,
    private val canScroll: () -> Boolean,
    private val snapAnimationSpec: AnimationSpec<Float>?,
    private val flingAnimationSpec: DecayAnimationSpec<Float>?,
) {
    val nestedScrollConnection: NestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (!canScroll() || available.y >= 0f) return Offset.Zero
                val previousOffset = state.heightOffset
                state.heightOffset += available.y
                return Offset(0f, state.heightOffset - previousOffset)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (!canScroll()) return Offset.Zero
                state.contentOffset += consumed.y

                when {
                    consumed.y > 0f -> {
                        state.heightOffset += consumed.y
                    }

                    available.y > 0f -> {
                        val previousOffset = state.heightOffset
                        state.heightOffset += available.y
                        return Offset(0f, state.heightOffset - previousOffset)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                if (available.y > 0f || consumed.y > 0f) {
                    state.contentOffset = 0f
                }
                val superConsumed = super.onPostFling(consumed, available)
                return superConsumed + settleQuickReturnTopBar(state, available.y, flingAnimationSpec, snapAnimationSpec)
            }
        }
}

@Composable
internal fun rememberMiuixQuickReturnTopBarBehavior(
    state: MiuixQuickReturnTopBarState = rememberMiuixQuickReturnTopBarState(),
    canScroll: () -> Boolean = { true },
    snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = 2500f),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay(),
): MiuixQuickReturnTopBarBehavior =
    remember(state, canScroll, snapAnimationSpec, flingAnimationSpec) {
        MiuixQuickReturnTopBarBehavior(
            state = state,
            canScroll = canScroll,
            snapAnimationSpec = snapAnimationSpec,
            flingAnimationSpec = flingAnimationSpec,
        )
    }

@Composable
internal fun MiuixQuickReturnTopBar(
    title: String,
    subtitle: String = "",
    color: androidx.compose.ui.graphics.Color = MiuixTheme.colorScheme.surface,
    titleColor: androidx.compose.ui.graphics.Color = MiuixTheme.colorScheme.onSurface,
    subtitleColor: androidx.compose.ui.graphics.Color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    behavior: MiuixQuickReturnTopBarBehavior,
    bottomContent: @Composable () -> Unit = {},
) {
    val actionsRow =
        @Composable {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }

    Layout(
        content = {
            Box(
                Modifier
                    .layoutId("navigationIcon")
                    .padding(start = QUICK_RETURN_NAVIGATION_ICON_PADDING),
            ) {
                navigationIcon()
            }
            Box(
                Modifier
                    .layoutId("title")
                    .padding(horizontal = QUICK_RETURN_TITLE_PADDING),
            ) {
                top.yukonga.miuix.kmp.basic.Text(
                    text = title,
                    color = titleColor,
                    maxLines = 1,
                    fontSize = MiuixTheme.textStyles.title3.fontSize,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                )
            }
            Box(
                Modifier
                    .layoutId("actionIcons")
                    .padding(end = QUICK_RETURN_ACTION_ICON_PADDING),
            ) {
                actionsRow()
            }
            if (subtitle.isNotEmpty()) {
                Box(Modifier.layoutId("subtitle")) {
                    top.yukonga.miuix.kmp.basic.Text(
                        text = subtitle,
                        color = subtitleColor,
                        style = MiuixTheme.textStyles.body2,
                    )
                }
            }
            Box(Modifier.layoutId("bottomContent")) {
                bottomContent()
            }
        },
        modifier =
            Modifier
                .background(color)
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTapGestures { }
                },
    ) { measurables, constraints ->
        val navigationIconPlaceable =
            measurables
                .fastFirst { it.layoutId == "navigationIcon" }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))
        val actionIconsPlaceable =
            measurables
                .fastFirst { it.layoutId == "actionIcons" }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))
        val maxTitleWidth = constraints.maxWidth - navigationIconPlaceable.width - actionIconsPlaceable.width
        val titlePlaceable =
            measurables
                .fastFirst { it.layoutId == "title" }
                .measure(constraints.copy(minWidth = 0, maxWidth = (maxTitleWidth * 0.9f).roundToInt(), minHeight = 0))
        val subtitlePlaceable =
            measurables
                .firstOrNull { it.layoutId == "subtitle" }
                ?.measure(constraints.copy(minWidth = 0, maxWidth = (maxTitleWidth * 0.9f).roundToInt(), minHeight = 0))
        val bottomContentPlaceable =
            measurables
                .fastFirst { it.layoutId == "bottomContent" }
                .measure(constraints.copy(minWidth = 0, minHeight = 0))

        val collapsedHeight = QUICK_RETURN_COLLAPSED_HEIGHT.roundToPx()
        val verticalCenter = QUICK_RETURN_CENTER_HEIGHT.roundToPx() / 2
        val subtitleHeight = subtitlePlaceable?.height ?: 0
        val subtitleY = verticalCenter + titlePlaceable.height / 2
        val subtitleBottomPadding =
            if (subtitlePlaceable != null) {
                QUICK_RETURN_SUBTITLE_BOTTOM_PADDING.roundToPx()
            } else {
                0
            }
        val topRowHeight = maxOf(collapsedHeight, subtitleY + subtitleHeight + subtitleBottomPadding)
        if (behavior.state.heightOffsetLimit != -topRowHeight.toFloat()) {
            behavior.state.heightOffsetLimit = -topRowHeight.toFloat()
        }

        val offset = behavior.state.heightOffset.roundToInt()
        val visibleTopRowHeight = lerp(topRowHeight, 0, behavior.state.collapsedFraction)
        val layoutHeight = visibleTopRowHeight + bottomContentPlaceable.height

        layout(constraints.maxWidth, layoutHeight) {
            val headerY = offset

            navigationIconPlaceable.placeRelative(
                x = 0,
                y = headerY + verticalCenter - navigationIconPlaceable.height / 2,
            )

            var baseX = (constraints.maxWidth - titlePlaceable.width) / 2
            if (baseX < navigationIconPlaceable.width) {
                baseX += navigationIconPlaceable.width - baseX
            } else if (baseX + titlePlaceable.width > constraints.maxWidth - actionIconsPlaceable.width) {
                baseX += (constraints.maxWidth - actionIconsPlaceable.width) - (baseX + titlePlaceable.width)
            }
            titlePlaceable.placeRelative(
                x = baseX,
                y = headerY + verticalCenter - titlePlaceable.height / 2,
            )

            actionIconsPlaceable.placeRelative(
                x = constraints.maxWidth - actionIconsPlaceable.width,
                y = headerY + verticalCenter - actionIconsPlaceable.height / 2,
            )

            subtitlePlaceable?.placeRelative(
                x = (constraints.maxWidth - subtitlePlaceable.width) / 2,
                y = headerY + subtitleY,
            )

            bottomContentPlaceable.placeRelative(
                x = 0,
                y = topRowHeight + offset,
            )
        }
    }
}

private suspend fun settleQuickReturnTopBar(
    state: MiuixQuickReturnTopBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?,
): Velocity {
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }

    var remainingVelocity = velocity
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        try {
            AnimationState(initialValue = 0f, initialVelocity = velocity).animateDecay(flingAnimationSpec) {
                val delta = value - lastValue
                val initialHeightOffset = state.heightOffset
                state.heightOffset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.heightOffset)
                lastValue = value
                remainingVelocity = this.velocity
                if (abs(delta - consumed) > 0.5f) {
                    cancelAnimation()
                }
            }
        } catch (_: CancellationException) {
        }
    }

    if (snapAnimationSpec != null && state.heightOffset < 0 && state.heightOffset > state.heightOffsetLimit) {
        AnimationState(initialValue = state.heightOffset).animateTo(
            if (state.collapsedFraction < 0.5f) {
                0f
            } else {
                state.heightOffsetLimit
            },
            animationSpec = snapAnimationSpec,
        ) {
            state.heightOffset = value
        }
    }

    return Velocity(0f, velocity - remainingVelocity)
}

private val QUICK_RETURN_TITLE_PADDING = 26.dp
private val QUICK_RETURN_NAVIGATION_ICON_PADDING = 16.dp
private val QUICK_RETURN_ACTION_ICON_PADDING = 16.dp
private val QUICK_RETURN_COLLAPSED_HEIGHT = 52.dp
private val QUICK_RETURN_CENTER_HEIGHT = 50.dp
private val QUICK_RETURN_SUBTITLE_BOTTOM_PADDING = 8.dp
