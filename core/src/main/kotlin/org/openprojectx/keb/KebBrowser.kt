package org.openprojectx.keb

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright

public class KebBrowser private constructor(
    private val playwright: Playwright,
    private val browser: Browser,
    public val config: KebConfig,
) : AutoCloseable {
    public fun newSession(): KebSession {
        val contextOptions = Browser.NewContextOptions()
        config.baseUrl?.let { contextOptions.setBaseURL(it.toString()) }

        val context = browser.newContext(contextOptions)
        context.setDefaultTimeout(config.actionTimeout.inWholeMilliseconds.toDouble())
        context.setDefaultNavigationTimeout(config.navigationTimeout.inWholeMilliseconds.toDouble())
        return KebSession(context, context.newPage(), config)
    }

    public override fun close() {
        browser.close()
        playwright.close()
    }

    public companion object {
        public fun launch(config: KebConfig = KebConfig()): KebBrowser {
            val playwright = Playwright.create()
            val browserType = when (config.browser) {
                KebBrowserName.CHROMIUM -> playwright.chromium()
                KebBrowserName.FIREFOX -> playwright.firefox()
                KebBrowserName.WEBKIT -> playwright.webkit()
            }
            return try {
                val browser = config.remoteEndpoint?.let { endpoint ->
                    val connectOptions = BrowserType.ConnectOptions()
                        .setTimeout(config.remoteConnectTimeout.inWholeMilliseconds.toDouble())
                    config.slowMotion?.let {
                        connectOptions.setSlowMo(it.inWholeMilliseconds.toDouble())
                    }
                    browserType.connect(endpoint.toString(), connectOptions)
                } ?: run {
                    val launchOptions = BrowserType.LaunchOptions()
                        .setHeadless(config.headless)
                    config.browserChannel?.let(launchOptions::setChannel)
                    config.executablePath?.let(launchOptions::setExecutablePath)
                    config.slowMotion?.let {
                        launchOptions.setSlowMo(it.inWholeMilliseconds.toDouble())
                    }
                    browserType.launch(launchOptions)
                }
                KebBrowser(playwright, browser, config)
            } catch (error: Throwable) {
                playwright.close()
                throw error
            }
        }
    }
}
