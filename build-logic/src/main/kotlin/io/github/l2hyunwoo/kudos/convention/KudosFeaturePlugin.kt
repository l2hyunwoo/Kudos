package io.github.l2hyunwoo.kudos.convention

import com.github.gmazzo.buildconfig.BuildConfigExtension
import io.github.l2hyunwoo.kudos.getLocalProperty
import io.github.l2hyunwoo.kudos.library
import io.github.l2hyunwoo.kudos.libs
import io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformAndroidPlugin
import io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformPlugin
import io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformiOSPlugin
import io.github.l2hyunwoo.kudos.primitive.composeMultiplatformDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.buildConfigField
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KudosFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("androidLibrary").get().get().pluginId)
            apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
            apply(libs.findPlugin("composeMultiplatform").get().get().pluginId)
            apply(libs.findPlugin("composeCompiler").get().get().pluginId)
            apply(libs.findPlugin("metro").get().get().pluginId)
            apply(libs.findPlugin("buildConfig").get().get().pluginId)
        }

        apply<KotlinMultiPlatformPlugin>()
        apply<KotlinMultiPlatformAndroidPlugin>()
        apply<KotlinMultiPlatformiOSPlugin>()

        composeMultiplatformDependencies()

        // Configure BuildConfig
        extensions.configure<BuildConfigExtension> {
            val supabaseUrl = getLocalProperty("SUPABASE_URL", "")
            val supabaseAnonKey = getLocalProperty("SUPABASE_ANON_KEY", "")

            buildConfigField("SUPABASE_URL", supabaseUrl)
            buildConfigField("SUPABASE_ANON_KEY", supabaseAnonKey)
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain {
                    dependencies {
                        implementation(project(":core:design"))
                        // implementation(project(":core:navigation"))
                        implementation(libs.library("androidx-navigation-compose"))
                        implementation(libs.library("androidx-lifecycle-runtimeCompose"))
                    }
                }

            }
        }
    }
}
