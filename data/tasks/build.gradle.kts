plugins {
    alias(libs.plugins.androidLibrary)
    id("kudos.kotlin.multiplatform")
    id("kudos.ktor")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.network)
        }
    }
}

android.namespace = "io.github.l2hyunwoo.data.tasks"