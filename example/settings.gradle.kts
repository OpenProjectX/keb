pluginManagement {
    resolutionStrategy.eachPlugin {
        if (requested.id.id == "org.openprojectx.gradle.dependency.bundle") {
            useVersion(
                providers.gradleProperty("kebDependencyBundleVersion")
                    .getOrElse("0.1.1"),
            )
        }
    }
    repositories {
        val offlineOnly = providers.gradleProperty("kebBundleOfflineOnly").isPresent
        providers.gradleProperty("kebBundleRepository").orNull?.let {
            maven {
                url = uri(it)
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
            gradlePluginPortal()
            mavenCentral()
        }
    }
}

dependencyResolutionManagement {
    repositories {
        val offlineOnly = providers.gradleProperty("kebBundleOfflineOnly").isPresent
        val bundleRepository = providers.gradleProperty("kebBundleRepository").orNull
        if (bundleRepository != null) {
            maven {
                name = "kebBundle"
                url = uri(bundleRepository)
                metadataSources {
                    gradleMetadata()
                    mavenPom()
                    artifact()
                }
                if (!offlineOnly) {
                    content { includeGroupByRegex("org\\.openprojectx(\\..*)?") }
                }
            }
        } else {
            maven {
                name = "kebLocal"
                url = uri("../build/example-maven")
                content {
                    includeGroup("org.openprojectx.test.keb")
                }
            }
        }
        if (!offlineOnly) {
            mavenCentral()
        }
    }
}

rootProject.name = "keb-example"

include("ui")
include("testing")
