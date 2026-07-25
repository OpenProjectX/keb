package org.openprojectx.keb.example

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit

abstract class ExampleUiServer {
    companion object {
        const val BASE_URL = "http://127.0.0.1:4173"

        private var process: Process? = null

        @JvmStatic
        @BeforeAll
        fun startUi() {
            val uiDirectory = Path.of(
                checkNotNull(System.getProperty("keb.example.uiDir")) {
                    "The keb.example.uiDir system property is required"
                },
            ).toFile()

            process = ProcessBuilder("bun", "run", "preview")
                .directory(uiDirectory)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start()

            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build()
            val request = HttpRequest.newBuilder(URI(BASE_URL))
                .timeout(Duration.ofSeconds(1))
                .GET()
                .build()

            val deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos()
            while (System.nanoTime() < deadline) {
                val running = process?.isAlive == true
                check(running) { "Vite preview exited before becoming ready" }

                val ready = runCatching {
                    client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200
                }.getOrDefault(false)
                if (ready) return

                Thread.sleep(100)
            }

            error("Vite preview did not become ready at $BASE_URL")
        }

        @JvmStatic
        @AfterAll
        fun stopUi() {
            val running = process ?: return
            running.descendants().forEach(ProcessHandle::destroy)
            running.destroy()
            if (!running.waitFor(5, TimeUnit.SECONDS)) {
                running.descendants().forEach(ProcessHandle::destroyForcibly)
                running.destroyForcibly()
            }
            process = null
        }
    }
}
