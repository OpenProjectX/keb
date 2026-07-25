# Keb independent example

This directory is a standalone Gradle build. It does not include the Keb source
projects and does not use a Gradle composite build. The `testing` module resolves
published Maven coordinates from `../build/example-maven`.

```text
example
├── ui        Bun + Vite + React + Material UI
└── testing   Kotlin + JUnit 5 + Keb end-to-end journeys
```

## Run everything

From the Keb repository root:

```shell
./gradlew publishExampleArtifacts
./gradlew -p example check
```

The first command publishes:

```text
org.openprojectx.test.keb:keb-core:0.1.0-SNAPSHOT
org.openprojectx.test.keb:keb-junit5:0.1.0-SNAPSHOT
```

The second command:

1. runs `bun install --frozen-lockfile`;
2. type-checks and builds the Vite production bundle;
3. starts `vite preview` for the test suite;
4. runs the Keb/JUnit journeys in Chromium;
5. stops the preview server.

The standalone build disables caching for changing Maven modules, so republished
snapshot artifacts are picked up immediately.

## UI development

```shell
./gradlew -p example :ui:bunDev
```

The application is served at `http://127.0.0.1:4173`.

Bun honors the calling environment's `.npmrc` or `bunfig.toml`; this example
does not override the npm registry.

## Headed mode and runtime properties

Run Chromium with a visible browser window without editing the test:

```shell
./gradlew -p example :testing:test -Dkeb.headless=false
```

Gradle project-property syntax is also supported:

```shell
./gradlew -p example :testing:test -Pkeb.headless=false
```

Available overrides are:

| Property | Example |
| --- | --- |
| `keb.baseUrl` | `http://127.0.0.1:4173` |
| `keb.browser` | `chromium`, `firefox`, or `webkit` |
| `keb.headless` | `true` or `false` |
| `keb.slowMoMillis` | `250` |
| `keb.actionTimeoutMillis` | `10000` |
| `keb.navigationTimeoutMillis` | `30000` |
| `keb.artifactsDirectory` | `build/keb-artifacts` |

For an easy-to-follow headed run:

```shell
./gradlew -p example :testing:test \
  -Dkeb.headless=false \
  -Dkeb.slowMoMillis=250
```

## Coverage

The UI deliberately includes:

- application bar, drawer, breadcrumbs, avatars, badges, lists, and tooltips;
- text, email, password, date, select, autocomplete, radio, checkbox, switch,
  and slider inputs;
- buttons, icon buttons, floating action buttons, links, menus, and pagination;
- cards, chips, alerts, snackbars, dialogs, progress indicators, and skeletons;
- tabs, accordions, tables, ratings, and responsive layouts;
- light and dark theme modes.

The test project organizes these controls using Keb pages and modules and
locates them primarily by accessible role, label, and visible name.
