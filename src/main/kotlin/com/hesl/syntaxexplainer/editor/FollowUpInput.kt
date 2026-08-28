package com.hesl.syntaxexplainer.editor

import com.intellij.ide.KeyboardAwareFocusOwner
import com.intellij.ui.components.JBTextField
import java.awt.event.KeyEvent

class FollowUpInput(
    onSubmit: (String) -> Unit,
) : JBTextField(), KeyboardAwareFocusOwner {
    init {
        emptyText.text = "继续追问，按 Enter 发送"
        isEnabled = false
        addActionListener {
            val question = followUpQuestion(text, isEnabled) ?: return@addActionListener

            text = ""
            onSubmit(question)
        }
    }

    override fun skipKeyEventDispatcher(event: KeyEvent): Boolean = true
}

internal fun followUpQuestion(text: String, inputEnabled: Boolean): String? =
    text.trim().takeIf { inputEnabled && it.isNotEmpty() }
