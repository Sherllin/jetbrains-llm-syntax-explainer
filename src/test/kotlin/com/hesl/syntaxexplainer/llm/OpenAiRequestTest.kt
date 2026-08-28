package com.hesl.syntaxexplainer.llm

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenAiRequestTest {
    @Test
    fun `request targets chat completions and preserves message order`() {
        val body = JsonParser.parseString(
            OpenAiRequest.body(
                model = "demo",
                messages = listOf(
                    ChatMessage(ChatRole.USER, "解释 \"x\"\n下一行"),
                    ChatMessage(ChatRole.ASSISTANT, "这是变量 x。"),
                    ChatMessage(ChatRole.USER, "为什么？"),
                ),
            ),
        ).asJsonObject

        assertEquals("https://api.example.com/v1/chat/completions", OpenAiRequest.endpoint("https://api.example.com/v1"))
        assertEquals("demo", body["model"].asString)
        assertTrue(body["stream"].asBoolean)
        val messages = body["messages"].asJsonArray
        assertEquals(3, messages.size())
        assertEquals("user", messages[0].asJsonObject["role"].asString)
        assertEquals("解释 \"x\"\n下一行", messages[0].asJsonObject["content"].asString)
        assertEquals("assistant", messages[1].asJsonObject["role"].asString)
        assertEquals("这是变量 x。", messages[1].asJsonObject["content"].asString)
        assertEquals("user", messages[2].asJsonObject["role"].asString)
        assertEquals("为什么？", messages[2].asJsonObject["content"].asString)
    }
}
