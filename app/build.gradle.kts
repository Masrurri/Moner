plugins {
    alias(libs.plugins.moner.android.application)
    alias(libs.plugins.moner.android.hilt)
}

android {
    namespace = "com.alviy.app.moner"

    defaultConfig {
        applicationId = "com.alviy.app.moner"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":feature-home"))
    implementation(project(":feature-budget"))
    implementation(project(":feature-settings"))
    implementation(project(":feature-receipt-gather"))

    implementation(libs.bundles.ui)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.kotlinx.coroutines)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
