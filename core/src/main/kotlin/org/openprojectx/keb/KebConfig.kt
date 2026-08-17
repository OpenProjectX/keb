package org.openprojectx.keb

import java.net.URI
import java.nio.file.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.milliseconds

public enum class KebBrowserName {
    CHROMIUM,
    FIREFOX,
    WEBKIT,
}

public enum class KebVideoMode {
    /** Do not record video. */
    OFF,

    /** Record every session, retaining files only when the test failed. */
    RETAIN_ON_FAILURE,

    /** Retain video for every session. */
    ON,
}

/** Video frame dimensions in pixels. */
public data class KebVideoSize(
    val width: Int,
    val height: Int,
) {
    init {
        require(width > 0) { "video width must be greater than zero" }
        require(height > 0) { "video height must be greater than zero" }
    }
}

public data class KebConfig(
    val baseUrl: URI? = null,
    val browser: KebBrowserName = KebBrowserName.CHROMIUM,
    val headless: Boolean = true,
    val browserChannel: String? = null,
    val executablePath: Path? = null,
    val remoteEndpoint: URI? = null,
    val remoteConnectTimeout: Duration = 30.seconds,
    val slowMotion: Duration? = null,
    val actionTimeout: Duration = 10.seconds,
    val navigationTimeout: Duration = 30.seconds,
    val artifactsDirectory: Path = Path.of("build", "keb-artifacts"),
    val videoMode: KebVideoMode = KebVideoMode.OFF,
    val videoSize: KebVideoSize? = null,
    val videoStagingDirectory: Path = Path.of(".keb-video-staging"),
) {
    init {
        require(actionTimeout.isPositive()) { "actionTimeout must be positive" }
        require(navigationTimeout.isPositive()) { "navigationTimeout must be positive" }
        require(remoteConnectTimeout.isPositive()) { "remoteConnectTimeout must be positive" }
        require(slowMotion == null || !slowMotion.isNegative()) {
            "slowMotion must not be negative"
        }
        require(browserChannel == null || executablePath == null) {
            "browserChannel and executablePath cannot both be configured"
        }
        require(browserChannel == null || browser == KebBrowserName.CHROMIUM) {
            "browserChannel is supported only with the Chromium browser type"
        }
        require(remoteEndpoint == null || remoteEndpoint.scheme in setOf("ws", "wss")) {
            "remoteEndpoint must use the ws or wss scheme"
        }
        require(remoteEndpoint == null || browserChannel == null && executablePath == null) {
            "browserChannel and executablePath cannot be used with remoteEndpoint"
        }
    }

    internal fun resolve(path: String): String {
        if (URI.create(path).isAbsolute) return path

        val base = requireNotNull(baseUrl) {
            "KebConfig.baseUrl is required to navigate to the relative path '$path'"
        }
        return base.resolve(path).toString()
    }

    public companion object {
        /**
         * Applies JVM system properties over [defaults].
         *
         * Supported properties:
         * - `keb.baseUrl`
         * - `keb.browser` (`chromium`, `firefox`, or `webkit`)
         * - `keb.headless`
         * - `keb.browserChannel`
         * - `keb.executablePath`
         * - `keb.remoteEndpoint`
         * - `keb.remoteConnectTimeoutMillis`
         * - `keb.slowMoMillis`
         * - `keb.actionTimeoutMillis`
         * - `keb.navigationTimeoutMillis`
         * - `keb.artifactsDirectory`
         * - `keb.video` (`off`, `retain-on-failure`, or `on`)
         * - `keb.videoWidth` and `keb.videoHeight`
         * - `keb.videoStagingDirectory`
         */
        public fun fromSystemProperties(defaults: KebConfig = KebConfig()): KebConfig =
            defaults.copy(
                baseUrl = property("keb.baseUrl")?.let(::URI) ?: defaults.baseUrl,
                browser = property("keb.browser")
                    ?.let { value ->
                        runCatching {
                            KebBrowserName.valueOf(value.uppercase())
                        }.getOrElse {
                            throw IllegalArgumentException(
                                "keb.browser must be chromium, firefox, or webkit; was '$value'",
                            )
                        }
                    }
                    ?: defaults.browser,
                headless = property("keb.headless")
                    ?.let { value ->
                        value.toBooleanStrictOrNull()
                            ?: throw IllegalArgumentException(
                                "keb.headless must be true or false; was '$value'",
                            )
                    }
                    ?: defaults.headless,
                browserChannel = property("keb.browserChannel")
                    ?: defaults.browserChannel,
                executablePath = property("keb.executablePath")
                    ?.let(Path::of)
                    ?: defaults.executablePath,
                remoteEndpoint = property("keb.remoteEndpoint")
                    ?.let(URI::create)
                    ?: defaults.remoteEndpoint,
                remoteConnectTimeout = property("keb.remoteConnectTimeoutMillis")
                    ?.let { milliseconds("keb.remoteConnectTimeoutMillis", it) }
                    ?: defaults.remoteConnectTimeout,
                slowMotion = property("keb.slowMoMillis")
                    ?.let { milliseconds("keb.slowMoMillis", it, allowZero = true) }
                    ?: defaults.slowMotion,
                actionTimeout = property("keb.actionTimeoutMillis")
                    ?.let { milliseconds("keb.actionTimeoutMillis", it) }
                    ?: defaults.actionTimeout,
                navigationTimeout = property("keb.navigationTimeoutMillis")
                    ?.let { milliseconds("keb.navigationTimeoutMillis", it) }
                    ?: defaults.navigationTimeout,
                artifactsDirectory = property("keb.artifactsDirectory")
                    ?.let(Path::of)
                    ?: defaults.artifactsDirectory,
                videoMode = property("keb.video")
                    ?.let(::videoMode)
                    ?: defaults.videoMode,
                videoSize = videoSize(defaults.videoSize),
                videoStagingDirectory = property("keb.videoStagingDirectory")
                    ?.let(Path::of)
                    ?: defaults.videoStagingDirectory,
            )

        private fun property(name: String): String? =
            System.getProperty(name)?.trim()?.takeIf(String::isNotEmpty)

        private fun milliseconds(
            name: String,
            value: String,
            allowZero: Boolean = false,
        ): Duration {
            val number = value.toLongOrNull()
                ?: throw IllegalArgumentException("$name must be an integer number of milliseconds; was '$value'")
            require(number > 0 || allowZero && number == 0L) {
                "$name must be ${if (allowZero) "zero or greater" else "greater than zero"}; was '$value'"
            }
            return number.milliseconds
        }

        private fun videoMode(value: String): KebVideoMode =
            runCatching {
                KebVideoMode.valueOf(value.uppercase().replace('-', '_'))
            }.getOrElse {
                throw IllegalArgumentException(
                    "keb.video must be off, retain-on-failure, or on; was '$value'",
                )
            }

        private fun videoSize(default: KebVideoSize?): KebVideoSize? {
            val width = property("keb.videoWidth")
            val height = property("keb.videoHeight")
            if (width == null && height == null) return default
            require(width != null && height != null) {
                "keb.videoWidth and keb.videoHeight must be configured together"
            }
            return KebVideoSize(
                width = positiveInt("keb.videoWidth", width),
                height = positiveInt("keb.videoHeight", height),
            )
        }

        private fun positiveInt(name: String, value: String): Int {
            val number = value.toIntOrNull()
                ?: throw IllegalArgumentException("$name must be an integer; was '$value'")
            require(number > 0) { "$name must be greater than zero; was '$value'" }
            return number
        }
    }
}
