package org.openprojectx.keb.example

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import org.openprojectx.keb.KebModule
import org.openprojectx.keb.KebPage
import org.openprojectx.keb.KebSession
import org.openprojectx.keb.setTo

class LaboratoryPage(keb: KebSession) : KebPage(keb, "/") {
    val profile by module(::ProfileForm) {
        css("#profile-form")
    }

    val playground by module(::ComponentPlayground) {
        css("#components")
    }

    val workspaceNavigation by module(::WorkspaceNavigation) {
        role(AriaRole.NAVIGATION, "Main navigation")
    }

    val team by module(::TeamTable) {
        role(AriaRole.TABLE, "Team members")
    }

    val openNavigation by content {
        role(AriaRole.BUTTON, "Open navigation")
    }

    val closeNavigation by content {
        role(AriaRole.BUTTON, "Close navigation")
    }

    val navigation by content {
        role(AriaRole.NAVIGATION, "Main navigation")
    }

    val selectedWorkspace by content {
        testId("navigation-destination")
    }

    val selectedWorkspacePath by content {
        testId("navigation-path")
    }

    val toggleColorMode by content {
        role(AriaRole.BUTTON, "Toggle color mode")
    }

    private val title by content {
        heading("One interface. Many testable interactions.")
    }

    override fun at() = verify {
        title.isVisible()
        url endsWith "/"
    }
}

class WorkspaceNavigation(
    keb: KebSession,
    root: Locator,
) : KebModule(keb, root) {
    val customerExperience by content {
        role(AriaRole.BUTTON, "Customer experience")
    }

    val digitalCommerce by content {
        role(AriaRole.BUTTON, "Digital commerce")
    }

    val checkoutOperations by content {
        role(AriaRole.BUTTON, "Checkout operations")
    }

    fun selectCheckoutOperations() {
        customerExperience.click()
        digitalCommerce.click()
        checkoutOperations.click()
    }
}

class ProfileForm(
    keb: KebSession,
    root: Locator,
) : KebModule(keb, root) {
    val fullName by content {
        label("Full name")
    }

    val email by content {
        label("Email address")
    }

    val password by content {
        label("Password")
    }

    val department by content {
        role(AriaRole.COMBOBOX, "Department")
    }

    val skills by content {
        role(AriaRole.COMBOBOX, "Skills")
    }

    val startDate by content {
        label("Start date")
    }

    val junior by content {
        label("Junior")
    }

    val acceptTerms by content {
        label("Accept terms")
    }

    val emailNotifications by content {
        label("Email notifications")
    }

    val budget by content {
        role(AriaRole.SLIDER, "Automation budget")
    }

    val save by content {
        role(AriaRole.BUTTON, "Save profile")
    }

    val summary by content {
        testId("save-summary")
    }

    fun completeFor(name: String) {
        fullName setTo name
        email setTo "ada@example.test"
        password setTo "correct horse battery staple"

        department.click()
        keb.page.getByRole(
            AriaRole.OPTION,
            com.microsoft.playwright.Page.GetByRoleOptions().setName("Quality engineering"),
        ).click()

        skills.fill("Play")
        keb.page.getByRole(
            AriaRole.OPTION,
            com.microsoft.playwright.Page.GetByRoleOptions().setName("Playwright"),
        ).click()

        startDate.fill("2026-08-01")
        junior.check()
        acceptTerms.check()
        if (!emailNotifications.isChecked()) emailNotifications.check()
        budget.press("ArrowRight")
        save.click()
    }
}

class ComponentPlayground(
    keb: KebSession,
    root: Locator,
) : KebModule(keb, root) {
    val overviewTab by content {
        role(AriaRole.TAB, "Overview")
    }

    val activityTab by content {
        role(AriaRole.TAB, "Activity")
    }

    val tabContent by content {
        testId("tab-content")
    }

    val waitingQuestion by content {
        role(AriaRole.BUTTON, "How does Keb wait?")
    }

    val waitingAnswer by content {
        testId("waiting-answer")
    }

    val openDialog by content {
        role(AriaRole.BUTTON, "Open confirmation dialog")
    }

    val moreActions by content {
        role(AriaRole.BUTTON, "More actions")
    }

    fun selectActivity() {
        activityTab.click()
    }

    fun expandWaitingAnswer() {
        waitingQuestion.click()
    }
}

class TeamTable(
    keb: KebSession,
    root: Locator,
) : KebModule(keb, root) {
    val ada by content {
        root.getByRole(
            AriaRole.ROW,
            Locator.GetByRoleOptions().setName(
                java.util.regex.Pattern.compile("Ada Lovelace.*Automation architect.*Active"),
            ),
        )
    }

    val nextPage by content {
        root.getByRole(
            AriaRole.BUTTON,
            Locator.GetByRoleOptions().setName("Go to page 2"),
        )
    }
}
