package org.openprojectx.keb

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import java.util.regex.Pattern

public class VerificationScope internal constructor(
    private val page: Page,
) {
    public val url: UrlExpectation = UrlExpectation(page)

    public fun Locator.isVisible() {
        assertThat(this).isVisible()
    }

    public fun Locator.isHidden() {
        assertThat(this).isHidden()
    }

    public fun Locator.isEnabled() {
        assertThat(this).isEnabled()
    }

    public fun Locator.isChecked() {
        assertThat(this).isChecked()
    }

    public infix fun Locator.hasText(expected: String) {
        assertThat(this).hasText(expected)
    }

    public infix fun Locator.containsText(expected: String) {
        assertThat(this).containsText(expected)
    }
}

public class UrlExpectation internal constructor(
    private val page: Page,
) {
    public infix fun endsWith(suffix: String) {
        assertThat(page).hasURL(Pattern.compile(".*${escapeRegex(suffix)}$"))
    }

    public infix fun isExactly(expected: String) {
        assertThat(page).hasURL(expected)
    }

    public infix fun matches(pattern: Regex) {
        assertThat(page).hasURL(pattern.toPattern())
    }

    private fun escapeRegex(value: String): String = buildString {
        value.forEach { character ->
            if (character in """\.^$|?*+()[]{}""") append('\\')
            append(character)
        }
    }
}
