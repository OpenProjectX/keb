package org.openprojectx.keb.allure

import com.microsoft.playwright.ConsoleMessage
import com.microsoft.playwright.Tracing
import io.qameta.allure.Allure
import org.openprojectx.keb.junit5.KebTestArtifact
import org.openprojectx.keb.junit5.KebTestReportContext
import org.openprojectx.keb.junit5.KebTestReporter
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

public class AllureKebTestReporter : KebTestReporter {
    private val traces: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override fun testStarted(context: KebTestReportContext) {
        context.testClass.getAnnotation(BusinessScenario::class.java)?.applyToAllure()
        context.testMethod?.getAnnotation(BusinessScenario::class.java)?.applyToAllure()

        if (traceMode() != TraceMode.OFF) {
            runCatching {
                context.session.context.tracing().start(
                    Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
                        .setTitle(context.displayName),
                )
                traces += context.id
            }
        }
    }

    override fun beforeSessionClose(context: KebTestReportContext, failure: Throwable?) {
        if (failure != null) captureFailureDiagnostics(context)
        stopTrace(context, failure)
    }

    override fun testFinished(context: KebTestReportContext, failure: Throwable?) {
        context.artifacts.forEach { artifact ->
            runCatching {
                Files.newInputStream(artifact.path).use { stream ->
                    Allure.getLifecycle().addAttachment(
                        artifact.name,
                        artifact.mediaType,
                        artifact.extension,
                        stream,
                    )
                }
            }
        }
    }

    private fun captureFailureDiagnostics(context: KebTestReportContext) {
        val page = context.session.page
        Files.createDirectories(context.artifactDirectory)

        captureText(
            context,
            "Page source",
            "page-source.html",
            "text/html",
        ) { page.content() }
        captureText(
            context,
            "Console messages",
            "console.txt",
            "text/plain",
        ) { page.consoleMessages().joinToString("\n") { format(it) } }
        captureText(
            context,
            "Page errors",
            "page-errors.txt",
            "text/plain",
        ) { page.pageErrors().joinToString("\n") }
    }

    private fun captureText(
        context: KebTestReportContext,
        name: String,
        fileName: String,
        mediaType: String,
        content: () -> String,
    ) {
        runCatching {
            val value = content()
            if (value.isBlank()) return
            val path = context.artifactDirectory.resolve(fileName)
            Files.writeString(path, value)
            context.addArtifact(
                KebTestArtifact(
                    name = name,
                    mediaType = mediaType,
                    extension = fileName.substringAfterLast('.'),
                    path = path,
                ),
            )
        }
    }

    private fun stopTrace(context: KebTestReportContext, failure: Throwable?) {
        if (!traces.remove(context.id)) return
        runCatching {
            val retain = traceMode() == TraceMode.ON || failure != null
            if (retain) {
                Files.createDirectories(context.artifactDirectory)
                val path = context.artifactDirectory.resolve("trace.zip")
                context.session.context.tracing().stop(Tracing.StopOptions().setPath(path))
                context.addArtifact(
                    KebTestArtifact(
                        name = "Playwright trace",
                        mediaType = "application/zip",
                        extension = "zip",
                        path = path,
                    ),
                )
            } else {
                context.session.context.tracing().stop()
            }
        }
    }

    private fun BusinessScenario.applyToAllure() {
        epic.takeIf(String::isNotBlank)?.let(Allure::epic)
        feature.takeIf(String::isNotBlank)?.let(Allure::feature)
        story.takeIf(String::isNotBlank)?.let(Allure::story)
        owner.takeIf(String::isNotBlank)?.let { Allure.label("owner", it) }
        Allure.label("severity", severity.name.lowercase())
        description.takeIf(String::isNotBlank)?.let(Allure::description)
    }

    private fun format(message: ConsoleMessage): String = buildString {
        append('[').append(message.type()).append("] ").append(message.text())
        message.location().takeIf(String::isNotBlank)?.let { append(" (").append(it).append(')') }
    }

    private fun traceMode(): TraceMode = when (
        System.getProperty("keb.allure.trace", "retain-on-failure")
            .trim()
            .lowercase()
            .replace('_', '-')
    ) {
        "off", "false" -> TraceMode.OFF
        "on", "true" -> TraceMode.ON
        else -> TraceMode.RETAIN_ON_FAILURE
    }

    private enum class TraceMode {
        OFF,
        RETAIN_ON_FAILURE,
        ON,
    }
}
