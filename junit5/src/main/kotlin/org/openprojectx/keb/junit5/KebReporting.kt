package org.openprojectx.keb.junit5

import org.openprojectx.keb.KebSession
import java.lang.reflect.Method
import java.nio.file.Path

/** A file produced while executing a Keb test. */
public data class KebTestArtifact(
    val name: String,
    val mediaType: String,
    val extension: String,
    val path: Path,
)

/** Test state exposed to optional reporting integrations. */
public class KebTestReportContext internal constructor(
    public val id: String,
    public val displayName: String,
    public val testClass: Class<*>,
    public val testMethod: Method?,
    public val session: KebSession,
    public val artifactDirectory: Path,
) {
    private val mutableArtifacts: MutableList<KebTestArtifact> = mutableListOf()

    public val artifacts: List<KebTestArtifact>
        get() = mutableArtifacts.toList()

    public fun addArtifact(artifact: KebTestArtifact) {
        mutableArtifacts.removeAll { it.path == artifact.path }
        mutableArtifacts += artifact
    }
}

/**
 * Service-provider interface for reporting systems such as Allure.
 *
 * Implementations are discovered from `META-INF/services` when their module is
 * present on the test runtime classpath.
 */
public interface KebTestReporter {
    public fun testStarted(context: KebTestReportContext) {}

    public fun beforeSessionClose(
        context: KebTestReportContext,
        failure: Throwable?,
    ) {}

    public fun testFinished(
        context: KebTestReportContext,
        failure: Throwable?,
    ) {}
}
