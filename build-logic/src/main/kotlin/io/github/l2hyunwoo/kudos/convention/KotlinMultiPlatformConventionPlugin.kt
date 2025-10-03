package io.github.l2hyunwoo.kudos.convention

import io.github.l2hyunwoo.kudos.libs
import io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformAndroidPlugin
import io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformPlugin
import io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformiOSPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class KotlinMultiPlatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
        }

        apply<KotlinMultiPlatformPlugin>()
        apply<KotlinMultiPlatformAndroidPlugin>()
        apply<KotlinMultiPlatformiOSPlugin>()
    }
}