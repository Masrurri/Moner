plugins {
    alias(libs.plugins.moner.android.library)
    alias(libs.plugins.moner.android.hilt)
    alias(libs.plugins.moner.android.room)
}

android {
    namespace = "com.alviy.moner.core"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.bundles.ui)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.networking)
    implementation(libs.kotlinx.coroutines)
}
