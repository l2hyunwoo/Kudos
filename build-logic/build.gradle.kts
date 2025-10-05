plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.buildConfig.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("ktor") {
            id = "kudos.ktor"
            implementationClass = "io.github.l2hyunwoo.kudos.primitive.KtorPlugin"
        }
        register("kmpAndroid") {
            id = "kudos.kmp.android"
            implementationClass = "io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformAndroidPlugin"
        }
        register("kmpIos") {
            id = "kudos.kmp.ios"
            implementationClass = "io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformiOSPlugin"
        }
        register("kmpPrimitive") {
            id = "kudos.kmp"
            implementationClass = "io.github.l2hyunwoo.kudos.primitive.KotlinMultiPlatformPlugin"
        }
        register("kmpConvention") {
            id = "kudos.kotlin.multiplatform"
            implementationClass =
                "io.github.l2hyunwoo.kudos.convention.KotlinMultiPlatformConventionPlugin"
        }
        register("cmpConvention") {
            id = "kudos.compose.multiplatform"
            implementationClass =
                "io.github.l2hyunwoo.kudos.convention.ComposeMultiPlatformConventionPlugin"
        }
        register("kudosFeature") {
            id = "kudos.feature"
            implementationClass =
                "io.github.l2hyunwoo.kudos.convention.KudosFeaturePlugin"
        }
    }
}