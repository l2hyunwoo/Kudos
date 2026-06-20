plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    id("kudos.kotlin.multiplatform")
    id("kudos.compose.multiplatform")
}

kotlin {
    android {
        namespace = "io.github.l2hyunwoo.kudos.core.designsystem"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.material3)
            implementation(libs.material.icons.extended)

            implementation(libs.coil)
            implementation(libs.coil.network)
        }
        appleMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }
}
