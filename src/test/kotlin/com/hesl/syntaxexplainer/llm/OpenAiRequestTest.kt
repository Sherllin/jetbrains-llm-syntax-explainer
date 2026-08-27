package com.hesl.syntaxexplainer.llm

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenAiRequestTest {
    @Test
    fun `request targets chat completions and preserves prompt text`() {
        val body = JsonParser.parseString(
            OpenAiRequest.body(model = "demo", prompt = "解释 \"x\"\n下一行"),
        ).asJsonObject

        assertEquals("https://api.example.com/v1/chat/completions", OpenAiRequest.endpoint("https://api.example.com/v1"))
        assertEquals("demo", body["model"].asString)
        assertTrue(body["stream"].asBoolean)
        assertEquals("解释 \"x\"\n下一行", body["messages"].asJsonArray[0].asJsonObject["content"].asString)
    }
}
