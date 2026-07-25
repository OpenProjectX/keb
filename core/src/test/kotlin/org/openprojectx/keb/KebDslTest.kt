package org.openprojectx.keb

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole.BUTTON
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.URI

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
