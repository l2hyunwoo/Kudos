import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    id("kudos.kotlin.multiplatform")
    id("kudos.compose.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.design)

            implementation(libs.soil.query)
            implementation(libs.soil.query.compose)
            implementation(libs.soil.reacty)
            implementation(libs.material3)
            implementation(libs.material3.adaptive)
            // As of Compose Multiplatform >= 1.8.2 you need to explicitly add the dependency on material-icons-core
            // https://www.jetbrains.com/help/kotlin-multiplatform-dev/whats-new-compose-180.html#implicit-dependency-on-material-icons-core-removed
            implementation(libs.material.icons.core)
        }
    }
}

android.namespace = "io.github.l2hyunwoo.kudos.core.soil"
