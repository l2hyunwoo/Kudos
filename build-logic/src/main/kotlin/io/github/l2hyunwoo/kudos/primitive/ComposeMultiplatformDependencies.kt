package io.github.l2hyunwoo.kudos.primitive

import io.github.l2hyunwoo.kudos.libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.composeMultiplatformDependencies() {
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain {
                dependencies {
                    implementation(libs.findLibrary("compose-runtime").get())
                    implementation(libs.findLibrary("compose-foundation").get())
                    implementation(libs.findLibrary("compose-ui").get())
                    implementation(libs.findLibrary("compose-components-resources").get())
                    implementation(libs.findLibrary("compose-components-uiToolingPreview").get())
                    implementation(libs.findLibrary("material-icons-extended").get())
                    implementation(libs.findLibrary("material3").get())
                }
            }
            // com.android.kotlin.multiplatform.library is single-variant: there is no
            // debugImplementation configuration, so debug-only tooling goes on androidMain.
            androidMain {
                dependencies {
                    implementation(libs.findLibrary("compose-ui-tooling").get())
                    implementation(libs.findLibrary("compose-ui-toolingPreview").get())
                }
            }
        }
    }
}
