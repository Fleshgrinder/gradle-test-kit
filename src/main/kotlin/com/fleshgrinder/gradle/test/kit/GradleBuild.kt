package com.fleshgrinder.gradle.test.kit

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

inline fun GradleBuild(action: GradleBuild.() -> Unit): GradleBuild =
    GradleBuild().apply(action)

/**
 * > Mutation of repositories declared in settings is only allowed during settings evaluation
 */
@GradleBuildDsl
class GradleBuild : GradleBuildLayout {
    private val arguments: MutableList<String> = mutableListOf()
    private var buildCache = false
    private var daemon = false
    private var distributionPath: Path? = null
    private val environment: MutableMap<String, String> = mutableMapOf()
    @PublishedApi internal val input = StringBuilder()
    private val files = mutableMapOf<String, StringBuilder>()
    private var name: String = "test"
    private var timeout: Duration = Duration.ofMinutes(1)
    private var workingDirectory: Path? = null

    override fun String.with(content: String) {
        files.getOrPut(this, ::StringBuilder).append(content)
    }

    fun withArguments(argument: Any?) = apply {
        argument?.toString()?.let(arguments::add)
    }

    fun withArguments(argument: Any?, vararg arguments: Any?) = apply {
        withArguments(argument)
        for (it in arguments) withArguments(it)
    }

    fun withBuildCache() = apply {
        buildCache = true
    }

    fun withDaemon() = apply {
        daemon = true
    }

    fun withDistributionPath(distributionPath: File) =
        withDistributionPath(distributionPath.toPath())

    fun withDistributionPath(distributionPath: Path) = apply {
        this.distributionPath = distributionPath
    }

    fun withEnvironment(name: String, value: Any?) = apply {
        environment[name] = value?.toString() ?: ""
    }

    fun withEnvironment(variable: Pair<String, Any?>) =
        withEnvironment(variable.first, variable.second)

    fun withEnvironment(variable: Pair<String, Any?>, vararg variables: Pair<String, Any?>) = apply {
        withEnvironment(variable)
        for (it in variables) withEnvironment(it)
    }

    fun withEnvironment(variables: Map<String, Any?>) = apply {
        for (entry in variables.entries) withEnvironment(entry.key, entry.value)
    }

    fun withGradleProperty(name: Any, value: Any?) = apply {
        withArguments("-P$name=${value ?: ""}")
    }

    fun withGradleProperties(gradleProperty: Pair<Any, Any?>) =
        withGradleProperty(gradleProperty.first, gradleProperty.second)

    fun withGradleProperties(gradleProperty: Pair<Any, Any?>, vararg gradleProperties: Pair<Any, Any?>) = apply {
        withGradleProperties(gradleProperty)
        for (it in gradleProperties) withGradleProperties(it)
    }

    fun withGradleProperties(gradleProperties: Map<Any, Any?>) = apply {
        for (it in gradleProperties.entries) withGradleProperty(it.key, it.value)
    }

    fun withInput(line: Any?) = apply {
        input.append(line)
    }

    fun withInput(line: Any?, vararg lines: Any?) = apply {
        withInput(line)
        for (it in lines) withInput(it)
    }

    inline fun withInput(action: Appendable.() -> Unit) = apply {
        input.apply(action)
    }

    inline fun withLayout(action: GradleBuildLayout.() -> Unit) = apply {
        apply(action)
    }

    fun withName(name: String) = apply {
        this.name = name
    }

    fun withSilentOutput() = apply {
        withArguments("--quiet")
        withGradleProperty("org.gradle.welcome", "never")
    }

    fun withSystemProperty(name: Any, value: Any?) = apply {
        withArguments("-D$name=${value ?: ""}")
    }

    fun withSystemProperties(systemProperty: Pair<Any, Any?>) =
        withSystemProperty(systemProperty.first, systemProperty.second)

    fun withSystemProperties(systemProperty: Pair<Any, Any?>, vararg systemProperties: Pair<Any, Any?>) = apply {
        withSystemProperties(systemProperty)
        for (it in systemProperties) withSystemProperties(it)
    }

    fun withSystemProperties(systemProperties: Map<Any, Any?>) = apply {
        for (it in systemProperties.entries) withSystemProperty(it.key, it.value)
    }

    fun withTimeout(timeout: Duration) = apply {
        this.timeout = timeout
    }

    fun withTimeout(seconds: Long, nanos: Long = 0) =
        withTimeout(Duration.ofSeconds(seconds, nanos))

    fun withWorkingDirectory(workingDirectory: File) =
        withWorkingDirectory(workingDirectory.toPath())

    fun withWorkingDirectory(workingDirectory: Path) = apply {
        this.workingDirectory = workingDirectory
    }

    @Synchronized
    fun execute(): GradleBuildResult {
        val projectDirectory = Files.createDirectories((workingDirectory ?: Files.createTempDirectory("gradle-build")).resolve(name))
        files.forEach { (file, content) ->
            projectDirectory.resolve(file.removePrefix("/")).also {
                Files.createDirectories(it.parent)
                Files.writeString(it, content)
            }
        }

        val environment = HashMap<String, String>(environment.size).apply {
            this["JAVA_HOME"] = System.getProperty("java.home")
            environment.forEach(::put)
        }

        val arguments = ArrayList(arguments).apply {
            val ext = if (File.separatorChar == '/') "" else ".bat"
            add(0, (distributionPath ?: Path.of(System.getProperty("org.gradle.distribution.path"))).resolve("bin/gradle$ext").toRealPath().toString())
            if (!buildCache) add("--no-build-cache")
            if (!daemon) add("--no-daemon")
        }

        return GradleBuildResult(projectDirectory.toFile(), environment, arguments, input.toString(), timeout)
    }
}
