package org.openprojectx.keb

public operator fun <T> KebSession.invoke(block: KebSession.() -> T): T = block()

public fun <T> drive(
    config: KebConfig = KebConfig(),
    block: KebSession.() -> T,
): T = KebBrowser.launch(config).use { browser ->
    val session = browser.newSession()
    var failure: Throwable? = null
    try {
        session.block()
    } catch (error: Throwable) {
        failure = error
        throw error
    } finally {
        try {
            session.close(failed = failure != null)
        } catch (closeError: Throwable) {
            failure?.addSuppressed(closeError) ?: throw closeError
        }
    }
}
