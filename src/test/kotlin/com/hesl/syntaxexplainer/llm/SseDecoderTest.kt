package com.hesl.syntaxexplainer.llm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SseDecoderTest {
    @Test
    fun `decode returns escaped content delta`() {
        val line = """data: {"choices":[{"delta":{"content":"第一行\n\"说明\""}}]}"""

        assertEquals("第一行\n\"说明\"", SseDecoder.decode(line))
    }

    @Test
    fun `decode ignores done marker and non data lines`() {
        assertNull(SseDecoder.decode("data: [DONE]"))
        assertNull(SseDecoder.decode(": keep-alive"))
    }
}
