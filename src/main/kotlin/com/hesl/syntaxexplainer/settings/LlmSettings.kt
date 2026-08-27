package com.hesl.syntaxexplainer.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.BaseState

private const val DEFAULT_PROMPT = """请用简洁中文解析所选代码的语法结构、逐段含义和关键语言特性。只解释所选代码，不猜测文件中未提供的内容。"""

@State(name = "LlmSyntaxExplainerSettings", storages = [Storage("llm-syntax-explainer.xml")])
@Service(Service.Level.APP)
class LlmSettings : SimplePersistentStateComponent<LlmSettings.Data>(Data()) {
    class Data : BaseState() {
        var baseUrl by string("https://api.openai.com/v1")
        var model by string("")
        var autoExplain by property(true)
        var debounceMs by property(600)
        var maxSelectionChars by property(12_000)
        var prompt by string(DEFAULT_PROMPT)
    }

    companion object {
        fun getInstance(): LlmSettings = service()
        fun defaultPrompt(): String = DEFAULT_PROMPT
    }
}

object ApiKeyStore {
    private val attributes = CredentialAttributes(
        generateServiceName("LLM Syntax Explainer", "OpenAI-compatible API key"),
    )

    fun load(): String = PasswordSafe.instance.getPassword(attributes).orEmpty()

    fun save(value: String) {
        PasswordSafe.instance.setPassword(attributes, value.trim().ifEmpty { null })
    }

    fun loadAsync(onLoaded: (String) -> Unit) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val value = load()
            ApplicationManager.getApplication().invokeLater { onLoaded(value) }
        }
    }

    fun saveAsync(value: String) {
        ApplicationManager.getApplication().executeOnPooledThread { save(value) }
    }
}
