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
        System.setProperty("keb.executablePath", "/opt/browsers/firefox")
        System.setProperty("keb.remoteConnectTimeoutMillis", "20000")
        System.setProperty("keb.slowMoMillis", "125")
        System.setProperty("keb.actionTimeoutMillis", "5000")
        System.setProperty("keb.navigationTimeoutMillis", "15000")
        System.setProperty("keb.artifactsDirectory", "build/custom-artifacts")

        val config = KebConfig.fromSystemProperties()

        assertEquals(URI("http://127.0.0.1:8080"), config.baseUrl)
        assertEquals(KebBrowserName.FIREFOX, config.browser)
        assertFalse(config.headless)
        assertEquals(Path.of("/opt/browsers/firefox"), config.executablePath)
        assertEquals(20000.milliseconds, config.remoteConnectTimeout)
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

    @Test
    fun `browser channel and executable path are mutually exclusive`() {
        assertThrows<IllegalArgumentException> {
            KebConfig(
                browserChannel = "chrome",
                executablePath = Path.of("/opt/google/chrome/chrome"),
            )
        }
    }

    @Test
    fun `branded browser channels require chromium`() {
        assertThrows<IllegalArgumentException> {
            KebConfig(
                browser = KebBrowserName.FIREFOX,
                browserChannel = "chrome",
            )
        }
    }

    @Test
    fun `remote endpoint and timeout are read from system properties`() {
        System.setProperty("keb.remoteEndpoint", "ws://playwright.example:3000/")
        System.setProperty("keb.remoteConnectTimeoutMillis", "45000")

        val config = KebConfig.fromSystemProperties()

        assertEquals(URI("ws://playwright.example:3000/"), config.remoteEndpoint)
        assertEquals(45000.milliseconds, config.remoteConnectTimeout)
    }

    @Test
    fun `remote endpoint rejects local launch options and non websocket schemes`() {
        assertThrows<IllegalArgumentException> {
            KebConfig(
                remoteEndpoint = URI("ws://playwright.example:3000/"),
                executablePath = Path.of("/opt/google/chrome/chrome"),
            )
        }
        assertThrows<IllegalArgumentException> {
            KebConfig(remoteEndpoint = URI("http://playwright.example:3000/"))
        }
    }

    companion object {
        private val propertyNames = listOf(
            "keb.baseUrl",
            "keb.browser",
            "keb.headless",
            "keb.browserChannel",
            "keb.executablePath",
            "keb.remoteEndpoint",
            "keb.remoteConnectTimeoutMillis",
            "keb.slowMoMillis",
            "keb.actionTimeoutMillis",
            "keb.navigationTimeoutMillis",
            "keb.artifactsDirectory",
        )
    }
}
