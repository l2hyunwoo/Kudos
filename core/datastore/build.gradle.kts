import java.util.Properties

plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    id("kudos.kotlin.multiplatform")
    alias(libs.plugins.metro)
}

kotlin {
    android {
        namespace = "io.github.l2hyunwoo.kudos.core.datastore"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.datastore.preferences)
        }
    }
}
