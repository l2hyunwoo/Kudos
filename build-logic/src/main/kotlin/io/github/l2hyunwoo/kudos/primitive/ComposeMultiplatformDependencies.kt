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
                    // org.jetbrains.compose.ui:ui-tooling-preview supplies the live @Preview
                    // (androidx.compose.ui.tooling.preview.Preview). The components-ui-tooling-preview
                    // variant only re-exports a @Deprecated shim of the same symbol, so use the ui one.
                    implementation(libs.findLibrary("compose-ui-toolingPreview").get())
                    implementation(libs.findLibrary("material-icons-extended").get())
                    implementation(libs.findLibrary("material3").get())
                }
            }
            // com.android.kotlin.multiplatform.library is single-variant: there is no
            // debugImplementation configuration, so debug-only tooling goes on androidMain.
            androidMain {
                dependencies {
                    implementation(libs.findLibrary("compose-ui-tooling").get())
                }
            }
        }
    }
}
