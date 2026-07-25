# Keb

Keb is an experimental Kotlin browser automation DSL inspired by Geb and
powered by Playwright.

The MVP focuses on compiled Kotlin DSLs in `.kt` files. Pages and modules add
structure, typing, and readable journeys while Playwright `Locator` remains the
element abstraction.

## Modules

- `keb-core` — browser lifecycle, sessions, pages, modules, content delegates,
  interactions, and verification.
- `keb-junit5` — isolated session injection and failure screenshots for JUnit 5.

## Page and module DSL

```kotlin
class LoginPage(keb: KebSession) : KebPage(keb, "/login") {
    val form by module(::LoginForm) {
        testId("login-form")
    }

    private val title by content {
        heading("Sign in")
    }

    override fun at() = verify {
        title.isVisible()
        url endsWith "/login"
    }
}

class LoginForm(
    keb: KebSession,
    root: Locator,
) : KebModule(keb, root) {
    private val username by content { label("Username") }
    private val password by content { label("Password") }
    private val signIn by content { role(AriaRole.BUTTON, "Sign in") }

    fun loginAs(user: String, secret: String): DashboardPage {
        username setTo user
        password setTo secret
        signIn.click()
        return keb.at()
    }
}

class DashboardPage(keb: KebSession) : KebPage(keb) {
    val welcome by content { testId("welcome") }

    override fun at() = verify {
        welcome.isVisible()
        url endsWith "/dashboard"
    }
}
```

Content returns native Playwright locators. Locators are lazy and re-resolve
against the current DOM; modules scope all their content beneath a root locator.

## JUnit 5

```kotlin
@KebTest(baseUrl = "https://example.test")
class LoginTest {
    @Test
    fun `a user can sign in`(keb: KebSession) = keb {
        val dashboard = to<LoginPage>()
            .form
            .loginAs("alice", "secret")

        dashboard.verify {
            dashboard.welcome hasText "Welcome, alice"
        }
        Unit
    }
}
```

`@KebTest` creates a fresh browser context and page for every test. When a test
fails, the extension attempts to save a full-page screenshot under
`build/keb-artifacts`.

Keb does not add a second implicit wait around content. Actions and verification
use Playwright's actionability checks and retrying assertions.

## Local development

Install the Chromium binary used by Playwright:

```shell
./gradlew :core:playwright --args="install chromium"
```

Run all checks:

```shell
./gradlew check
```

Runtime `.kts` scenario loading is intentionally outside MVP1. The public DSL is
ordinary compiled Kotlin, retaining IDE completion, navigation, refactoring,
and standard test discovery.

## Independent example

The [example](example/README.md) directory is a separate multi-module Gradle
build containing:

- a Bun, Vite, React, and Material UI component laboratory;
- a Kotlin/JUnit test project that consumes Keb through Maven coordinates.

Publish the current Keb artifacts and run the complete example:

```shell
./gradlew publishExampleArtifacts
./gradlew -p example check
```

Use runtime properties to override the `@KebTest` defaults:

```shell
./gradlew -p example :testing:test \
  -Dkeb.headless=false \
  -Dkeb.slowMoMillis=250
```

## Releasing

The [release workflow](.github/workflows/release.yml) runs on pushes to
`master` and can also be started manually. It verifies the library and
independent example, signs both publications, runs the Gradle Release Plugin,
and closes/releases the Sonatype staging repository.

Configure these GitHub Actions secrets:

- `OSSRH_USERNAME`
- `OSSRH_PASSWORD`
- `SIGNING_KEY_ASC`
- `SIGNING_KEY_PASSWORD`
- `RELEASE_GITHUB_TOKEN` when the default workflow token cannot push release
  commits and tags through branch protection

The workflow publishes `keb-core` and `keb-junit5` under
`org.openprojectx.test.keb`.
