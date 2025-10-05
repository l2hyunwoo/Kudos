import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    id("kudos.kotlin.multiplatform")
    id("kudos.compose.multiplatform")
    alias(libs.plugins.metro)
    alias(libs.plugins.buildConfig)
}

buildConfig {
    val fileInputStream = rootProject.file("local.properties").inputStream()
    val properties = Properties().apply { load(fileInputStream) }
    val supabaseUrl = properties.getProperty("SUPABASE_URL", "")
    val supabaseAnonKey = properties.getProperty("SUPABASE_ANON_KEY", "")

    buildConfigField("SUPABASE_URL", supabaseUrl)
    buildConfigField("SUPABASE_ANON_KEY", supabaseAnonKey)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    targets
        .filterIsInstance<KotlinNativeTarget>()
        .forEach { target ->
            target.binaries {
                framework {
                    baseName = "ComposeApp"
                    isStatic = true
                }
            }
        }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            // Core
            implementation(projects.core.datastore)
            implementation(projects.core.design)
            implementation(projects.core.network)

            // Data
            implementation(projects.data.tasks)

            // Features
            implementation(projects.feature.tasks)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
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

dependencies {
    debugImplementation(compose.uiTooling)
}

