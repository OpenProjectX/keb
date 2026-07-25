package org.openprojectx.keb

public operator fun <T> KebSession.invoke(block: KebSession.() -> T): T = block()

public fun <T> drive(
    config: KebConfig = KebConfig(),
    block: KebSession.() -> T,
): T = KebBrowser.launch(config).use { browser ->
    browser.newSession().use { session ->
        session.block()
    }
}
