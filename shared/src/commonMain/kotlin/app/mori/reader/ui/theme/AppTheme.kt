package app.mori.reader.ui.theme

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import app.mori.reader.data.settings.ThemeMode
import app.mori.reader.data.settings.UiThemeEngine
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    themeState: MoriThemeState,
    content: @Composable () -> Unit,
) {
    val darkTheme = themeState.themeMode.isDarkTheme(isSystemInDarkTheme())
    val baseDensity = LocalDensity.current
    val densityScale = themeState.uiScalePercent / 100f
    val scaledDensity =
        remember(baseDensity, densityScale) {
            Density(
                density = baseDensity.density * densityScale,
                fontScale = baseDensity.fontScale,
            )
        }

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        when (themeState.uiThemeEngine) {
            UiThemeEngine.Miuix -> {
                val controller =
                    remember(themeState.themeMode, themeState.monetEnabled, themeState.monetKeyColor, darkTheme) {
                        ThemeController(
                            colorSchemeMode = themeState.themeMode.toColorSchemeMode(themeState.monetEnabled),
                            keyColor = themeState.monetKeyColor.takeIf { it != 0L }?.let(::Color),
                            colorSpec = ThemeColorSpec.Spec2025,
                            paletteStyle = ThemePaletteStyle.TonalSpot,
                            isDark = darkTheme,
                        )
                    }

                MiuixTheme(
                    controller = controller,
                ) {
                    ProvideMoriTheme(
                        themeMode = themeState.themeMode,
                        uiThemeEngine = themeState.uiThemeEngine,
                        materialEInkMode = false,
                    ) {
                        ApplySystemBarsThemeEffect(darkTheme = darkTheme)
                        content()
                    }
                }
            }

            UiThemeEngine.Material -> {
                val materialThemeConfig =
                    if (themeState.materialEInkMode) {
                        val colorScheme = remember(darkTheme) { materialEInkColorScheme(darkTheme) }
                        remember(colorScheme) {
                            MaterialThemeConfig(
                                seedColor = colorScheme.primary,
                                colorSchemeOverride = colorScheme,
                            )
                        }
                    } else {
                        rememberMaterialThemeConfig(
                            darkTheme = darkTheme,
                            monetEnabled = themeState.monetEnabled,
                            monetKeyColor = themeState.monetKeyColor,
                        )
                    }
                val dynamicThemeState =
                    rememberDynamicMaterialThemeState(
                        isDark = darkTheme,
                        style = PaletteStyle.TonalSpot,
                        contrastLevel = -1.0,
                        specVersion = ColorSpec.SpecVersion.SPEC_2025,
                        seedColor = materialThemeConfig.seedColor,
                        modifyColorScheme =
                            materialThemeConfig.colorSchemeOverride?.let { colorSchemeOverride ->
                                { colorSchemeOverride }
                            },
                    )
                DynamicMaterialExpressiveTheme(
                    state = dynamicThemeState,
                    motionScheme = MotionScheme.expressive(),
                    animate = !themeState.materialEInkMode,
                ) {
                    CompositionLocalProvider(
                        LocalOverscrollFactory provides
                            if (themeState.materialEInkMode) {
                                null
                            } else {
                                LocalOverscrollFactory.current
                            },
                    ) {
                        ProvideMoriTheme(
                            themeMode = themeState.themeMode,
                            uiThemeEngine = themeState.uiThemeEngine,
                            materialEInkMode = themeState.materialEInkMode,
                        ) {
                            ApplySystemBarsThemeEffect(darkTheme = darkTheme)
                            content()
                        }
                    }
                }
            }
        }
    }
}

@Composable
expect fun ApplySystemBarsThemeEffect(darkTheme: Boolean)

private fun ThemeMode.isDarkTheme(systemDarkTheme: Boolean): Boolean =
    when (this) {
        ThemeMode.System -> systemDarkTheme
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

private fun ThemeMode.toColorSchemeMode(monetEnabled: Boolean): ColorSchemeMode =
    when (this) {
        ThemeMode.System -> if (monetEnabled) ColorSchemeMode.MonetSystem else ColorSchemeMode.System
        ThemeMode.Light -> if (monetEnabled) ColorSchemeMode.MonetLight else ColorSchemeMode.Light
        ThemeMode.Dark -> if (monetEnabled) ColorSchemeMode.MonetDark else ColorSchemeMode.Dark
    }
