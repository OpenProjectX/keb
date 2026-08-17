plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    api(project(":junit5"))
    implementation(libs.allureJavaCommons)
    runtimeOnly(libs.allureJupiter)

    testImplementation(libs.allureJupiter)
    testRuntimeOnly(libs.junitJupiterEngine)
    testRuntimeOnly(libs.junitPlatformLauncher)
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.17")
}
