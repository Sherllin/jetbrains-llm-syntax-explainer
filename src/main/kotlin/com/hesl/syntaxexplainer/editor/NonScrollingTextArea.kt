package com.hesl.syntaxexplainer.editor

import java.awt.Rectangle
import javax.swing.JTextArea

internal class NonScrollingTextArea(text: String) : JTextArea(text) {
    override fun scrollRectToVisible(rectangle: Rectangle) = Unit
}
