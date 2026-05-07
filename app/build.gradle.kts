@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(25)
}

android {
    namespace = "app.mori.reader"
    compileSdk = 37

    externalNativeBuild {
        cmake {
            path = file("../external/hoshidicts-kotlin-bridge/app/src/main/cpp/CMakeLists.txt")
        }
    }

    defaultConfig {
        applicationId = "app.mori.reader"
        minSdk = 26
        targetSdk = 37
        ndk.abiFilters.add("arm64-v8a")
//        ndk.abiFilters.add("x86_64")
        versionCode = 101
        versionName = "0.1.1"
    }

    buildFeatures {
        compose = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("mori.jks")
            storePassword = "123456"
            keyAlias = "mori"
            keyPassword = "123456"
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            dependenciesInfo.includeInApk = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

base {
    archivesName.set(
        "mori" + "-v" + "0.1.1"
    )
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.miuix.blur.android)
    implementation(libs.koin.android)
}
