pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            name = "kebLocal"
            url = uri("../build/example-maven")
            content {
                includeGroup("org.openprojectx.test.keb")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "keb-example"

include("ui")
include("testing")
