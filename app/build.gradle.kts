import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

android {
    namespace = "com.pierbezuhoff.justtext"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pierbezuhoff.justtext"
        minSdk = 24 // Android 7.0
        targetSdk = 36
        versionCode = 4
        versionName = "1.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    lint {
        // otherwise linter crashes during signed release build...
        disable += "NullSafeMutableLiveData"
    }
    packaging {
        resources.pickFirsts.add("META-INF/INDEX.LIST")
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material3.adaptive)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.core.ktx)
    implementation(libs.core.splashScreen) // splash screen backport
    implementation(libs.datastore)
    implementation(libs.datastore.tink)
    implementation(libs.serialization.json)
    implementation(libs.coil)
    implementation(libs.ktor.client.core)
//    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.android)
//    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.colormath)
    implementation(libs.colormath.compose)
    implementation(libs.slf4j.api)
    implementation(libs.logback)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    testImplementation(libs.kotest.junit5)
    testImplementation(libs.kotest.assertion.core)
    testImplementation(libs.kotest.property)
}
