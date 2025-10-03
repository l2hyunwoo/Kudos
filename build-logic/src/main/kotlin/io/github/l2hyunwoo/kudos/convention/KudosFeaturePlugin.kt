package io.github.l2hyunwoo.kudos.convention

import io.github.l2hyunwoo.kudos.library
import io.github.l2hyunwoo.kudos.libs
import io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformAndroidPlugin
import io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformPlugin
import io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformiOSPlugin
import io.github.l2hyunwoo.kudos.primitive.composeMultiplatformDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KudosFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("androidLibrary").get().get().pluginId)
            apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
            apply(libs.findPlugin("composeMultiplatform").get().get().pluginId)
            apply(libs.findPlugin("composeCompiler").get().get().pluginId)
        }

        apply<KotlinMultiPlatformPlugin>()
        apply<KotlinMultiPlatformAndroidPlugin>()
        apply<KotlinMultiPlatformiOSPlugin>()

        composeMultiplatformDependencies()

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain {
                    dependencies {
                        implementation(project(":core:designsystem"))
                        // implementation(project(":core:navigation"))
                        implementation(libs.library("androidx-navigation-compose"))
                        implementation(libs.library("androidx-lifecycle-runtimeCompose"))
                    }
                }

            }
        }
    }
}
