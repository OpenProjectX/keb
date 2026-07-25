plugins {
    base
}

val bunInstall by tasks.registering(Exec::class) {
    group = "build"
    description = "Installs the UI dependencies with Bun"
    workingDir = projectDir
    commandLine("bun", "install", "--frozen-lockfile")
    inputs.files("package.json", "bun.lock")
    outputs.dir("node_modules")
}

val bunBuild by tasks.registering(Exec::class) {
    group = "build"
    description = "Builds the React UI with Vite"
    dependsOn(bunInstall)
    workingDir = projectDir
    commandLine("bun", "run", "build")
    inputs.files(
        fileTree("src"),
        "index.html",
        "package.json",
        "tsconfig.json",
        "vite.config.ts",
    )
    outputs.dir("dist")
}

tasks.named("assemble") {
    dependsOn(bunBuild)
}

tasks.named("check") {
    dependsOn(bunBuild)
}

tasks.register<Exec>("bunDev") {
    group = "application"
    description = "Starts the Vite development server"
    dependsOn(bunInstall)
    workingDir = projectDir
    commandLine("bun", "run", "dev")
}
