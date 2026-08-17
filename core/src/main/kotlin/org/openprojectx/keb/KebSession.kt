package org.openprojectx.keb

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Video
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

@KebDsl
public class KebSession internal constructor(
    public val context: BrowserContext,
    public val page: Page,
    public val config: KebConfig,
) : AutoCloseable {
    private val videos = linkedSetOf<Video>()
    private val defaultArtifactDirectory = config.artifactsDirectory
        .resolve("session-${UUID.randomUUID().toString().take(8)}")
    private var closed = false
    private var retainedVideos: List<Path> = emptyList()

    init {
        if (config.videoMode != KebVideoMode.OFF) {
            page.video()?.let(videos::add)
            context.onPage { newPage -> newPage.video()?.let(videos::add) }
        }
    }

    public fun <P : KebPage> to(factory: (KebSession) -> P): P {
        val destination = factory(this)
        val path = requireNotNull(destination.path) {
            "${destination::class.simpleName} does not define a path"
        }
        page.navigate(config.resolve(path))
        destination.verifyAt()
        return destination
    }

    public inline fun <reified P : KebPage> to(): P = to(pageFactory())

    public fun <P : KebPage> at(factory: (KebSession) -> P): P =
        factory(this).also(KebPage::verifyAt)

    public inline fun <reified P : KebPage> at(): P = at(pageFactory())

    public fun <P : KebPage> at(page: P, assertions: P.() -> Unit = {}): P {
        page.verifyAt()
        page.assertions()
        return page
    }

    public fun back() {
        page.goBack()
    }

    public fun refresh() {
        page.reload()
    }

    public override fun close() {
        finish(failed = false, artifactDirectory = defaultArtifactDirectory)
    }

    /** Closes this session and applies the configured video policy for [failed]. */
    public fun close(failed: Boolean) {
        finish(failed = failed, artifactDirectory = defaultArtifactDirectory)
    }

    /**
     * Closes this session and saves retained videos below [artifactDirectory].
     *
     * Test integrations should pass their outcome so `RETAIN_ON_FAILURE` can
     * discard recordings from successful tests.
     */
    public fun close(
        failed: Boolean,
        artifactDirectory: Path,
    ) {
        finish(failed, artifactDirectory)
    }

    /**
     * Closes this session and returns videos retained by the configured policy.
     *
     * Framework integrations can use the returned client-local paths to publish
     * recordings even when Playwright is connected to a remote browser.
     */
    public fun finish(
        failed: Boolean,
        artifactDirectory: Path = defaultArtifactDirectory,
    ): List<Path> {
        if (closed) return retainedVideos
        closed = true

        var problem: Throwable? = null
        fun attempt(action: () -> Unit) {
            try {
                action()
            } catch (error: Throwable) {
                problem?.addSuppressed(error) ?: run { problem = error }
            }
        }

        var contextClosed = false
        attempt {
            context.close()
            contextClosed = true
        }

        if (contextClosed && videos.isNotEmpty()) {
            val retain = config.videoMode == KebVideoMode.ON ||
                config.videoMode == KebVideoMode.RETAIN_ON_FAILURE && failed
            if (retain) attempt { Files.createDirectories(artifactDirectory) }

            val savedVideos = mutableListOf<Path>()
            videos.forEachIndexed { index, video ->
                if (retain) {
                    val destination = artifactDirectory.resolve("video-${index + 1}.webm")
                    attempt {
                        video.saveAs(destination)
                        savedVideos.add(destination)
                    }
                }
                attempt { video.delete() }
            }
            retainedVideos = savedVideos
            runCatching { Files.deleteIfExists(config.videoStagingDirectory) }
        }

        problem?.let { throw it }
        return retainedVideos
    }

    @PublishedApi
    internal inline fun <reified P : KebPage> pageFactory(): (KebSession) -> P = { session ->
        try {
            P::class.java.getDeclaredConstructor(KebSession::class.java)
                .also { it.trySetAccessible() }
                .newInstance(session)
        } catch (error: ReflectiveOperationException) {
            throw KebException(
                "${P::class.simpleName} must declare a constructor accepting KebSession",
                error,
            )
        }
    }
}
