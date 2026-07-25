plugins {
    kotlin("jvm") version "2.2.21"
}

val kebVersion: String by project
val kebPropertyNames = listOf(
    "keb.baseUrl",
    "keb.browser",
    "keb.headless",
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
