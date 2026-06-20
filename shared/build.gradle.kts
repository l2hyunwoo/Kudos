import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    id("kudos.kotlin.multiplatform")
    id("kudos.compose.multiplatform")
    alias(libs.plugins.buildConfig)
}

buildConfig {
    // Pin the package so the generated class is io.github.l2hyunwoo.kudos.BuildConfig,
    // the same package as IosAppGraph.kt (no import needed). Without this the gmazzo
    // plugin derives it from project.group ("Kudos.shared"), which would change the import.
    packageName("io.github.l2hyunwoo.kudos")

    val fileInputStream = rootProject.file("local.properties").inputStream()
    val properties = Properties().apply { load(fileInputStream) }
    val supabaseUrl = properties.getProperty("SUPABASE_URL", "")
    val supabaseAnonKey = properties.getProperty("SUPABASE_ANON_KEY", "")

    buildConfigField("SUPABASE_URL", supabaseUrl)
    buildConfigField("SUPABASE_ANON_KEY", supabaseAnonKey)
}

kotlin {
    android {
        namespace = "io.github.l2hyunwoo.kudos.shared"
    }

    // Keep baseName "ComposeApp" so the iOS Swift side keeps `import ComposeApp` and
    // the existing pbxproj framework wiring unchanged (the module rename is Stage 5).
    targets
        .filterIsInstance<KotlinNativeTarget>()
        .forEach { target ->
            target.binaries {
                framework {
                    baseName = "ComposeApp"
                    isStatic = true
                }
            }
        }

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.common)
            implementation(projects.core.datastore)
            implementation(projects.core.design)
            implementation(projects.core.network)

            // Data
            implementation(projects.data.categories)
            implementation(projects.data.tasks)

            // Features
            // The feature *Context.Factory types are supertypes of the public AppGraph interface,
            // so they must be `api` for the :androidApp MainActivity that uses AppGraph as a
            // context receiver (Gradle api/impl rule). feature.main is only used inside App()'s
            // body, so it stays `implementation`.
            api(projects.feature.category)
            implementation(projects.feature.main)
            api(projects.feature.project)
            api(projects.feature.tasks)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.soil.query)
            implementation(libs.soil.query.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
