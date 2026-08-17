package org.openprojectx.keb

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole.BUTTON
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

class KebDslTest {
    @Test
    fun `pages and modules form a typed readable journey`() {
        val config = KebConfig(baseUrl = URI("http://127.0.0.1:${server.address.port}"))

        KebBrowser.launch(config).use { browser ->
            browser.newSession().use { keb ->
                val dashboard = keb {
                    to<LoginPage>()
                        .form
                        .loginAs("alice", "secret")
                }

                dashboard.verify {
                    dashboard.welcome hasText "Welcome, alice"
                    url endsWith "/dashboard"
                }
                assertEquals("/dashboard", URI(dashboard.currentUrl).path)
            }
        }
    }

    @Test
    fun `video can be retained for every session`(@TempDir temporaryDirectory: Path) {
        val artifacts = temporaryDirectory.resolve("artifacts")
        drive(
            KebConfig(
                videoMode = KebVideoMode.ON,
                videoSize = KebVideoSize(640, 360),
                artifactsDirectory = artifacts,
                videoStagingDirectory = temporaryDirectory.resolve("staging"),
            ),
        ) {
            page.setContent("<h1>Recorded journey</h1>")
            page.waitForTimeout(100.0)
        }

        assertEquals(1, videosIn(artifacts).size)
    }

    @Test
    fun `retain on failure removes passing video and keeps failing video`(@TempDir temporaryDirectory: Path) {
        val passingArtifacts = temporaryDirectory.resolve("passing")
        drive(
            KebConfig(
                videoMode = KebVideoMode.RETAIN_ON_FAILURE,
                artifactsDirectory = passingArtifacts,
                videoStagingDirectory = temporaryDirectory.resolve("passing-staging"),
            ),
        ) {
            page.setContent("<h1>Passing journey</h1>")
        }
        assertFalse(Files.exists(passingArtifacts))

        val failingArtifacts = temporaryDirectory.resolve("failing")
        assertThrows<IllegalStateException> {
            drive(
                KebConfig(
                    videoMode = KebVideoMode.RETAIN_ON_FAILURE,
                    artifactsDirectory = failingArtifacts,
                    videoStagingDirectory = temporaryDirectory.resolve("failing-staging"),
                ),
            ) {
                page.setContent("<h1>Failing journey</h1>")
                error("expected failure")
            }
        }
        assertTrue(videosIn(failingArtifacts).isNotEmpty())
    }

    private fun videosIn(directory: Path): List<Path> {
        if (Files.notExists(directory)) return emptyList()
        return Files.walk(directory).use { paths ->
            paths.filter { it.fileName.toString().endsWith(".webm") }.toList()
        }
    }

    class LoginPage(keb: KebSession) : KebPage(keb, "/login") {
        val form by module(::LoginForm) {
            testId("login-form")
        }

        private val title by content {
            heading("Sign in")
        }

        override fun at() = verify {
            title.isVisible()
            url endsWith "/login"
        }
    }

    class LoginForm(
        keb: KebSession,
        root: Locator,
    ) : KebModule(keb, root) {
        private val username by content {
            label("Username")
        }

        private val password by content {
            label("Password")
        }

        private val signIn by content {
            role(BUTTON, "Sign in")
        }

        fun loginAs(user: String, secret: String): DashboardPage {
            username setTo user
            password setTo secret
            signIn.click()
            return keb.at()
        }
    }

    class DashboardPage(keb: KebSession) : KebPage(keb) {
        val welcome by content {
            testId("welcome")
        }

        override fun at() = verify {
            welcome.isVisible()
            url endsWith "/dashboard"
        }
    }

    companion object {
        private lateinit var server: HttpServer

        @JvmStatic
        @BeforeAll
        fun startServer() {
            server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
            server.createContext("/login") { exchange ->
                val response = """
                    <!doctype html>
                    <html>
                      <body>
                        <h1>Sign in</h1>
                        <form data-testid="login-form">
                          <label>Username <input name="username"></label>
                          <label>Password <input name="password" type="password"></label>
                          <button type="submit">Sign in</button>
                        </form>
                        <script>
                          document.querySelector('form').addEventListener('submit', event => {
                            event.preventDefault();
                            const user = document.querySelector('[name=username]').value;
                            history.pushState({}, '', '/dashboard');
                            document.body.innerHTML = `<h1 data-testid="welcome">Welcome, ${'$'}{user}</h1>`;
                          });
                        </script>
                      </body>
                    </html>
                """.trimIndent().toByteArray()

                exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            server.start()
        }

        @JvmStatic
        @AfterAll
        fun stopServer() {
            server.stop(0)
        }
    }
}
