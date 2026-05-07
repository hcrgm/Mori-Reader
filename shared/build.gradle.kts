@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(25)

    android {
        androidResources.enable = true
        compileSdk {
            version = release(37)
        }
        minSdk = 26
        namespace = "app.mori.reader.shared"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.ui)
            api(libs.compose.components.resources)

            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.miuix.ui)
            implementation(libs.miuix.icons)
            implementation(libs.miuix.navigation3.ui)
            implementation(libs.miuix.blur)
            implementation(libs.miuix.preference)
            implementation(libs.reorderable)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        androidMain.dependencies {
            implementation("androidx.appcompat:appcompat:1.7.0")
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.coil.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
        }
        androidMain {
            kotlin.srcDir("../external/hoshidicts-kotlin-bridge/app/src/main/java")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "app.mori.reader.shared.generated.resources"
}
