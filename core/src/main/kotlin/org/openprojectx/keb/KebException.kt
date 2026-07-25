package org.openprojectx.keb

public open class KebException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

public class PageVerificationException(
    page: KebPage,
    cause: Throwable,
) : KebException(
    "Could not verify ${page::class.simpleName ?: page::class.java.name} at ${page.currentUrl}",
    cause,
)
