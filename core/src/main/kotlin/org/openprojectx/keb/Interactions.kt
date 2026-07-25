package org.openprojectx.keb

import com.microsoft.playwright.Locator

public infix fun Locator.setTo(value: String) {
    fill(value)
}

public infix fun Locator.select(value: String) {
    selectOption(value)
}

public infix fun Locator.pressKey(key: String) {
    press(key)
}
