package com.hesl.syntaxexplainer.editor

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

internal class DismissOnClickListener(
    private val onDismiss: () -> Unit,
) : MouseAdapter() {
    override fun mousePressed(event: MouseEvent) {
        onDismiss()
    }
}
