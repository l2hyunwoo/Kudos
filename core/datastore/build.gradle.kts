import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    id("kudos.kotlin.multiplatform")
    alias(libs.plugins.metro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.datastore.preferences)
        }
    }
}

android.namespace = "io.github.l2hyunwoo.kudos.core.datastore"
