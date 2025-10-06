plugins {
    alias(libs.plugins.androidLibrary)
    id("kudos.kotlin.multiplatform")
    id("kudos.ktor")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.datastore)
            implementation(projects.core.network)

            implementation(libs.soil.query)
            implementation(libs.androidx.datastore.preferences)
        }
    }
}

android.namespace = "io.github.l2hyunwoo.data.tasks"