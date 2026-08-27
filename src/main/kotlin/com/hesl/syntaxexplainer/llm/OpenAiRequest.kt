package com.hesl.syntaxexplainer.llm

import com.google.gson.JsonArray
import com.google.gson.JsonObject

object OpenAiRequest {
    fun endpoint(baseUrl: String): String = "${baseUrl.trimEnd('/')}/chat/completions"

    fun body(model: String, prompt: String): String {
        val message = JsonObject().apply {
            addProperty("role", "user")
            addProperty("content", prompt)
        }
        return JsonObject().apply {
            addProperty("model", model)
            addProperty("stream", true)
            add("messages", JsonArray().apply { add(message) })
        }.toString()
    }
}
