import java.util.Properties

plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    id("kudos.kotlin.multiplatform")
    id("kudos.compose.multiplatform")
    alias(libs.plugins.metro)
}

kotlin {
    android {
        namespace = "io.github.l2hyunwoo.kudos.core.common"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
