pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Moner"

include(":app")

// Shared foundation: base classes, design system, data layer, networking
include(":core")

// Remote APIs
include(":api-gmail")

// Screens
include(":feature-home")
include(":feature-budget")
include(":feature-settings")
include(":feature-receipt-gather")
