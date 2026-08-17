package org.openprojectx.keb.allure

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class BusinessScenario(
    val epic: String = "",
    val feature: String = "",
    val story: String = "",
    val owner: String = "",
    val severity: BusinessSeverity = BusinessSeverity.NORMAL,
    val description: String = "",
)

public enum class BusinessSeverity {
    BLOCKER,
    CRITICAL,
    NORMAL,
    MINOR,
    TRIVIAL,
}
