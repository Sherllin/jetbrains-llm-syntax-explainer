package com.hesl.syntaxexplainer.llm

import com.google.gson.JsonArray
import com.google.gson.JsonObject

object OpenAiRequest {
    fun endpoint(baseUrl: String): String = "${baseUrl.trimEnd('/')}/chat/completions"

    fun body(model: String, prompt: String): String =
        body(model, listOf(ChatMessage(ChatRole.USER, prompt)))

    fun body(model: String, messages: List<ChatMessage>): String {
        return JsonObject().apply {
            addProperty("model", model)
            addProperty("stream", true)
            add("messages", JsonArray().apply {
                messages.forEach { message ->
                    add(JsonObject().apply {
                        addProperty("role", message.role.apiValue)
                        addProperty("content", message.content)
                    })
                }
            })
        }.toString()
    }
}
