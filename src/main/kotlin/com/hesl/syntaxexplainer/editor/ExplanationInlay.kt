package com.hesl.syntaxexplainer.editor

import com.intellij.openapi.editor.ComponentInlayAlignment
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.InlayProperties
import com.intellij.openapi.editor.addComponentInlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel

class ExplanationInlay(
    private val editor: Editor,
    offset: Int,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    private val view = View(editor, onSubmit, onDismiss)
    private val inlay = editor.addComponentInlay(
        offset = offset,
        properties = InlayProperties()
            .relatesToPrecedingText(true)
            .showAbove(false)
            .priority(0),
        component = view,
        alignment = ComponentInlayAlignment.FIT_VIEWPORT_X_SPAN,
    )

    fun update(text: String, inputEnabled: Boolean) {
        view.update(text, inputEnabled)
        inlay?.update()
    }

    fun dispose() {
        disposeInlay(inlay)
    }

    private class View(
        private val editor: Editor,
        onSubmit: (String) -> Unit,
        onDismiss: () -> Unit,
    ) : JPanel(BorderLayout(0, JBUI.scale(8))) {
        private val output = NonScrollingTextArea("正在解析…").apply {
            isEditable = false
            isFocusable = false
            lineWrap = true
            wrapStyleWord = true
            isOpaque = false
            border = null
            font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
            foreground = JBColor(0x30343B, 0xDFE1E5)
        }
        private val input = FollowUpInput(onSubmit)
        private val dismissOnClick = DismissOnClickListener(onDismiss)

        init {
            isOpaque = false
            border = JBUI.Borders.empty(12)
            minimumSize = Dimension(JBUI.scale(260), 0)
            addMouseListener(dismissOnClick)
            output.addMouseListener(dismissOnClick)
            add(output, BorderLayout.CENTER)
            add(input, BorderLayout.SOUTH)
        }

        fun update(text: String, inputEnabled: Boolean) {
            output.text = text
            input.isEnabled = inputEnabled
            revalidate()
            parent?.revalidate()
            repaint()
        }

        override fun getPreferredSize(): Dimension {
            val width = (editor.scrollingModel.visibleArea.width - JBUI.scale(32)).coerceIn(
                JBUI.scale(260),
                JBUI.scale(760),
            )
            val contentWidth = (width - insets.left - insets.right).coerceAtLeast(JBUI.scale(120))
            output.setSize(contentWidth, Short.MAX_VALUE.toInt())
            val outputHeight = output.preferredSize.height.coerceAtLeast(editor.lineHeight)
            return Dimension(
                width,
                insets.top + outputHeight + JBUI.scale(8) + input.preferredSize.height + insets.bottom,
            )
        }

        override fun paintComponent(graphics: Graphics) {
            val g = graphics.create() as Graphics2D
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g.color = JBColor(0xF3F6FA, 0x2B2D30)
                g.fillRoundRect(0, JBUI.scale(4), width, height - JBUI.scale(8), JBUI.scale(10), JBUI.scale(10))
            } finally {
                g.dispose()
            }
            super.paintComponent(graphics)
        }
    }
}
