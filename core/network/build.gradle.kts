plugins {
    alias(libs.plugins.androidLibrary)
    id("kudos.kotlin.multiplatform")
    id("kudos.ktor")
}

kotlin {
    sourceSets {}
}

android.namespace = "io.github.l2hyunwoo.kudos.core.network"
