package io.github.l2hyunwoo.kudos

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.FileInputStream
import java.util.Properties

internal val ExtensionContainer.libs: VersionCatalog
    get() = getByType<VersionCatalogsExtension>().named("libs")

internal val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal val Project.applicationExtension: CommonExtension<*, *, *, *, *, *>
    get() = extensions.getByType<ApplicationExtension>()

internal val Project.libraryExtension: CommonExtension<*, *, *, *, *, *>
    get() = extensions.getByType<LibraryExtension>()

internal val Project.androidExtension: CommonExtension<*, *, *, *, *, *>
    get() = runCatching { libraryExtension }
        .recoverCatching { applicationExtension }
        .onFailure { println("Could not find Library or Application extension from this project") }
        .getOrThrow()

internal fun VersionCatalog.version(name: String): String {
    return findVersion(name).get().requiredVersion
}

internal fun VersionCatalog.library(name: String): MinimalExternalModuleDependency {
    return findLibrary(name).get().get()
}

internal fun VersionCatalog.plugin(name: String): PluginDependency {
    return findPlugin(name).get().get()
}

internal fun VersionCatalog.bundle(name: String): ExternalModuleDependencyBundle {
    return findBundle(name).get().get()
}

internal fun Project.kotlin(action: KotlinMultiplatformExtension.() -> Unit) {
    extensions.configure(action)
}

/**
 * Get property from local.properties file
 */
internal fun Project.getLocalProperty(key: String, defaultValue: String = ""): String {
    val localPropertiesFile = rootProject.file("local.properties")
    if (!localPropertiesFile.exists()) {
        return defaultValue
    }

    val properties = Properties()
    FileInputStream(localPropertiesFile).use { properties.load(it) }
    return properties.getProperty(key, defaultValue)
}

/**
 * Get all local properties
 */
internal fun Project.getLocalProperties(): Properties {
    val localPropertiesFile = rootProject.file("local.properties")
    val properties = Properties()

    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use { properties.load(it) }
    }

    return properties
}
