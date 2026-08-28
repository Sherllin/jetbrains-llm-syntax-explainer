package com.hesl.syntaxexplainer.editor

import java.awt.Rectangle
import javax.swing.JPanel
import kotlin.test.Test
import kotlin.test.assertEquals

class NonScrollingTextAreaTest {
    @Test
    fun `output scroll requests do not propagate to the editor viewport`() {
        val parent = TrackingPanel()
        val output = NonScrollingTextArea("正在解析…")
        parent.add(output)

        output.scrollRectToVisible(Rectangle(0, 200, 20, 20))

        assertEquals(0, parent.scrollRequests)
    }

    private class TrackingPanel : JPanel() {
        var scrollRequests = 0

        override fun scrollRectToVisible(rectangle: Rectangle) {
            scrollRequests++
            super.scrollRectToVisible(rectangle)
        }
    }
}
