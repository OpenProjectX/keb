package org.openprojectx.keb.junit5

import org.junit.jupiter.api.Test
import org.openprojectx.keb.KebPage
import org.openprojectx.keb.KebSession
import org.openprojectx.keb.invoke

@KebTest
class KebExtensionTest {
    @Test
    fun `injects an isolated session`(keb: KebSession) = keb {
        page.setContent("<main><h1>Injected session</h1></main>")
        at<InjectedPage>()
        Unit
    }

    class InjectedPage(keb: KebSession) : KebPage(keb) {
        private val title by content {
            heading("Injected session")
        }

        override fun at() = verify {
            title.isVisible()
        }
    }
}
