package app.mori.reader.ui.theme

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun ApplySystemBarsThemeEffect(darkTheme: Boolean) {
    val view = LocalView.current
    val activity = view.context.findActivity() ?: return

    DisposableEffect(activity, darkTheme) {
        activity.enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.auto(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                ) {
                    darkTheme
                },
            navigationBarStyle =
                SystemBarStyle.auto(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                ) {
                    darkTheme
                },
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }

        onDispose { }
    }
}

private tailrec fun Context.findActivity(): ComponentActivity? =
    when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
