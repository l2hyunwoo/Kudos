plugins {
    id("kudos.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.data.tasks)

            implementation(libs.soil.query)
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
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

android.namespace = "io.github.l2hyunwoo.tasks"