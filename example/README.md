# Keb Example

Standalone multi-module example for Keb:

- `ui` — Bun, Vite, React, and Material UI.
- `testing` — Kotlin/JUnit browser tests consuming locally published Keb
  artifacts.

From the repository root:

```shell
./gradlew publishExampleArtifacts
./gradlew -p example check
```

Start the UI:

```shell
./gradlew -p example :ui:bunDev
```

See the [detailed example guide](../docs/example.adoc).
