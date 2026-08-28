package com.hesl.syntaxexplainer.llm

import com.intellij.util.concurrency.AppExecutorUtil
import com.hesl.syntaxexplainer.settings.ValidatedSettings
import java.io.Closeable
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReference

class OpenAiStreamClient(
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build(),
) {
    fun stream(
        settings: ValidatedSettings,
        prompt: String,
        onDelta: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit,
    ): StreamHandle = stream(
        settings = settings,
        messages = listOf(ChatMessage(ChatRole.USER, prompt)),
        onDelta = onDelta,
        onComplete = onComplete,
        onError = onError,
    )

    fun stream(
        settings: ValidatedSettings,
        messages: List<ChatMessage>,
        onDelta: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (String) -> Unit,
    ): StreamHandle {
        val input = AtomicReference<InputStream?>()
        val future = AppExecutorUtil.getAppExecutorService().submit {
            runCatching {
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(OpenAiRequest.endpoint(settings.baseUrl)))
                    .timeout(Duration.ofSeconds(90))
                    .header("Authorization", "Bearer ${settings.apiKey}")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(OpenAiRequest.body(settings.model, messages)))
                    .build()
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
                input.set(response.body())

                if (response.statusCode() !in 200..299) {
                    val detail = response.body().bufferedReader().use { it.readText().take(300) }
                    error("请求失败 (${response.statusCode()}): $detail")
                }

                response.body().bufferedReader().useLines { lines ->
                    lines.mapNotNull(SseDecoder::decode).forEach(onDelta)
                }
                onComplete()
            }.onFailure { error ->
                if (!Thread.currentThread().isInterrupted) {
                    onError(error.message ?: "LLM 请求失败")
                }
            }
        }
        return StreamHandle(future, input)
    }
}

class StreamHandle internal constructor(
    private val future: Future<*>,
    private val input: AtomicReference<InputStream?>,
) : Closeable {
    override fun close() {
        input.getAndSet(null)?.close()
        future.cancel(true)
    }
}
