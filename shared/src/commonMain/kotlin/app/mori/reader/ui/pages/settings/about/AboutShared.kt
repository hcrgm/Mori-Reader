package app.mori.reader.ui.pages.settings.about

import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal const val SOURCE_URL = "https://github.com/hcrgm/Mori-Reader"
internal const val RELEASES_URL = "https://github.com/hcrgm/Mori-Reader/releases/latest"

internal data class LicenseItem(
    val name: String,
    val license: String,
    val url: String,
)

internal val OpenSourceLicenses =
    listOf(
        LicenseItem("Kotlin", "Apache License 2.0", "https://github.com/JetBrains/kotlin"),
        LicenseItem("Compose Multiplatform", "Apache License 2.0", "https://github.com/JetBrains/compose-multiplatform"),
        LicenseItem("AndroidX", "Apache License 2.0", "https://github.com/androidx/androidx"),
        LicenseItem("Miuix", "Apache License 2.0", "https://github.com/compose-miuix-ui/miuix"),
        LicenseItem("Koin", "Apache License 2.0", "https://github.com/InsertKoinIO/koin"),
        LicenseItem("Ktor", "Apache License 2.0", "https://github.com/ktorio/ktor"),
        LicenseItem("kotlinx.serialization", "Apache License 2.0", "https://github.com/Kotlin/kotlinx.serialization"),
        LicenseItem("Coil", "Apache License 2.0", "https://github.com/coil-kt/coil"),
        LicenseItem("Media3", "Apache License 2.0", "https://github.com/androidx/media"),
    )

@Composable
internal fun MiuixValueText(text: String) {
    Text(
        text = text,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}
