plugins {
    id("kudos.feature")
}

kotlin {
    android {
        namespace = "io.github.l2hyunwoo.main"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.common)
            implementation(projects.feature.tasks)
            implementation(projects.feature.category)
            implementation(projects.data.categories)
            implementation(projects.data.tasks)

            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.datetime)
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
