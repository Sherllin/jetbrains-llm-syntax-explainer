package com.hesl.syntaxexplainer.editor

import java.awt.event.MouseEvent
import javax.swing.JPanel
import kotlin.test.Test
import kotlin.test.assertEquals

class DismissOnClickListenerTest {
    @Test
    fun `mouse press dismisses the conversation`() {
        var dismissCount = 0
        val component = JPanel().apply {
            addMouseListener(DismissOnClickListener { dismissCount++ })
        }

        component.dispatchEvent(
            MouseEvent(
                component,
                MouseEvent.MOUSE_PRESSED,
                0,
                0,
                10,
                10,
                1,
                false,
            ),
        )

        assertEquals(1, dismissCount)
    }
}
