plugins {
    base
}

tasks.named("check") {
    dependsOn(":ui:bunBuild", ":testing:test")
}
