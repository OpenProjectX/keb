package org.openprojectx.keb

public abstract class KebPage(
    keb: KebSession,
    public open val path: String? = null,
) : ContentContainer(keb, rootLocator = null) {
    public val currentUrl: String
        get() = keb.page.url()

    protected abstract fun at()

    public fun verifyAt() {
        try {
            at()
        } catch (error: AssertionError) {
            throw PageVerificationException(this, error)
        }
    }

    public fun verify(assertions: VerificationScope.() -> Unit) {
        VerificationScope(keb.page).assertions()
    }
}
