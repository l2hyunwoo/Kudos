plugins {
    id("kudos.feature")
    id("kudos.ktor")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.network)
        }
    }
}

android.namespace = "io.github.l2hyunwoo.data.tasks"