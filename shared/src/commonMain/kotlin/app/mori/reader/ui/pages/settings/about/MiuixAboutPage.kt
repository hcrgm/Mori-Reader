@file:OptIn(ExperimentalScrollBarApi::class)

package app.mori.reader.ui.pages.settings.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import app.mori.reader.ui.components.miuix.effect.BgEffectBackground
import app.mori.reader.ui.components.miuix.effect.ColorBlendToken
import app.mori.reader.ui.theme.isMoriDarkTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.VerticalScrollBar
import top.yukonga.miuix.kmp.basic.rememberScrollBarAdapter
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRenderEffectSupported
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.interfaces.ExperimentalScrollBarApi
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
internal fun MiuixAboutPage(
    settings: AppSettings,
    onOpenLicenses: () -> Unit,
    onBack: () -> Unit,
) {
    val topAppBarScrollBehavior = MiuixScrollBehavior()
    val lazyListState = rememberLazyListState()
    val scrollProgress by remember {
        derivedStateOf {
            when {
                lazyListState.firstVisibleItemIndex > 0 -> {
                    1f
                }

                else -> {
                    val spacer = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == AboutLogoSpacerItemKey }
                    if (spacer != null && spacer.size > 0) {
                        (lazyListState.firstVisibleItemScrollOffset.toFloat() / spacer.size).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                }
            }
        }
    }
    val backdrop = rememberAboutBackdrop(settings.appearance.blurEnabled)
    val blurActive = backdrop != null && scrollProgress == 1f
    val barColor =
        if (blurActive) {
            Color.Transparent
        } else {
            if (scrollProgress == 1f) MiuixTheme.colorScheme.surface else Color.Transparent
        }

    Scaffold(
        topBar = {
            MiuixBlurredBar(backdrop = backdrop, blurEnabled = blurActive) {
                SmallTopAppBar(
                    title = stringResource(Res.string.about_title),
                    scrollBehavior = topAppBarScrollBehavior,
                    color = barColor,
                    titleColor =
                        MiuixTheme.colorScheme.onSurface.copy(
                            alpha = ((scrollProgress - 0.35f) / 0.65f).coerceIn(0f, 1f),
                        ),
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(MiuixIcons.Back, contentDescription = stringResource(Res.string.cd_back))
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            AboutContent(
                blurEnabled = settings.appearance.blurEnabled,
                padding = innerPadding,
                topAppBarScrollBehavior = topAppBarScrollBehavior,
                lazyListState = lazyListState,
                scrollProgress = scrollProgress,
                onOpenLicenses = onOpenLicenses,
            )
        }
    }
}

@Composable
private fun AboutContent(
    blurEnabled: Boolean,
    padding: PaddingValues,
    topAppBarScrollBehavior: ScrollBehavior,
    lazyListState: LazyListState,
    scrollProgress: Float,
    onOpenLicenses: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val appInfo = rememberMoriAppInfo()
    val backdrop = rememberAboutBackdrop(blurEnabled)
    val isInDark = isMoriDarkTheme()
    val dynamicBackground = remember { isRuntimeShaderSupported() }

    val cardBlend = if (isInDark) ColorBlendToken.Overlay_Thin_Light else ColorBlendToken.Pured_Regular_Light
    val logoBlend =
        remember(isInDark) {
            if (isInDark) {
                listOf(
                    BlendColorEntry(Color(0xe6a1a1a1), BlurBlendMode.ColorDodge),
                    BlendColorEntry(Color(0x4de6e6e6), BlurBlendMode.LinearLight),
                    BlendColorEntry(Color(0xff1af500), BlurBlendMode.Lab),
                )
            } else {
                listOf(
                    BlendColorEntry(Color(0xcc4a4a4a), BlurBlendMode.ColorBurn),
                    BlendColorEntry(Color(0xff4f4f4f), BlurBlendMode.LinearLight),
                    BlendColorEntry(Color(0xff1af200), BlurBlendMode.Lab),
                )
            }
        }

    val actions =
        remember(uriHandler, onOpenLicenses) {
            listOf(
                MiuixAboutAction(
                    title = Res.string.about_view_source,
                    icon = MiuixIcons.Link,
                    endText = "GitHub",
                    onClick = { uriHandler.openUri(SOURCE_URL) },
                ),
                MiuixAboutAction(
                    title = Res.string.about_check_updates,
                    summary = Res.string.about_update_summary,
                    icon = MiuixIcons.Update,
                    endText = "Latest",
                    onClick = { uriHandler.openUri(RELEASES_URL) },
                ),
                MiuixAboutAction(
                    title = Res.string.about_open_source_licenses,
                    icon = MiuixIcons.Info,
                    onClick = onOpenLicenses,
                ),
            )
        }

    val versionCodeProgress = ((scrollProgress - 0.05f) / 0.15f).coerceIn(0f, 1f)
    val projectNameProgress = ((scrollProgress - 0.20f) / 0.15f).coerceIn(0f, 1f)
    val iconProgress = ((scrollProgress - 0.35f) / 0.15f).coerceIn(0f, 1f)
    var logoHeightDp by remember { mutableStateOf(300.dp) }
    val scrollTopPadding = padding.calculateTopPadding()
    val logoTopPadding = scrollTopPadding + 40.dp

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        BgEffectBackground(
            dynamicBackground = dynamicBackground,
            isFullSize = true,
            modifier = Modifier.fillMaxSize(),
            bgModifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier,
            alpha = { 1f - scrollProgress },
        ) {
            MiuixAboutLogoHeader(
                appName = appInfo.appName,
                versionText =
                    stringResource(
                        Res.string.about_version,
                        appInfo.versionName,
                        appInfo.versionCode,
                    ),
                backdrop = backdrop,
                logoBlend = logoBlend,
                iconProgress = iconProgress,
                projectNameProgress = projectNameProgress,
                versionCodeProgress = versionCodeProgress,
                topPadding = logoTopPadding + 52.dp,
                onHeightChanged = { heightDp -> logoHeightDp = heightDp },
            )
            LazyColumn(
                state = lazyListState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .overScrollVertical()
                        .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                contentPadding =
                    PaddingValues(
                        top = scrollTopPadding,
                    ),
            ) {
                item(key = AboutLogoSpacerItemKey) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(logoHeightDp + 52.dp + logoTopPadding - scrollTopPadding + 126.dp),
                    )
                }
                item(key = "about") {
                    Column(
                        modifier =
                            Modifier
                                .fillParentMaxHeight()
                                .padding(bottom = padding.calculateBottomPadding() + 24.dp),
                    ) {
                        MiuixAboutCard(
                            actions = actions.take(2),
                            backdrop = backdrop,
                            cardBlend = cardBlend,
                        )
                        MiuixAboutCard(
                            actions = actions.drop(2),
                            backdrop = backdrop,
                            cardBlend = cardBlend,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
            }
        }

        VerticalScrollBar(
            adapter = rememberScrollBarAdapter(lazyListState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            trackPadding = padding,
        )
    }
}

@Composable
private fun MiuixAboutLogoHeader(
    appName: String,
    versionText: String,
    backdrop: LayerBackdrop?,
    logoBlend: List<BlendColorEntry>,
    iconProgress: Float,
    projectNameProgress: Float,
    versionCodeProgress: Float,
    topPadding: androidx.compose.ui.unit.Dp,
    onHeightChanged: (androidx.compose.ui.unit.Dp) -> Unit,
) {
    val density = LocalDensity.current
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = topPadding, start = 24.dp, end = 24.dp)
                .onSizeChanged { size ->
                    with(density) { onHeightChanged(size.height.toDp()) }
                },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        clip = true
                        shape = RoundedCornerShape(24.dp)
                        alpha = 1 - iconProgress
                        scaleX = 1 - (iconProgress * 0.05f)
                        scaleY = 1 - (iconProgress * 0.05f)
                    }.background(Color(0xFF3DDC84)),
        ) {
            Image(
                modifier = Modifier.size(74.dp),
                painter = painterResource(Res.drawable.mori_app_icon),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
        }
        Text(
            modifier =
                Modifier
                    .padding(top = 12.dp, bottom = 5.dp)
                    .graphicsLayer {
                        alpha = 1 - projectNameProgress
                        scaleX = 1 - (projectNameProgress * 0.05f)
                        scaleY = 1 - (projectNameProgress * 0.05f)
                    }.then(
                        if (backdrop != null) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = RoundedCornerShape(16.dp),
                                blurRadius = 150f,
                                colors =
                                    BlurColors(
                                        blendColors = logoBlend,
                                    ),
                                contentBlendMode = BlendMode.DstIn,
                            )
                        } else {
                            Modifier
                        },
                    ),
            text = appName,
            color = MiuixTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 35.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = 1 - versionCodeProgress
                        scaleX = 1 - (versionCodeProgress * 0.05f)
                        scaleY = 1 - (versionCodeProgress * 0.05f)
                    },
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            text = versionText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MiuixAboutCard(
    actions: List<MiuixAboutAction>,
    backdrop: LayerBackdrop?,
    cardBlend: List<BlendColorEntry>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .padding(horizontal = 12.dp)
                .then(
                    if (backdrop != null) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = RoundedCornerShape(16.dp),
                            blurRadius = 60f,
                            colors =
                                BlurColors(
                                    blendColors = cardBlend,
                                ),
                        )
                    } else {
                        Modifier
                    },
                ),
        colors =
            CardDefaults.defaultColors(
                if (backdrop != null) Color.Transparent else MiuixTheme.colorScheme.surfaceContainer,
                Color.Transparent,
            ),
    ) {
        actions.forEach { action ->
            ArrowPreference(
                title = stringResource(action.title),
                summary = action.summary?.let { stringResource(it) },
                onClick = action.onClick,
                startAction = {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                },
                endActions = {
                    action.endText?.let {
                        MiuixValueText(it)
                    }
                },
            )
        }
    }
}

@Composable
private fun MiuixBlurredBar(
    backdrop: LayerBackdrop?,
    blurEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            if (blurEnabled && backdrop != null) {
                Modifier.textureBlur(
                    backdrop = backdrop,
                    shape = RectangleShape,
                    blurRadius = 25f,
                    colors =
                        BlurColors(
                            blendColors =
                                listOf(
                                    BlendColorEntry(
                                        color = MiuixTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    ),
                                ),
                        ),
                )
            } else {
                Modifier
            },
    ) {
        content()
    }
}

@Composable
private fun rememberAboutBackdrop(blurEnabled: Boolean): LayerBackdrop? {
    if (!blurEnabled || !isRenderEffectSupported()) return null

    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

private data class MiuixAboutAction(
    val title: StringResource,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val summary: StringResource? = null,
    val endText: String? = null,
)

private const val AboutLogoSpacerItemKey = "logoSpacer"
