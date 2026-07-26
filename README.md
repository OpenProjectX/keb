# Keb

Keb is a Kotlin browser automation DSL inspired by Geb and powered by
[Playwright](https://playwright.dev/java/).

It combines typed page objects, reusable modules, Kotlin property delegates, and
Playwright's native locators and auto-waiting.

## Modules

- `keb-core` — browser lifecycle and the page/module DSL.
- `keb-junit5` — JUnit 5 session injection and failure screenshots.

## Quick start

```kotlin
dependencies {
    testImplementation("org.openprojectx.test.keb:keb-junit5:<version>")
}
```

```kotlin
class LoginPage(keb: KebSession) : KebPage(keb, "/login") {
    val username by content { label("Username") }
    val password by content { label("Password") }
    val signIn by content { role(AriaRole.BUTTON, "Sign in") }

    override fun at() = verify {
        heading("Sign in").isVisible()
    }
}

@KebTest(baseUrl = "https://example.test")
class LoginTest {
    @Test
    fun `user can sign in`(keb: KebSession) = keb {
        val page = to<LoginPage>()
        page.username setTo "alice"
        page.password setTo "secret"
        page.signIn.click()
        Unit
    }
}
```

## Documentation

See the [Keb documentation](docs/index.adoc) for setup, DSL usage,
configuration, the independent example, and releasing.

## Build

```shell
./gradlew :core:playwright --args="install chromium"
./gradlew check
```

Run the independent React/Material UI example:

```shell
./gradlew publishExampleArtifacts
./gradlew -p example check
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Keb is licensed under the [Apache License 2.0](LICENSE).
