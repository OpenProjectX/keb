package org.openprojectx.keb.junit5

import com.microsoft.playwright.Page
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.openprojectx.keb.KebBrowser
import org.openprojectx.keb.KebConfig
import org.openprojectx.keb.KebSession
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.ServiceLoader

public class KebExtension :
    BeforeEachCallback,
    AfterEachCallback,
    ParameterResolver {

    override fun beforeEach(context: ExtensionContext) {
        val annotation = context.requiredTestClass.getAnnotation(KebTest::class.java)
        val config = KebConfig.fromSystemProperties(
            KebConfig(
                baseUrl = annotation.baseUrl.takeIf(String::isNotBlank)?.let(URI::create),
                browser = annotation.browser,
                headless = annotation.headless,
                videoMode = annotation.video,
            ),
        )
        val browser = KebBrowser.launch(config)

        try {
            val session = browser.newSession()
            val reportContext = KebTestReportContext(
                id = context.uniqueId,
                displayName = context.displayName,
                testClass = context.requiredTestClass,
                testMethod = context.testMethod.orElse(null),
                session = session,
                artifactDirectory = artifactDirectory(context, config.artifactsDirectory),
            )
            store(context).put(
                STATE_KEY,
                State(
                    browser = browser,
                    session = session,
                    report = reportContext,
                ),
            )
            reporters.forEach { it.testStarted(reportContext) }
        } catch (error: Throwable) {
            store(context).remove(STATE_KEY)
            browser.close()
            throw error
        }
    }

    override fun afterEach(context: ExtensionContext) {
        val state = store(context).remove(STATE_KEY, State::class.java) ?: return
        val failure = context.executionException.orElse(null)

        if (failure != null) captureFailure(state)

        var cleanupFailure: Throwable? = null
        fun cleanup(action: () -> Unit) {
            try {
                action()
            } catch (error: Throwable) {
                cleanupFailure?.addSuppressed(error) ?: run { cleanupFailure = error }
            }
        }
        reporters.forEach { reporter ->
            cleanup { reporter.beforeSessionClose(state.report, failure) }
        }
        cleanup {
            state.session.finish(
                failed = failure != null,
                artifactDirectory = state.report.artifactDirectory,
            ).forEachIndexed { index, path ->
                state.report.addArtifact(
                    KebTestArtifact(
                        name = "Session video ${index + 1}",
                        mediaType = "video/webm",
                        extension = "webm",
                        path = path,
                    ),
                )
            }
        }
        cleanup(state.browser::close)
        reporters.forEach { reporter ->
            cleanup { reporter.testFinished(state.report, failure) }
        }

        cleanupFailure?.let { error ->
            if (failure != null) failure.addSuppressed(error) else throw error
        }
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Boolean = parameterContext.parameter.type == KebSession::class.java

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Any = requireState(extensionContext).session

    private fun captureFailure(state: State) {
        runCatching {
            Files.createDirectories(state.report.artifactDirectory)
            val path = state.report.artifactDirectory.resolve("failure.png")
            state.session.page.screenshot(
                Page.ScreenshotOptions()
                    .setPath(path)
                    .setFullPage(true),
            )
            state.report.addArtifact(
                KebTestArtifact(
                    name = "Failure screenshot",
                    mediaType = "image/png",
                    extension = "png",
                    path = path,
                ),
            )
        }
    }

    private fun artifactDirectory(context: ExtensionContext, root: Path): Path {
        val name = context.displayName
            .replace(Regex("""[^a-zA-Z0-9._-]+"""), "-")
            .trim('-')
            .ifBlank { "test" }
            .take(100)
            .trimEnd('-', '.')
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(context.uniqueId.toByteArray(StandardCharsets.UTF_8))
            .take(4)
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
        return root.resolve("$name-$digest")
    }

    private fun requireState(context: ExtensionContext): State =
        checkNotNull(store(context).get(STATE_KEY, State::class.java)) {
            "KebSession is not available outside a running @KebTest"
        }

    private fun store(context: ExtensionContext): ExtensionContext.Store =
        context.getStore(
            ExtensionContext.Namespace.create(KebExtension::class.java, context.uniqueId),
        )

    private data class State(
        val browser: KebBrowser,
        val session: KebSession,
        val report: KebTestReportContext,
    )

    private companion object {
        const val STATE_KEY = "keb-state"

        val reporters: List<KebTestReporter> by lazy {
            ServiceLoader.load(
                KebTestReporter::class.java,
                Thread.currentThread().contextClassLoader,
            ).toList()
        }
    }
}
