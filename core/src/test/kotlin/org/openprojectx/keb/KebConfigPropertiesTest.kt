package org.openprojectx.keb

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.nio.file.Path
import kotlin.time.Duration.Companion.milliseconds

class KebConfigPropertiesTest {
    @AfterEach
    fun clearProperties() {
        propertyNames.forEach(System::clearProperty)
    }

    @Test
    fun `system properties override config defaults`() {
        System.setProperty("keb.baseUrl", "http://127.0.0.1:8080")
        System.setProperty("keb.browser", "firefox")
        System.setProperty("keb.headless", "false")
        System.setProperty("keb.slowMoMillis", "125")
        System.setProperty("keb.actionTimeoutMillis", "5000")
        System.setProperty("keb.navigationTimeoutMillis", "15000")
        System.setProperty("keb.artifactsDirectory", "build/custom-artifacts")

        val config = KebConfig.fromSystemProperties()

        assertEquals(URI("http://127.0.0.1:8080"), config.baseUrl)
        assertEquals(KebBrowserName.FIREFOX, config.browser)
        assertFalse(config.headless)
        assertEquals(125.milliseconds, config.slowMotion)
        assertEquals(5000.milliseconds, config.actionTimeout)
        assertEquals(15000.milliseconds, config.navigationTimeout)
        assertEquals(Path.of("build/custom-artifacts"), config.artifactsDirectory)
    }

    @Test
    fun `invalid boolean fails with the property name`() {
        System.setProperty("keb.headless", "sometimes")

        val error = assertThrows<IllegalArgumentException> {
            KebConfig.fromSystemProperties()
        }

        assertTrue(error.message.orEmpty().contains("keb.headless"))
    }

    companion object {
        private val propertyNames = listOf(
            "keb.baseUrl",
            "keb.browser",
            "keb.headless",
            "keb.slowMoMillis",
            "keb.actionTimeoutMillis",
            "keb.navigationTimeoutMillis",
            "keb.artifactsDirectory",
        )
    }
}
