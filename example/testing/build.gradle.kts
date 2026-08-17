plugins {
    kotlin("jvm") version "2.2.21"
}

val kebVersion: String by project
val kebPropertyNames = listOf(
    "keb.baseUrl",
    "keb.browser",
    "keb.headless",
    "keb.browserChannel",
    "keb.executablePath",
    "keb.remoteEndpoint",
    "keb.remoteConnectTimeoutMillis",
    "keb.slowMoMillis",
    "keb.actionTimeoutMillis",
    "keb.navigationTimeoutMillis",
    "keb.artifactsDirectory",
)

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation("org.openprojectx.test.keb:keb-core:$kebVersion")
    testImplementation("org.openprojectx.test.keb:keb-junit5:$kebVersion")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.configureEach {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

tasks.test {
    dependsOn(":ui:bunBuild")
    useJUnitPlatform()

    workingDir = rootProject.projectDir
    systemProperty("keb.example.uiDir", project(":ui").projectDir.absolutePath)
    kebPropertyNames.forEach { name ->
        providers.gradleProperty(name)
            .orElse(providers.systemProperty(name))
            .orNull
            ?.let { value -> systemProperty(name, value) }
    }

    testLogging {
        events("passed", "skipped", "failed")
    }
}

val remoteBrowserUp by tasks.registering(Exec::class) {
    group = "verification"
    description = "Starts the Playwright 1.62.1 browser server in Docker"
    workingDir = rootProject.projectDir
    commandLine(
        "docker", "compose",
        "--project-name", "keb-example-playwright",
        "--file", "compose.yaml",
        "up", "--detach", "--wait",
    )
}

val remoteBrowserDown by tasks.registering(Exec::class) {
    group = "verification"
    description = "Stops the example Playwright browser server"
    workingDir = rootProject.projectDir
    commandLine(
        "docker", "compose",
        "--project-name", "keb-example-playwright",
        "--file", "compose.yaml",
        "down", "--remove-orphans",
    )
}

tasks.register<Test>("remoteTest") {
    group = "verification"
    description = "Runs the UI journeys against Playwright in Docker"
    dependsOn(":ui:bunBuild", remoteBrowserUp)
    finalizedBy(remoteBrowserDown)
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()

    workingDir = rootProject.projectDir
    environment("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")
    systemProperty("keb.example.uiDir", project(":ui").projectDir.absolutePath)
    systemProperty("keb.remoteEndpoint", "ws://127.0.0.1:3000/")
    systemProperty("keb.baseUrl", "http://host.docker.internal:4173")
    kebPropertyNames.forEach { name ->
        providers.gradleProperty(name)
            .orElse(providers.systemProperty(name))
            .orNull
            ?.let { value -> systemProperty(name, value) }
    }

    testLogging {
        events("passed", "skipped", "failed")
    }
}
