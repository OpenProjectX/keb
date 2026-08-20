plugins {
    base
    id("org.openprojectx.gradle.dependency.bundle")
}

repositories {
    val offlineOnly = providers.gradleProperty("kebBundleOfflineOnly").isPresent
    providers.gradleProperty("kebBundleRepository").orNull?.let { repositoryPath ->
        maven {
            name = "kebBundle"
            url = uri(repositoryPath)
            metadataSources {
                gradleMetadata()
                mavenPom()
                artifact()
            }
            if (!offlineOnly) {
                content { includeGroupByRegex("org\\.openprojectx(\\..*)?") }
            }
        }
    }
    if (!offlineOnly) {
        maven { url = uri("https://plugins.gradle.org/m2") }
        mavenCentral()
    }
}

dependencyBundle {
    val dependencyBundleVersion = providers.gradleProperty("kebDependencyBundleVersion")
        .getOrElse("0.1.1")
    configurations.addAll(
        "testRuntimeClasspath",
        "allure3Package",
        "allureCommandline",
        "allureNodeDistribution",
    )
    includeBuildDependencies.set(true)
    includeSources.set(false)
    providers.gradleProperty("kebBundleOutput").orNull?.let { outputDirectory.set(file(it)) }

    // Plugin markers and Kotlin's lazily selected compiler implementation are
    // required when a derived image starts with a completely empty Gradle home.
    module("org.openprojectx.gradle.dependency.bundle:org.openprojectx.gradle.dependency.bundle.gradle.plugin:$dependencyBundleVersion")
    module("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.2.21")
    module("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21@pom")
    module("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21@jar")
    module("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21@module")
    module("org.jetbrains.kotlin:fus-statistics-gradle-plugin:2.2.21@module")
    module("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.2.21@module")
    module("org.jetbrains.kotlin:kotlin-gradle-plugins-bom:2.2.21@pom")
    module("com.google.code.gson:gson-parent:2.11.0@pom")
    module("com.google.errorprone:error_prone_parent:2.27.0@pom")
    module("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.8.0@pom")
    module("org.apiguardian:apiguardian-api:1.1.2")
    module("io.qameta.allure-report:io.qameta.allure-report.gradle.plugin:4.1.0")
    module("org.jetbrains.kotlin:kotlin-build-tools-impl:2.2.21")
    // The dependency-bundle plugin itself is built with Kotlin 2.3.0. Gradle
    // resolves that plugin before the export task can observe its classpath,
    // so include the runtime pieces explicitly for a truly empty Gradle home.
    module("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    module("org.jetbrains.kotlin:kotlin-stdlib:2.3.0@pom")
    module("org.jetbrains.kotlin:kotlin-reflect:2.3.0")
    module("org.jetbrains.kotlin:kotlin-reflect:2.3.0@pom")
    module("org.jetbrains:annotations:13.0")
    module("org.jetbrains:annotations:13.0@pom")

    // A second export invocation enriches every component captured by the
    // first pass with conventional Maven metadata. This is necessary for a
    // file-backed repository consumed from an otherwise empty Gradle home.
    providers.gradleProperty("kebBundleOutput").orNull
        ?.let(::file)
        ?.resolve("dependency-graph.json")
        ?.takeIf { it.isFile }
        ?.readLines()
        ?.asSequence()
        ?.map(String::trim)
        ?.filter { it.startsWith('"') && it.endsWith(" : {") }
        ?.map { it.substringAfter('"').substringBefore('"') }
        ?.filter { coordinate -> coordinate.count { it == ':' } == 2 }
        // Artifact-only distributions (for example Node's tarball) have no
        // Maven POM and are already complete after the first capture.
        ?.filterNot { coordinate -> coordinate.startsWith("org.nodejs:") }
        ?.distinct()
        ?.forEach { coordinate -> module("$coordinate@pom") }
}

tasks.named("check") {
    dependsOn(":ui:bunBuild", ":testing:test")
}
