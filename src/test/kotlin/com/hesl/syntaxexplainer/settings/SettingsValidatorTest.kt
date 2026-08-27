package com.hesl.syntaxexplainer.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SettingsValidatorTest {
    @Test
    fun `valid settings normalize trailing slash`() {
        val result = SettingsValidator.validate(
            baseUrl = " https://api.example.com/v1/ ",
            model = " demo-model ",
            apiKey = " secret ",
        )

        assertEquals(
            ValidatedSettings(
                baseUrl = "https://api.example.com/v1",
                model = "demo-model",
                apiKey = "secret",
            ),
            result,
        )
    }

    @Test
    fun `missing model reports a user facing validation error`() {
        val result = SettingsValidator.validate(
            baseUrl = "https://api.example.com/v1",
            model = " ",
            apiKey = "secret",
        )

        assertEquals("请先配置模型名称", assertIs<InvalidSettings>(result).message)
    }
}
