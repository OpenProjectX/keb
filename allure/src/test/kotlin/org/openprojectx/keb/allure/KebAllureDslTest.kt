package org.openprojectx.keb.allure

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.openprojectx.keb.KebSession
import org.openprojectx.keb.junit5.KebTest

@KebTest
class KebAllureDslTest {
    @Test
    @DisplayName("Business journey records contextual evidence")
    @BusinessScenario(
        epic = "Reporting",
        feature = "Business journeys",
        story = "Numbered evidence",
        owner = "Quality team",
    )
    fun `records a numbered journey`(keb: KebSession) = keb.journey {
        given("a business status is available") {
            page.setContent("<main><h1>Order accepted</h1></main>")
            parameter("Order state", "Accepted")
            Unit
        }

        then("the accepted state is visible", screenshot = Screenshot.AFTER) {
            val status = page.getByRole(
                com.microsoft.playwright.options.AriaRole.HEADING,
                com.microsoft.playwright.Page.GetByRoleOptions().setName("Order accepted"),
            )
            evidence("Accepted state", status)
            Unit
        }
    }
}
