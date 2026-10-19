plugins {
    alias(libs.plugins.moner.android.library)
    alias(libs.plugins.moner.android.hilt)
}

android {
    namespace = "com.alviy.moner.api.gmail"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.bundles.networking)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.google.play.services.auth)
}
