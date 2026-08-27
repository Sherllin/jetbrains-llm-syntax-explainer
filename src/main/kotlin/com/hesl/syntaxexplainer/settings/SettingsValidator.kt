package com.hesl.syntaxexplainer.settings

sealed interface SettingsValidation

data class ValidatedSettings(
    val baseUrl: String,
    val model: String,
    val apiKey: String,
) : SettingsValidation

data class InvalidSettings(val message: String) : SettingsValidation

object SettingsValidator {
    fun validate(baseUrl: String, model: String, apiKey: String): SettingsValidation {
        val normalizedUrl = baseUrl.trim().trimEnd('/')
        if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
            return InvalidSettings("请配置有效的 Base URL")
        }

        val normalizedModel = model.trim()
        if (normalizedModel.isEmpty()) return InvalidSettings("请先配置模型名称")

        val normalizedKey = apiKey.trim()
        if (normalizedKey.isEmpty()) return InvalidSettings("请先配置 API Key")

        return ValidatedSettings(normalizedUrl, normalizedModel, normalizedKey)
    }
}
