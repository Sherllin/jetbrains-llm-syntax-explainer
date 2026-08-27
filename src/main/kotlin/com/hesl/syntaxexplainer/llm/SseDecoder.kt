package com.hesl.syntaxexplainer.llm

import com.google.gson.JsonParser

object SseDecoder {
    fun decode(line: String): String? {
        if (!line.startsWith("data:")) return null
        val data = line.removePrefix("data:").trim()
        if (data == "[DONE]") return null

        return runCatching {
            JsonParser.parseString(data)
                .asJsonObject["choices"].asJsonArray[0].asJsonObject
                .getAsJsonObject("delta")["content"]
                ?.takeUnless { it.isJsonNull }
                ?.asString
        }.getOrNull()
    }
}
