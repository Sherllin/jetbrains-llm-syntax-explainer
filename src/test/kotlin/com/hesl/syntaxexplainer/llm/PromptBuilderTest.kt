package com.hesl.syntaxexplainer.llm

import kotlin.test.Test
import kotlin.test.assertEquals

class PromptBuilderTest {
    @Test
    fun `build includes language and selection without surrounding file content`() {
        val prompt = PromptBuilder.build(
            language = "Python",
            selectedCode = "total = sum(items)",
            instruction = "Explain syntax in Chinese.",
        )

        assertEquals(
            """Explain syntax in Chinese.

Language: Python
Selected code:
```Python
total = sum(items)
```""",
            prompt,
        )
    }
}
