package org.openprojectx.keb

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page

@KebDsl
public class KebSession internal constructor(
    public val context: BrowserContext,
    public val page: Page,
    public val config: KebConfig,
) : AutoCloseable {
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
        context.close()
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
