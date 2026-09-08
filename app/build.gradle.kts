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
    compileSdk = 37

    defaultConfig {
        applicationId = "com.pierbezuhoff.justtext"
        minSdk = 24 // Android 7.0
        targetSdk = 37
        versionCode = 5
        versionName = "1.4"
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
        resources.excludes.add("META-INF/INDEX.LIST")
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.adaptive)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)
    implementation(libs.core.splashScreen) // splash screen backport
    implementation(libs.datastore)
    implementation(libs.datastore.tink)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.serialization.json)
    implementation(libs.coil)
    implementation(libs.ktor.client.core)
//    implementation(libs.ktor.client.cio)
//    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.logging)
    implementation(libs.colormath)
    implementation(libs.colormath.compose)
    implementation(libs.slf4j.api)
    implementation(libs.logback)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.kotest.junit5)
    testImplementation(libs.kotest.assertion.core)
    testImplementation(libs.kotest.property)
}
