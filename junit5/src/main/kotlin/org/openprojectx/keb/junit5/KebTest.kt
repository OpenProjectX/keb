package org.openprojectx.keb.junit5

import org.junit.jupiter.api.extension.ExtendWith
import org.openprojectx.keb.KebBrowserName

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(KebExtension::class)
public annotation class KebTest(
    val baseUrl: String = "",
    val browser: KebBrowserName = KebBrowserName.CHROMIUM,
    val headless: Boolean = true,
)
