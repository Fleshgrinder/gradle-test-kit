import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version embeddedKotlinVersion
}

dependencies {
    api("org.junit.jupiter:junit-jupiter-api:5.9.0")
    api("io.strikt:strikt-core:0.34.1")
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjvm-default=all",
            )
        }
    }
    register<NotImplemented>("publishSnapshot")
    register<NotImplemented>("publishRelease")
}

abstract class NotImplemented : DefaultTask() {
    @TaskAction fun execute() {
        if (System.getenv("GITHUB_ACTIONS") == "true") {
            val file = buildString {
                append(".github/workflows/gradle-")
                name.forEach { c ->
                    if (c in 'A'..'Z') append('-').append(c.toLowerCase())
                    else append(c)
                }
                append(".yaml")
            }
            println("::error file=$file,title=Task $path::Not implemented!")
        } else {
            println("\u001B[31mNot implemented!\u001B[0m")
        }
    }
}
