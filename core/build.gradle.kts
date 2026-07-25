plugins {
    id("buildsrc.convention.kotlin-jvm")
}


dependencies {
    api(libs.playwright)

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiterApi)
    testRuntimeOnly(libs.junitJupiterEngine)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.register<JavaExec>("playwright") {
    group = "verification"
    description = "Runs the Playwright CLI, for example: playwright --args='install chromium'"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.microsoft.playwright.CLI")
}
