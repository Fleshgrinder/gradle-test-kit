package com.fleshgrinder.gradle.test.kit

import org.intellij.lang.annotations.Language

@GradleBuildDsl
interface GradleBuildLayout {
    infix fun String.with(content: String)

    fun String.createFile() {
        with("")
    }

    fun String.with(content: String, extension: String) {
        (if (endsWith(extension)) this else "$this$extension") with content
    }

    fun String.java(@Language("Java") content: String = "") {
        with(content, ".java")
    }

    fun String.kt(@Language("kotlin") content: String = "") {
        with(content, ".kt")
    }

    fun buildGradle(@Language("gradle") content: String = "") {
        "".buildGradle(content)
    }

    fun String.buildGradle(@Language("gradle") content: String = "") {
        "$this/build.gradle" with content
    }

    fun settingsGradle(@Language("gradle") content: String = "") {
        "".settingsGradle(content)
    }

    fun String.settingsGradle(@Language("gradle") content: String = "") {
        "$this/settings.gradle" with content
    }

    fun buildGradleKts(@Language("gradle.kts") content: String = "") {
        "".buildGradleKts(content)
    }

    fun String.buildGradleKts(@Language("gradle.kts") content: String = "") {
        "$this/build.gradle.kts" with content
    }

    fun settingsGradleKts(@Language("gradle.kts") content: String = "") {
        "".settingsGradleKts(content)
    }

    fun String.settingsGradleKts(@Language("gradle.kts") content: String = "") {
        "$this/settings.gradle.kts" with content
    }

    fun String.properties(@Language("Properties") content: String = "") {
        with(content, ".properties")
    }

    fun gradleProperties(@Language("Properties") content: String = "") {
        "".gradleProperties(content)
    }

    fun String.gradleProperties(@Language("Properties") content: String = "") {
        "$this/gradle.properties" with content
    }
}
