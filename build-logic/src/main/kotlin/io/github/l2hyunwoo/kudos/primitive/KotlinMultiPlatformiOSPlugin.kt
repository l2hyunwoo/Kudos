package io.github.l2hyunwoo.kudos.primitive

import io.github.l2hyunwoo.kudos.Arch
import io.github.l2hyunwoo.kudos.activeArch
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import kotlin.collections.plus

class KotlinMultiPlatformiOSPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        extensions.configure<KotlinMultiplatformExtension> {
            when (activeArch) {
                Arch.ARM -> {
                    iosSimulatorArm64()
                    iosArm64()
                }
                Arch.ARM_SIMULATOR_DEBUG -> {
                    iosSimulatorArm64()
                }
                // Compose Multiplatform 1.11.1+ no longer publishes iosX64 artifacts;
                // on Apple Silicon the simulator runs iosSimulatorArm64, so X86_64 falls back to it.
                Arch.X86_64 -> iosSimulatorArm64()
                Arch.ALL -> {
                    iosArm64()
                    iosSimulatorArm64()
                }
            }
            targets.withType<KotlinNativeTarget> {
                compilerOptions {
                    freeCompilerArgs.add("-Xexport-kdoc")
                }
            }
        }
    }
}
