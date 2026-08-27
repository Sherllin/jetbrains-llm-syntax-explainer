package com.hesl.syntaxexplainer.llm

object PromptBuilder {
    fun build(language: String, selectedCode: String, instruction: String): String =
        """${instruction.trim()}

Language: ${language.trim()}
Selected code:
```${language.trim()}
${selectedCode.trim()}
```"""
}
