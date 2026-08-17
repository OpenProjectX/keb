package org.openprojectx.keb.example

import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.openprojectx.keb.KebSession
import org.openprojectx.keb.allure.BusinessScenario
import org.openprojectx.keb.allure.Screenshot
import org.openprojectx.keb.allure.journey
import org.openprojectx.keb.invoke
import org.openprojectx.keb.junit5.KebTest

@KebTest(baseUrl = ExampleUiServer.BASE_URL)
class UiLaboratoryTest : ExampleUiServer() {
    @Test
    @DisplayName("Operations user opens the checkout workspace")
    @BusinessScenario(
        epic = "Customer Experience",
        feature = "Workspace Navigation",
        story = "Navigate through the business hierarchy",
        owner = "Digital Commerce Operations",
        description = "A business user follows the domain hierarchy to reach checkout operations.",
    )
    fun `operations user follows hierarchical navigation`(keb: KebSession) = keb.journey {
        lateinit var laboratory: LaboratoryPage

        given("the operations user is viewing the UI laboratory") {
            laboratory = to<LaboratoryPage>()
            parameter("Starting workspace", "UI laboratory")
        }

        whenever(
            "the user follows Customer experience to Digital commerce and Checkout operations",
            screenshot = Screenshot.AFTER,
        ) {
            laboratory.openNavigation.click()
            laboratory.workspaceNavigation.selectCheckoutOperations()
        }

        then(
            "the Checkout operations workspace is selected",
            screenshot = Screenshot.AFTER,
        ) {
            laboratory.verify {
                laboratory.selectedWorkspace containsText "Checkout operations"
            }
            evidence("Selected checkout workspace", laboratory.selectedWorkspace)
        }

        andThen("the complete business navigation path is visible") {
            laboratory.verify {
                laboratory.selectedWorkspacePath hasText
                    "Customer experience / Digital commerce / Checkout operations"
            }
            parameter("Business path", "Customer experience > Digital commerce > Checkout operations")
        }
    }

    @Test
    fun `navigation and display controls are accessible`(keb: KebSession) = keb {
        val laboratory = to<LaboratoryPage>()

        laboratory.openNavigation.click()
        laboratory.verify {
            laboratory.navigation.isVisible()
        }
        laboratory.closeNavigation.click()
        laboratory.toggleColorMode.click()
        Unit
    }

    @Test
    fun `profile form supports common input controls`(keb: KebSession) = keb {
        val laboratory = to<LaboratoryPage>()

        laboratory.profile.completeFor("Ada Keb")

        laboratory.verify {
            laboratory.profile.summary containsText "Profile saved for Ada Keb"
            laboratory.profile.acceptTerms.isChecked()
            laboratory.profile.junior.isChecked()
        }
        Unit
    }

    @Test
    fun `tabs accordions dialogs menus and tables work together`(keb: KebSession) = keb {
        val laboratory = to<LaboratoryPage>()

        laboratory.playground.selectActivity()
        laboratory.verify {
            laboratory.playground.tabContent containsText "latest browser journeys"
        }

        laboratory.playground.expandWaitingAnswer()
        laboratory.verify {
            laboratory.playground.waitingAnswer containsText "Playwright"
            laboratory.team.ada.isVisible()
        }

        laboratory.playground.openDialog.click()
        val dialog = page.getByRole(
            AriaRole.DIALOG,
            com.microsoft.playwright.Page.GetByRoleOptions().setName("Run the complete suite?"),
        )
        dialog.getByRole(
            AriaRole.BUTTON,
            com.microsoft.playwright.Locator.GetByRoleOptions().setName("Cancel"),
        ).click()

        laboratory.playground.moreActions.click()
        page.getByRole(
            AriaRole.MENUITEM,
            com.microsoft.playwright.Page.GetByRoleOptions().setName("Export report"),
        ).click()
        Unit
    }
}
