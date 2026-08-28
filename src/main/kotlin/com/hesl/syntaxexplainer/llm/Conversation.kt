package com.hesl.syntaxexplainer.llm

enum class ChatRole(val apiValue: String) {
    USER("user"),
    ASSISTANT("assistant"),
}

data class ChatMessage(
    val role: ChatRole,
    val content: String,
)

class Conversation(initialPrompt: String) {
    private val messages = mutableListOf(ChatMessage(ChatRole.USER, initialPrompt))
    private val transcript = StringBuilder()
    private val currentAssistant = StringBuilder()
    private var awaitingAssistant = true

    fun appendAssistantDelta(delta: String) {
        if (!awaitingAssistant) return
        currentAssistant.append(delta)
        transcript.append(delta)
    }

    fun completeAssistant(): Boolean {
        if (!awaitingAssistant) return false
        awaitingAssistant = false
        val answer = currentAssistant.toString()
        currentAssistant.clear()
        if (answer.isBlank()) return false
        messages += ChatMessage(ChatRole.ASSISTANT, answer)
        return true
    }

    fun failAssistant(message: String) {
        if (!awaitingAssistant) return
        awaitingAssistant = false
        if (currentAssistant.isNotEmpty()) transcript.append("\n\n")
        currentAssistant.clear()
        transcript.append(message)
    }

    fun beginFollowUp(question: String): Boolean {
        val content = question.trim()
        if (awaitingAssistant || content.isEmpty()) return false
        messages += ChatMessage(ChatRole.USER, content)
        transcript.append("\n\n你：").append(content).append("\n\nLLM：")
        awaitingAssistant = true
        return true
    }

    fun messages(): List<ChatMessage> = messages.toList()

    fun transcript(): String = transcript.toString()
}
