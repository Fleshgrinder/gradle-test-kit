package com.fleshgrinder.gradle.test.kit

import com.fleshgrinder.gradle.test.kit.GradleBuildStatus.*
import java.io.File
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private val NON_UNIX_LINE_SEPARATORS = Regex("(\r\n|\n\r|\r)")

class GradleBuildResult(
    val directory: File,
    val environment: Map<String, String>,
    val command: List<String>,
    val input: String,
    val timeout: Duration,
) {
    val exitCode: Int
    val output: String
    val status: GradleBuildStatus

    init {
        if (!directory.resolve("settings.gradle").exists() && !directory.resolve("settings.gradle.kts").exists()) {
            directory.resolve("settings.gradle").createNewFile()
        }

        val process = ProcessBuilder(command).apply {
            directory(directory)
            environment().apply {
                clear()
                putAll(environment)
            }
            redirectErrorStream(true)
        }.start()

        // We MUST NOT block the current thread while supplying and consuming the I/O streams of the process so that we
        // can enforce the timeout that follows.
        thread(isDaemon = true) { input.byteInputStream().transferTo(process.outputStream) }
        val output = CompletableFuture.supplyAsync { process.inputStream.readAllBytes().toString(Charsets.UTF_8) }

        try {
            status = if (process.waitFor(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
                exitCode = process.exitValue()
                if (exitCode == 0) Success else Failure
            } else {
                exitCode = -1
                Timeout
            }
            this.output = output.get().replace(NON_UNIX_LINE_SEPARATORS, "\n").trim()
        } finally {
            process.destroyForcibly()
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(this::class.java.canonicalName)
            .append("(exitCode=")
            .append(exitCode)
            .append(", status=")
            .append(status)
            .append(", timeout=")
            .append(timeout)
            .appendLine("):")

        if (environment.isNotEmpty()) {
            for ((k, v) in environment) {
                sb.append(k).append('=').append(v.escape()).appendLine(" \\")
            }
            sb.append("  ")
        }

        for (arg in command) sb.append(arg.escape()).append(" \\\n    ")
        sb.append("--project-dir=").appendLine(directory.toString().escape())

        if (input.isEmpty()) {
            sb.appendLine("    <<<''")
        } else {
            sb.appendLine("    <<'EOT'")
            sb.appendLine(input)
            sb.appendLine("'EOT'")
        }

        return sb.appendLine(output).toString()
    }

    private fun String.escape(): String =
        replace(" ", "\\ ")
}
