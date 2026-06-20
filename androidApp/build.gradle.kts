import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Non-KMP Android application. AGP 9's built-in Kotlin makes the multiplatform plugin
// incompatible with com.android.application, so the app target lives in its own
// single-platform module and depends on :shared (a KMP library) for the shared UI/DI.
plugins {
    alias(libs.plugins.androidApplication)
    // android.builtInKotlin is disabled repo-wide, so the standalone Kotlin Android plugin
    // registers the `kotlin` extension that metro and compilerOptions rely on. The Kotlin
    // Gradle plugin is already on the build classpath (KMP modules), so apply it without a
    // version to avoid a "plugin already on the classpath" resolution conflict.
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.metro)
}

android {
    namespace = "io.github.l2hyunwoo.kudos"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.l2hyunwoo.kudos"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // App() in :shared is a context-parameter @Composable; calling it from MainActivity
        // requires this flag (context parameters are still gated in Kotlin 2.4).
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
    implementation(compose.runtime)
    debugImplementation(compose.uiTooling)
    implementation(compose.preview)
}
