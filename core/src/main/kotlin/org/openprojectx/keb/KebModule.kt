package org.openprojectx.keb

import com.microsoft.playwright.Locator

public abstract class KebModule(
    keb: KebSession,
    public val root: Locator,
) : ContentContainer(keb, root)
