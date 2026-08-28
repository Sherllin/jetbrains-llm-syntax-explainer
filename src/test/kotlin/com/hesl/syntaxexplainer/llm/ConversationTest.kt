package com.hesl.syntaxexplainer.llm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConversationTest {
    @Test
    fun `follow-up appends transcript and sends complete conversation history`() {
        val conversation = Conversation("initial code prompt")

        conversation.appendAssistantDelta("首次回答")
        assertTrue(conversation.completeAssistant())
        assertTrue(conversation.beginFollowUp("为什么？"))

        assertEquals(
            listOf(
                ChatMessage(ChatRole.USER, "initial code prompt"),
                ChatMessage(ChatRole.ASSISTANT, "首次回答"),
                ChatMessage(ChatRole.USER, "为什么？"),
            ),
            conversation.messages(),
        )
        assertEquals("首次回答\n\n你：为什么？\n\nLLM：", conversation.transcript())

        conversation.appendAssistantDelta("因为这是一次函数调用。")
        assertTrue(conversation.completeAssistant())
        assertEquals(
            ChatMessage(ChatRole.ASSISTANT, "因为这是一次函数调用。"),
            conversation.messages().last(),
        )
        assertEquals("首次回答\n\n你：为什么？\n\nLLM：因为这是一次函数调用。", conversation.transcript())
    }

    @Test
    fun `follow-up is rejected while streaming or when input is blank`() {
        val conversation = Conversation("initial code prompt")

        assertFalse(conversation.beginFollowUp("还没回答完"))
        conversation.appendAssistantDelta("首次回答")
        assertTrue(conversation.completeAssistant())
        assertFalse(conversation.beginFollowUp("   "))

        assertEquals("首次回答", conversation.transcript())
        assertEquals(2, conversation.messages().size)
    }

    @Test
    fun `failed response is shown but not stored as an assistant message`() {
        val conversation = Conversation("initial code prompt")

        conversation.failAssistant("请求失败，请重试。")
        assertTrue(conversation.beginFollowUp("换一种方式解释"))

        assertEquals(
            listOf(
                ChatMessage(ChatRole.USER, "initial code prompt"),
                ChatMessage(ChatRole.USER, "换一种方式解释"),
            ),
            conversation.messages(),
        )
        assertEquals("请求失败，请重试。\n\n你：换一种方式解释\n\nLLM：", conversation.transcript())
    }

    @Test
    fun `empty response still allows a follow-up`() {
        val conversation = Conversation("initial code prompt")

        assertFalse(conversation.completeAssistant())
        assertTrue(conversation.beginFollowUp("请重新解释"))
        assertEquals(
            listOf(
                ChatMessage(ChatRole.USER, "initial code prompt"),
                ChatMessage(ChatRole.USER, "请重新解释"),
            ),
            conversation.messages(),
        )
    }
}
