plugins {
    id("nevera.android.library")
    id("nevera.android.hilt")
}

android {
    namespace = "com.anddd.nevera.infra.ai"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(libs.coroutines.android)
    implementation(libs.timber)
    implementation(libs.play.ai.delivery)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}
