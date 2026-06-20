package io.github.l2hyunwoo.kudos.primitive

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import io.github.l2hyunwoo.kudos.libs
import io.github.l2hyunwoo.kudos.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiPlatformAndroidPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        // AGP 9's built-in Kotlin makes com.android.library incompatible with the KMP plugin,
        // so the Android target is configured via com.android.kotlin.multiplatform.library.
        plugins.withId("com.android.kotlin.multiplatform.library") {
            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
                    compileSdk = libs.version("android-compileSdk").toInt()
                    minSdk = libs.version("android-minSdk").toInt()

                    // androidResources.enable defaults to false for multiplatform libraries
                    // (true for plain com.android.library). With it off, the variant exposes no
                    // assets Sources, so the Compose plugin's CopyResourcesToAndroidAssetsTask
                    // never gets an outputDirectory and composeResources (.cvr) are dropped from
                    // the consuming APK, crashing at runtime with MissingResourceException.
                    androidResources.enable = true

                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_17)
                    }

                    packaging {
                        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
            }
        }
    }
}
