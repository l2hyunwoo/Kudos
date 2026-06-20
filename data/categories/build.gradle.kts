plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    id("kudos.kotlin.multiplatform")
    id("kudos.ktor")
}

kotlin {
    android {
        namespace = "io.github.l2hyunwoo.data.categories"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.core.datastore)
            implementation(projects.core.network)

            implementation(libs.kotlinx.datetime)
            implementation(libs.soil.query)
            implementation(libs.androidx.datastore.preferences)
        }
    }
}

ktorfit {
    compilerPluginVersion.set("2.3.5")
}
