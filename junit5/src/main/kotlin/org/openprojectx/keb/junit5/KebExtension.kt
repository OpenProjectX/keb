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
import java.nio.file.Files

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
            ),
        )
        val browser = KebBrowser.launch(config)

        try {
            store(context).put(STATE_KEY, State(browser, browser.newSession()))
        } catch (error: Throwable) {
            browser.close()
            throw error
        }
    }

    override fun afterEach(context: ExtensionContext) {
        val state = store(context).remove(STATE_KEY, State::class.java) ?: return
        val failure = context.executionException.orElse(null)

        try {
            if (failure != null) captureFailure(context, state.session)
        } finally {
            try {
                state.session.close()
            } finally {
                state.browser.close()
            }
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

    private fun captureFailure(context: ExtensionContext, session: KebSession) {
        val directory = session.config.artifactsDirectory
        Files.createDirectories(directory)
        val name = context.displayName
            .replace(Regex("""[^a-zA-Z0-9._-]+"""), "-")
            .trim('-')
            .ifBlank { "failed-test" }

        runCatching {
            session.page.screenshot(
                Page.ScreenshotOptions()
                    .setPath(directory.resolve("$name.png"))
                    .setFullPage(true),
            )
        }
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
    )

    private companion object {
        const val STATE_KEY = "keb-state"
    }
}
