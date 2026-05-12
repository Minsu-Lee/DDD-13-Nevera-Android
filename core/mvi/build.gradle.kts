plugins {
    id("nevera.android.library")
}

android {
    namespace = "com.anddd.nevera.core.mvi"
}

dependencies {
    api(libs.orbit.core)
    api(libs.orbit.viewmodel)
    implementation(libs.timber)
}
