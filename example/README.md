# Keb Example

Standalone multi-module example for Keb:

- `ui` — Bun, Vite, React, and Material UI.
- `testing` — Kotlin/JUnit browser tests consuming locally published Keb
  artifacts, including a business-readable Allure BDD journey.

From the repository root:

```shell
./gradlew publishExampleArtifacts
./gradlew -p example check
```

Force every task to run and bypass the Gradle build cache:

```shell
./gradlew -p example check --rerun-tasks --no-build-cache
```

Add `--refresh-dependencies` only when Gradle must also resolve dependencies
again.

Run the tests with a visible browser:

```shell
./gradlew -p example :testing:test -Dkeb.headless=false
```

Keep a video only when a test fails:

```shell
./gradlew -p example :testing:test -Dkeb.video=retain-on-failure
```

Run the same tests against Playwright in Docker:

```shell
./gradlew -p example :testing:remoteTest
```

Run the tests and generate the Allure HTML report:

```shell
./gradlew -p example :testing:allureReport
```

Open `example/testing/build/reports/allure-report/allureReport/index.html`.
Raw results remain in `example/testing/build/allure-results`. See the
[reporting guide](../docs/reporting.adoc) for the Kotlin journey DSL.

Start the UI:

```shell
./gradlew -p example :ui:bunDev
```

See the [detailed example guide](../docs/example.adoc).
