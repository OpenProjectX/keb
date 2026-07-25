package org.openprojectx.keb

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@KebDsl
public abstract class ContentContainer internal constructor(
    internal val session: KebSession,
    private val rootLocator: Locator?,
) {
    protected val keb: KebSession
        get() = session

    protected val playwrightPage: Page
        get() = keb.page

    public fun css(selector: String): Locator =
        rootLocator?.locator(selector) ?: playwrightPage.locator(selector)

    public fun text(value: String, exact: Boolean = false): Locator =
        rootLocator?.getByText(value, Locator.GetByTextOptions().setExact(exact))
            ?: playwrightPage.getByText(value, Page.GetByTextOptions().setExact(exact))

    public fun label(value: String, exact: Boolean = false): Locator =
        rootLocator?.getByLabel(value, Locator.GetByLabelOptions().setExact(exact))
            ?: playwrightPage.getByLabel(value, Page.GetByLabelOptions().setExact(exact))

    public fun placeholder(value: String, exact: Boolean = false): Locator =
        rootLocator?.getByPlaceholder(value, Locator.GetByPlaceholderOptions().setExact(exact))
            ?: playwrightPage.getByPlaceholder(value, Page.GetByPlaceholderOptions().setExact(exact))

    public fun testId(value: String): Locator =
        rootLocator?.getByTestId(value) ?: playwrightPage.getByTestId(value)

    public fun role(
        role: AriaRole,
        name: String? = null,
        exact: Boolean = false,
    ): Locator {
        return if (rootLocator == null) {
            val options = Page.GetByRoleOptions().setExact(exact)
            if (name != null) options.setName(name)
            playwrightPage.getByRole(role, options)
        } else {
            val options = Locator.GetByRoleOptions().setExact(exact)
            if (name != null) options.setName(name)
            rootLocator.getByRole(role, options)
        }
    }

    public fun heading(name: String, exact: Boolean = false): Locator =
        role(AriaRole.HEADING, name, exact)

    protected fun content(
        definition: ContentContainer.() -> Locator,
    ): ReadOnlyProperty<Any?, Locator> = ContentDelegate(this, definition)

    protected fun <M : KebModule> module(
        factory: (KebSession, Locator) -> M,
        root: ContentContainer.() -> Locator,
    ): ReadOnlyProperty<Any?, M> = ModuleDelegate(this, factory, root)
}

private class ContentDelegate(
    owner: ContentContainer,
    definition: ContentContainer.() -> Locator,
) : ReadOnlyProperty<Any?, Locator> {
    private val locator: Locator by lazy(LazyThreadSafetyMode.NONE) {
        owner.definition()
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Locator = locator
}

private class ModuleDelegate<M : KebModule>(
    owner: ContentContainer,
    factory: (KebSession, Locator) -> M,
    root: ContentContainer.() -> Locator,
) : ReadOnlyProperty<Any?, M> {
    private val module: M by lazy(LazyThreadSafetyMode.NONE) {
        factory(owner.session, owner.root())
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): M = module
}
