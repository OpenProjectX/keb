import io.github.gradlenexus.publishplugin.CloseNexusStagingRepository
import net.researchgate.release.ReleaseExtension
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.util.Base64

plugins {
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0" // nexus publish/close/release
    id("net.researchgate.release") version "3.1.0"

}

allprojects {
    group = "org.openprojectx.test.keb"
}


subprojects {
    tasks.register<DependencyReportTask>("allDependencies") {}

    // Apply to every module (safe even if a module doesn't publish)
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    // Configure publishing only when the project has a Java component (Kotlin/JVM typically applies java too)
    plugins.withId("java") {

        // ✅ Ensure required artifacts exist for Maven Central
        extensions.configure<JavaPluginExtension>("java") {
            withSourcesJar()
            withJavadocJar()
        }

        // Kotlin-only modules can produce "empty-ish" Javadoc; don't fail the build on doclint/errors
        tasks.withType(Javadoc::class.java).configureEach {
            isFailOnError = false
        }


        extensions.configure<PublishingExtension>("publishing") {
            repositories {
                maven {
                    name = "example"
                    url = rootProject.layout.buildDirectory.dir("example-maven").get().asFile.toURI()
                }
            }

            publications {
                // Create once per project
                if (findByName("mavenJava") == null) {
                    create<MavenPublication>("mavenJava") {
                        from(components["java"])

                        // Prefer explicit artifactId; by default it's project.name
                        artifactId = "keb-${project.name}"

                        pom {
                            // Module-specific name/description; override per-module if you want
                            name.set(project.name)
                            description.set("Kotlin browser automation DSL powered by Playwright")
                            url.set("https://github.com/OpenProjectX/keb")

                            licenses {
                                license {
                                    name.set("Apache License 2.0")
                                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                                }
                            }

                            developers {
                                developer {
                                    id.set("OpenProjectX")
                                    name.set("OpenProjectX")
                                    email.set("admin@openprojectx.org")
                                }
                            }

                            scm {
                                url.set("https://github.com/OpenProjectX/keb")
                                connection.set("scm:git:https://github.com/OpenProjectX/keb.git")
                                developerConnection.set("scm:git:ssh://git@github.com:OpenProjectX/keb.git")
                            }
                        }
                    }
                }
            }
        }
    }

    // Signing: only configure keys if provided (keeps local dev painless)
    extensions.configure<SigningExtension>("signing") {
        val keyFile = System.getenv("SIGNING_KEY_FILE")
        val keyPass = System.getenv("SIGNING_KEY_PASSWORD")

        if (!keyFile.isNullOrBlank()) {
            val keyText = file(keyFile).readText()
            useInMemoryPgpKeys(keyText, keyPass)

            // Sign all publications created in this subproject
            val publishing = extensions.findByType(PublishingExtension::class.java)
            if (publishing != null) {
                sign(publishing.publications)
            }
        }
    }
}

tasks.register("publishExampleArtifacts") {
    group = "publishing"
    description = "Publishes Keb modules to build/example-maven for the standalone example build"
    dependsOn(
        ":core:publishMavenJavaPublicationToExampleRepository",
        ":junit5:publishMavenJavaPublicationToExampleRepository",
    )
}


nexusPublishing {

    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))

        }
    }
}

val closeSonatypeStagingRepository =
    tasks.named<CloseNexusStagingRepository>("closeSonatypeStagingRepository")

tasks.register("requestSonatypeStagingRepositoryRelease") {
    group = "publishing"
    description = "Requests asynchronous release without polling the OSSRH compatibility status"
    dependsOn(closeSonatypeStagingRepository)

    doLast {
        val closeTask = closeSonatypeStagingRepository.get()
        val repository = closeTask.repository.get()
        val repositoryId = closeTask.stagingRepositoryId.get()
        val username = repository.username.orNull
            ?: throw GradleException("OSSRH_USERNAME is not configured")
        val password = repository.password.orNull
            ?: throw GradleException("OSSRH_PASSWORD is not configured")
        val credentials = Base64.getEncoder().encodeToString(
            "$username:$password".toByteArray(StandardCharsets.UTF_8),
        )
        val description = closeTask.repositoryDescription.get()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        val requestBody =
            """{"data":{"stagedRepositoryIds":["$repositoryId"],"description":"$description","autoDropAfterRelease":true}}"""
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${repository.nexusUrl.get()}staging/bulk/promote"))
            .header("Authorization", "Basic $credentials")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()
        val response = HttpClient.newHttpClient().send(
            request,
            HttpResponse.BodyHandlers.ofString(),
        )

        if (response.statusCode() !in 200..299) {
            throw GradleException(
                "Sonatype release request failed with HTTP ${response.statusCode()}: ${response.body()}",
            )
        }
        logger.lifecycle(
            "Sonatype accepted the asynchronous release request for staging repository '{}'",
            repositoryId,
        )
    }
}

configure<ReleaseExtension> {
    val skipPublish = providers.gradleProperty("keb.release.skipPublish")
        .map(String::toBoolean)
        .getOrElse(false)
    buildTasks.set(
        if (skipPublish) {
            emptyList()
        } else {
            listOf(
                "publishToSonatype",
                "closeSonatypeStagingRepository",
                "requestSonatypeStagingRepositoryRelease",
            )
        },
    )
    versionPropertyFile.set("gradle.properties")
    tagTemplate.set("\$name-\$version")

    with(git) {
        requireBranch.set("master")
    }
}
