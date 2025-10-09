plugins {
    id("kudos.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.data.categories)
            implementation(projects.data.tasks)

            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.material3)
            implementation(libs.soil.query)
            implementation(libs.soil.query.compose)
            implementation(libs.rin)
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

android.namespace = "io.github.l2hyunwoo.project"
