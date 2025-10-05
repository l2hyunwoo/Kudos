import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    id("kudos.kotlin.multiplatform")
    alias(libs.plugins.metro)
    id("kudos.ktor")
    alias(libs.plugins.buildConfig)
}

buildConfig {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { localProperties.load(it) }
    }

    buildConfigField("SUPABASE_URL", localProperties.getProperty("SUPABASE_URL", ""))
    buildConfigField("SUPABASE_ANON_KEY", localProperties.getProperty("SUPABASE_ANON_KEY", ""))
}

kotlin {
    sourceSets {}
}

android.namespace = "io.github.l2hyunwoo.kudos.core.network"
