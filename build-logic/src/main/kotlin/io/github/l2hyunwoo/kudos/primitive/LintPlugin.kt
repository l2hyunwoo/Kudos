package io.github.l2hyunwoo.kudos.primitive

import io.github.l2hyunwoo.kudos.library
import io.github.l2hyunwoo.kudos.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceTask
import org.gradle.kotlin.dsl.withType

/**
 * Applies kotlinter (ktlint engine) and registers the mrmans0n/compose-rules ktlint ruleset
 * via kotlinter's `ktlint` configuration. Idempotent so it can be applied from multiple
 * convention/primitive plugins without double-applying.
 *
 * PascalCase @Composable names are exempted from ktlint's standard:function-naming through the
 * root .editorconfig key `ktlint_function_naming_ignore_when_annotated_with = Composable`,
 * not by this ruleset jar.
 */
class LintPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            if (!pluginManager.hasPlugin("org.jmailen.kotlinter")) {
                pluginManager.apply("org.jmailen.kotlinter")
            }
            dependencies.add("ktlint", libs.library("compose-rules-ktlint"))

            // kotlinter's lint/format tasks are SourceTasks whose source is the Kotlin source set,
            // into which KSP/codegen plugins inject generated dirs under build/. Two consequences:
            //  1. Generated code would be linted (noise) -> exclude("**/build/**") drops it from
            //     the files actually checked.
            //  2. Gradle still sees the generated dir as a declared @InputFiles location produced
            //     by a codegen task (e.g. kspCommonMainKotlinMetadata) and fails validation with
            //     "implicit dependency". The exclude pattern does NOT remove the declared input
            //     root, so we must order the kotlinter task after every codegen task that writes
            //     into this module's build/ (KSP, BuildConfig, Compose resource generation).
            tasks.withType<SourceTask>().configureEach {
                if (name.startsWith("lintKotlin") || name.startsWith("formatKotlin")) {
                    // Drop generated files from what is actually linted. A pattern like
                    // "**/build/**" does NOT work here: a SourceTask matches exclude patterns
                    // relative to each source ROOT, and Compose/KSP generated dirs are themselves
                    // source roots (".../build/generated/.../kotlin"), so "build/" is not in the
                    // relative path. Filtering on the absolute path is what reliably excludes them.
                    exclude { it.file.absolutePath.contains("/build/") }
                    // The exclude above hides generated files from reports but does not remove the
                    // generated dir from the task's declared @InputFiles, so Gradle still flags an
                    // "implicit dependency" on the producing codegen task. Order after exactly
                    // those codegen tasks. Keep the matcher tight: a broad "generate*" match pulls
                    // in unrelated, sometimes-broken KMP iOS scaffolding (generateSyntheticLinkage…).
                    val codegen = tasks.matching { producer ->
                        producer.name.startsWith("ksp") ||
                            producer.name.startsWith("generateBuildConfig") ||
                            producer.name.contains("ResourceCollectors") ||
                            producer.name.contains("ComposeResourcesAccessors")
                    }
                    mustRunAfter(codegen)
                    dependsOn(codegen)
                }
            }
        }
    }
}
