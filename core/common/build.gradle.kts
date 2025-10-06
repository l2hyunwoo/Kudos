import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    id("kudos.kotlin.multiplatform")
    alias(libs.plugins.metro)
}

android.namespace = "io.github.l2hyunwoo.kudos.core.common"
