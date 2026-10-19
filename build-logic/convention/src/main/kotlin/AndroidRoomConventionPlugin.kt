import com.alviy.moner.buildlogic.bundle
import com.alviy.moner.buildlogic.library
import com.alviy.moner.buildlogic.libs
import com.android.build.gradle.LibraryExtension
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.process.CommandLineArgumentProvider
import java.io.File

/**
 * Room + KSP. Exports the schema of every database version to `<module>/schemas` (commit these:
 * they are the baseline for writing and testing migrations) and ships them as androidTest
 * assets so `MigrationTestHelper` can open older versions.
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")

        val schemaDir = file("schemas").apply { mkdirs() }

        extensions.configure<KspExtension> {
            arg(RoomSchemaArgProvider(schemaDir))
        }

        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> {
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                sourceSets.getByName("androidTest").assets.srcDir(schemaDir)
            }
        }

        dependencies {
            add("implementation", libs.bundle("room"))
            add("ksp", libs.library("room-compiler"))
            add("androidTestImplementation", libs.library("room-testing"))
            add("androidTestImplementation", libs.library("androidx-junit"))
            add("androidTestImplementation", libs.library("androidx-test-runner"))
        }
    }
}

/** Declares the schema directory as a task input so KSP output stays cacheable. */
private class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File,
) : CommandLineArgumentProvider {
    override fun asArguments() = listOf("room.schemaLocation=${schemaDir.path}")
}
