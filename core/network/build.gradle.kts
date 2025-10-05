plugins {
    alias(libs.plugins.androidLibrary)
    id("kudos.feature")
    id("kudos.ktor")
}

kotlin {
    sourceSets {}
}

android.namespace = "io.github.l2hyunwoo.kudos.core.network"
