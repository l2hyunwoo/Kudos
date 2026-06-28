dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // kotlinter-gradle is published only to the Gradle Plugin Portal, not Maven Central.
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
