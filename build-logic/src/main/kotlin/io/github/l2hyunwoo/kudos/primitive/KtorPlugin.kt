package io.github.l2hyunwoo.kudos.primitive

import io.github.l2hyunwoo.kudos.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KtorPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("ksp").get().get().pluginId)
            apply(libs.findPlugin("kotlinxSerialization").get().get().pluginId)
            apply(libs.findPlugin("ktorfit").get().get().pluginId)
        }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain {
                    dependencies {
                        implementation(libs.findLibrary("kotlinx-serialization-json").get())
                        implementation(libs.findLibrary("ktor-client-core").get())
                        implementation(libs.findLibrary("ktor-client-content-negotiation").get())
                        implementation(libs.findLibrary("ktor-serialization-kotlinx-json").get())
                        implementation(libs.findLibrary("ktor-logging").get())
                        implementation(libs.findLibrary("ktorfit").get())
                    }
                }

                androidMain {
                    dependencies {
                        implementation(libs.findLibrary("ktor-client-okhttp").get())
                        implementation("org.slf4j:slf4j-android:1.7.36")
                    }
                }

                iosMain {
                    dependencies {
                        implementation(libs.findLibrary("ktor-client-darwin").get())
                    }
                }
            }
        }
    }
}
