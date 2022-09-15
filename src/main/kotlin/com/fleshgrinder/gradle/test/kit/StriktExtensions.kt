package com.fleshgrinder.gradle.test.kit

import com.fleshgrinder.gradle.test.kit.GradleBuildStatus.*
import strikt.api.Assertion.Builder
import strikt.api.expectThat
import strikt.assertions.isEqualTo

/**
 * Asserts that the build times out.
 *
 * A times out if the [GradleBuildResult.status] is set to [Timeout]. It is set
 * to [Timeout] whenever the process is forcefully killed by the
 * [GradleBuildResult] implementation (the code is set to -1).
 */
fun GradleBuild.timesOut(): Builder<GradleBuildResult> =
    expectThat(execute()) and { get { status } isEqualTo Timeout }

/**
 * Asserts that the build fails.
 *
 * A build failed if the [GradleBuildResult.status] is set to [Failure]. It is
 * set to [Failure] whenever Gradle exits with a code greater zero.
 */
fun GradleBuild.isFailure(): Builder<GradleBuildResult> =
    expectThat(execute()) and { get { status } isEqualTo Failure }

/**
 * Asserts that the build succeeds.
 *
 * A build succeeds if the [GradleBuildResult.status] is set to [Success]. It is
 * set to [Success] whenever Gradle exits with a code of zero.
 */
fun GradleBuild.isSuccess(): Builder<GradleBuildResult> =
    expectThat(execute()) and { get { status } isEqualTo Success }

/**
 * Prints the output of the Gradle build to the given [sink], this is mostly useful for debugging.
 */
fun Builder<GradleBuildResult>.print(sink: Appendable = System.err): Builder<GradleBuildResult> =
    apply { sink.append(subject.output) }
