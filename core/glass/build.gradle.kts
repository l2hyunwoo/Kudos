plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    id("kudos.kotlin.multiplatform")
    id("kudos.compose.multiplatform")
}

kotlin {
    android {
        namespace = "io.github.l2hyunwoo.kudos.core.glass"
    }
    sourceSets {
        // The compose convention plugin already provides runtime/foundation/ui/material3 on
        // commonMain. The vendored Cloudy blur engine only needs foundation/ui (graphics layer,
        // Modifier.Node, RenderEffect/BlurEffect) plus coroutines for the off-main CPU blur path.
        androidMain.dependencies {
            // CPU blur (API < 31) runs off the main thread; coroutines drive the capture/blur jobs.
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
