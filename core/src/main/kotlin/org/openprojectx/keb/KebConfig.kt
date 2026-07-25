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

public data class KebConfig(
    val baseUrl: URI? = null,
    val browser: KebBrowserName = KebBrowserName.CHROMIUM,
    val headless: Boolean = true,
    val slowMotion: Duration? = null,
    val actionTimeout: Duration = 10.seconds,
    val navigationTimeout: Duration = 30.seconds,
    val artifactsDirectory: Path = Path.of("build", "keb-artifacts"),
) {
    init {
        require(actionTimeout.isPositive()) { "actionTimeout must be positive" }
        require(navigationTimeout.isPositive()) { "navigationTimeout must be positive" }
        require(slowMotion == null || !slowMotion.isNegative()) {
            "slowMotion must not be negative"
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
         * - `keb.slowMoMillis`
         * - `keb.actionTimeoutMillis`
         * - `keb.navigationTimeoutMillis`
         * - `keb.artifactsDirectory`
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
    }
}
