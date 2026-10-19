plugins {
    alias(libs.plugins.moner.android.feature)
}

android {
    namespace = "com.alviy.moner.feature.receipt.gather"
}

dependencies {
    implementation(project(":api-gmail"))
}
