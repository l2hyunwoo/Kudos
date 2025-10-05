plugins {
    id("kudos.feature")
    id("kudos.ktor")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
        }

        commonTest {
            dependencies {
            }
        }

        androidMain {
            dependencies {
            }
        }
        iosMain {
            dependencies {
            }
        }
    }
}

android.namespace = "io.github.l2hyunwoo.data.tasks"