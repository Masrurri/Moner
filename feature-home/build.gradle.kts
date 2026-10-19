plugins {
    alias(libs.plugins.moner.android.feature)
}

android {
    namespace = "com.alviy.moner.feature.home"
}

dependencies {
    implementation(project(":api-gmail"))
    implementation(libs.google.play.services.auth)
}
