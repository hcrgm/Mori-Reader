package app.mori.reader

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual fun platform(): String = "Android"

@Composable
actual fun rememberMoriAppInfo(): MoriAppInfo {
    val context = LocalContext.current
    return remember(context) {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        MoriAppInfo(
            appName = appName,
            versionName = packageInfo.versionName.orEmpty(),
            versionCode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
            },
        )
    }
}
