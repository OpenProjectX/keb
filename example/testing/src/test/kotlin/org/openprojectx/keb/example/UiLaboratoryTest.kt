package org.openprojectx.keb.example

import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Test
import org.openprojectx.keb.KebSession
import org.openprojectx.keb.invoke
import org.openprojectx.keb.junit5.KebTest

@KebTest(baseUrl = ExampleUiServer.BASE_URL)
class UiLaboratoryTest : ExampleUiServer() {
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
