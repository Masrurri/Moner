import com.alviy.moner.buildlogic.bundle
import com.alviy.moner.buildlogic.library
import com.alviy.moner.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Everything a screen module needs: Android library + Hilt + :core + the UI/lifecycle/navigation stack.
 * A new feature module's build file then only declares what is specific to it.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("moner.android.library")
        pluginManager.apply("moner.android.hilt")

        dependencies {
            add("implementation", project(":core"))

            add("implementation", libs.bundle("ui"))
            add("implementation", libs.bundle("navigation"))
            add("implementation", libs.bundle("lifecycle"))
            add("implementation", libs.library("kotlinx-coroutines"))
        }
    }
}
