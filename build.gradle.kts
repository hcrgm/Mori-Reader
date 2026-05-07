plugins {
    alias(libs.plugins.spotless)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.hotReload) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

spotless {
    kotlin {
        target(
            "app/**/*.kt",
            "shared/**/*.kt",
        )
        targetExclude(
            "**/build/**",
            "external/**",
        )
        ktlint()
    }

    kotlinGradle {
        target(
            "*.gradle.kts",
            "app/*.gradle.kts",
            "shared/*.gradle.kts",
        )
        targetExclude(
            "**/build/**",
            "external/**",
        )
        ktlint()
    }
}
