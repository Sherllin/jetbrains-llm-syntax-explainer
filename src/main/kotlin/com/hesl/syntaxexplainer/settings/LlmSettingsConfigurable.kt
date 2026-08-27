package com.hesl.syntaxexplainer.settings

import com.hesl.syntaxexplainer.llm.OpenAiStreamClient
import com.intellij.openapi.options.Configurable
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.SwingUtilities

class LlmSettingsConfigurable : Configurable {
    private val baseUrl = JBTextField()
    private val apiKey = JBPasswordField()
    private val model = JBTextField()
    private val autoExplain = JBCheckBox("选中代码后自动解析")
    private val connectionStatus = JBLabel()
    private val testConnection = JButton("测试连接").apply {
        addActionListener { testConnection() }
    }
    private val debounceMs = JSpinner(SpinnerNumberModel(600, 100, 5_000, 100))
    private val maxChars = JSpinner(SpinnerNumberModel(12_000, 100, 100_000, 500))
    private val prompt = JBTextArea(5, 48).apply {
        lineWrap = true
        wrapStyleWord = true
    }
    private var panel: JPanel? = null
    private var loadedApiKey = ""

    override fun getDisplayName(): String = "Code Syntax LLM"

    override fun createComponent(): JComponent {
        val note = JBLabel("API Key 保存在 JetBrains 安全凭据存储中，不写入项目。")
        note.foreground = JBColor.GRAY
        prompt.minimumSize = Dimension(420, 100)
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Base URL:", baseUrl)
            .addLabeledComponent("API Key:", apiKey)
            .addLabeledComponent("Model:", model)
            .addComponent(note)
            .addLabeledComponent("连接:", testConnection)
            .addComponent(connectionStatus)
            .addSeparator()
            .addComponent(autoExplain)
            .addLabeledComponent("选区稳定时间 (ms):", debounceMs)
            .addLabeledComponent("最大选区字符数:", maxChars)
            .addLabeledComponentFillVertically("解析提示词:", prompt)
            .addComponentFillVertically(JPanel(), 0)
            .panel
        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        val state = LlmSettings.getInstance().state
        return baseUrl.text.trim() != state.baseUrl ||
            model.text.trim() != state.model ||
            autoExplain.isSelected != state.autoExplain ||
            debounceMs.value != state.debounceMs ||
            maxChars.value != state.maxSelectionChars ||
            prompt.text.trim() != state.prompt ||
            String(apiKey.password) != loadedApiKey
    }

    override fun apply() {
        val state = LlmSettings.getInstance().state
        state.baseUrl = baseUrl.text.trim().trimEnd('/')
        state.model = model.text.trim()
        state.autoExplain = autoExplain.isSelected
        state.debounceMs = debounceMs.value as Int
        state.maxSelectionChars = maxChars.value as Int
        state.prompt = prompt.text.trim().ifEmpty { LlmSettings.defaultPrompt() }
        loadedApiKey = String(apiKey.password)
        ApiKeyStore.saveAsync(loadedApiKey)
    }

    override fun reset() {
        val state = LlmSettings.getInstance().state
        baseUrl.text = state.baseUrl
        model.text = state.model
        autoExplain.isSelected = state.autoExplain
        debounceMs.value = state.debounceMs
        maxChars.value = state.maxSelectionChars
        prompt.text = state.prompt
        ApiKeyStore.loadAsync {
            loadedApiKey = it
            apiKey.text = it
        }
    }

    override fun disposeUIResources() {
        panel = null
    }

    private fun testConnection() {
        when (val validation = SettingsValidator.validate(baseUrl.text, model.text, String(apiKey.password))) {
            is InvalidSettings -> connectionStatus.text = validation.message
            is ValidatedSettings -> {
                testConnection.isEnabled = false
                connectionStatus.text = "正在连接…"
                OpenAiStreamClient().stream(
                    settings = validation,
                    prompt = "只回复 OK",
                    onDelta = {},
                    onComplete = { showConnectionResult("连接成功") },
                    onError = { showConnectionResult(it) },
                )
            }
        }
    }

    private fun showConnectionResult(message: String) {
        SwingUtilities.invokeLater {
            connectionStatus.text = message
            testConnection.isEnabled = true
        }
    }
}
