package org.openprojectx.keb.allure

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import io.qameta.allure.Allure
import io.qameta.allure.model.Parameter
import org.openprojectx.keb.KebSession
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

public enum class Screenshot {
    NONE,
    ON_FAILURE,
    BEFORE,
    AFTER,
    BEFORE_AND_AFTER,
}

/** Creates numbered, business-readable Allure steps around a Keb journey. */
public fun KebSession.journey(
    title: String? = null,
    block: KebJourney.() -> Unit,
) {
    val run = { KebJourney(this).block() }
    if (title.isNullOrBlank()) {
        run()
    } else {
        Allure.step(title, Allure.ThrowableRunnableVoid { run() })
    }
}

public class KebJourney internal constructor(
    private val keb: KebSession,
) {
    private var number: Int = 0
    private var activeStep: Allure.StepContext? = null

    public fun <T> given(
        title: String,
        screenshot: Screenshot = Screenshot.ON_FAILURE,
        mask: List<Locator> = emptyList(),
        block: KebSession.() -> T,
    ): T = businessStep("Given", title, screenshot, mask, block)

    public fun <T> whenever(
        title: String,
        screenshot: Screenshot = Screenshot.ON_FAILURE,
        mask: List<Locator> = emptyList(),
        block: KebSession.() -> T,
    ): T = businessStep("When", title, screenshot, mask, block)

    public fun <T> then(
        title: String,
        screenshot: Screenshot = Screenshot.ON_FAILURE,
        mask: List<Locator> = emptyList(),
        block: KebSession.() -> T,
    ): T = businessStep("Then", title, screenshot, mask, block)

    public fun <T> andThen(
        title: String,
        screenshot: Screenshot = Screenshot.ON_FAILURE,
        mask: List<Locator> = emptyList(),
        block: KebSession.() -> T,
    ): T = businessStep("And then", title, screenshot, mask, block)

    public fun <T> step(
        title: String,
        screenshot: Screenshot = Screenshot.ON_FAILURE,
        mask: List<Locator> = emptyList(),
        block: KebSession.() -> T,
    ): T = businessStep(null, title, screenshot, mask, block)

    public fun <T> parameter(name: String, value: T): T =
        activeStep?.parameter(name, value) ?: Allure.parameter(name, value)

    public fun <T> maskedParameter(name: String, value: T): T =
        activeStep?.parameter(name, value, Parameter.Mode.MASKED)
            ?: Allure.parameter(name, value, Parameter.Mode.MASKED)

    public fun <T> hiddenParameter(name: String, value: T): T =
        activeStep?.parameter(name, value, Parameter.Mode.HIDDEN)
            ?: Allure.parameter(name, value, Parameter.Mode.HIDDEN)

    public fun evidence(
        name: String,
        fullPage: Boolean = true,
        mask: List<Locator> = emptyList(),
    ) {
        attachPageScreenshot(name, fullPage, mask)
    }

    public fun evidence(
        name: String,
        locator: Locator,
        mask: List<Locator> = emptyList(),
    ) {
        runCatching {
            val options = Locator.ScreenshotOptions()
            if (mask.isNotEmpty()) options.setMask(mask)
            attachBytes(name, "image/png", "png", locator.screenshot(options))
        }
    }

    public fun attachText(name: String, content: String) {
        attachBytes(name, "text/plain", "txt", content.toByteArray())
    }

    public fun attachJson(name: String, content: String) {
        attachBytes(name, "application/json", "json", content.toByteArray())
    }

    public fun attachFile(
        name: String,
        path: Path,
        mediaType: String = "application/octet-stream",
        extension: String = path.fileName.toString().substringAfterLast('.', "bin"),
    ) {
        Files.newInputStream(path).use { stream -> attachStream(name, mediaType, extension, stream) }
    }

    private fun <T> businessStep(
        keyword: String?,
        title: String,
        screenshot: Screenshot,
        mask: List<Locator>,
        block: KebSession.() -> T,
    ): T {
        number += 1
        val label = listOfNotNull("$number.", keyword, title).joinToString(" ")
        return Allure.step(label, Allure.ThrowableContextRunnable<T, Allure.StepContext> { stepContext ->
            val previous = activeStep
            activeStep = stepContext
            try {
                if (screenshot == Screenshot.BEFORE || screenshot == Screenshot.BEFORE_AND_AFTER) {
                    attachPageScreenshot("Before step", fullPage = true, mask)
                }
                val result = keb.block()
                if (screenshot == Screenshot.AFTER || screenshot == Screenshot.BEFORE_AND_AFTER) {
                    attachPageScreenshot("After step", fullPage = true, mask)
                }
                result
            } catch (failure: Throwable) {
                if (screenshot != Screenshot.NONE) {
                    attachPageScreenshot("Step failure", fullPage = true, mask)
                }
                throw failure
            } finally {
                activeStep = previous
            }
        })
    }

    private fun attachPageScreenshot(
        name: String,
        fullPage: Boolean,
        mask: List<Locator>,
    ) {
        runCatching {
            val options = Page.ScreenshotOptions().setFullPage(fullPage)
            if (mask.isNotEmpty()) options.setMask(mask)
            attachBytes(name, "image/png", "png", keb.page.screenshot(options))
        }
    }

    private fun attachBytes(name: String, mediaType: String, extension: String, bytes: ByteArray) {
        Allure.getLifecycle().addAttachment(name, mediaType, extension, bytes)
    }

    private fun attachStream(name: String, mediaType: String, extension: String, stream: InputStream) {
        Allure.getLifecycle().addAttachment(name, mediaType, extension, stream)
    }
}
