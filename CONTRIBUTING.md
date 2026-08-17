# Contributing to Keb

Thank you for helping improve Keb.

## Development setup

Requirements:

- JDK 17 or newer
- Bun 1.3.x for the example UI
- Git

Install Chromium and run the library checks:

```shell
./gradlew :core:playwright --args="install chromium"
./gradlew check
```

## Making changes

1. Create a focused branch from `master`.
2. Keep changes small and scoped to one concern.
3. Add or update tests for observable behavior.
4. Update the AsciiDoc guides when public APIs or workflows change.
5. Run the relevant checks before opening a pull request.

Do not commit generated output, browser binaries, `node_modules`, build
directories, credentials, or signing material.

## Kotlin and API guidelines

- Target JVM 17.
- Follow the official Kotlin style.
- Prefer explicit, typed APIs over runtime magic.
- Keep Playwright `Locator`, `Page`, and `BrowserContext` accessible.
- Use Playwright auto-waiting and retrying assertions instead of adding hidden
  polling layers.
- Prefer roles, labels, visible text, and test IDs over structural selectors.
- Preserve source and binary compatibility when practical.
- Add KDoc for public APIs whose behavior is not evident from their signature.

Run the core, JUnit, and reporting tests:

```shell
./gradlew :core:test :junit5:test :allure:test
```

## Independent example

Publish the current modules before testing the standalone consumer:

```shell
./gradlew publishExampleArtifacts
./gradlew -p example check
```

For UI development:

```shell
./gradlew -p example :ui:bunDev
```

Use `bun install`; Bun honors the developer or CI registry configuration. Keep
`example/ui/bun.lock` synchronized with `package.json`.

## Documentation

Keep the root README short. Detailed documentation belongs in AsciiDoc files
under `docs/`.

Check local links and formatting, and keep examples aligned with the tested
public API.

## Pull requests

A pull request should include:

- a clear description of the problem and solution;
- tests or an explanation of why tests are unnecessary;
- documentation for user-visible changes;
- notes about compatibility or migration concerns;
- no unrelated formatting or generated-file changes.

All checks must pass before merge. Releases are performed by the repository
release workflow; contributors should not change release versions in feature
pull requests.

## Reporting issues

Include the Keb version, Kotlin/JDK versions, browser and operating system,
minimal reproduction, expected behavior, actual behavior, and relevant logs or
failure artifacts.

## License

By contributing, you agree that your contributions are licensed under the
[Apache License 2.0](LICENSE).
