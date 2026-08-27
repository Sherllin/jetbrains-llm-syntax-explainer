package com.hesl.syntaxexplainer.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Graphics2D

class ExplanationInlay(private val editor: Editor, offset: Int) {
    private val renderer = Renderer(editor)
    private val inlay = editor.inlayModel.addBlockElement(offset, true, false, 0, renderer)

    fun update(text: String) {
        renderer.text = text
        inlay?.update()
    }

    fun dispose() {
        inlay?.dispose()
    }

    private class Renderer(private val editor: Editor) : EditorCustomElementRenderer {
        var text: String = "正在解析…"

        override fun calcWidthInPixels(inlay: Inlay<*>): Int =
            (editor.contentComponent.width - 32).coerceIn(260, 760)

        override fun calcHeightInPixels(inlay: Inlay<*>): Int {
            val metrics = editor.contentComponent.getFontMetrics(editor.colorsScheme.getFont(EditorFontType.PLAIN))
            return lines(metrics, calcWidthInPixels(inlay) - 24).size * metrics.height + 20
        }

        override fun paint(
            inlay: Inlay<*>,
            graphics: Graphics,
            targetRegion: Rectangle,
            textAttributes: TextAttributes,
        ) {
            val g = graphics.create() as Graphics2D
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g.color = JBColor(0xF3F6FA, 0x2B2D30)
                g.fillRoundRect(targetRegion.x, targetRegion.y + 4, targetRegion.width, targetRegion.height - 8, 10, 10)
                g.font = editor.colorsScheme.getFont(EditorFontType.PLAIN)
                g.color = JBColor(0x30343B, 0xDFE1E5)
                val metrics = g.fontMetrics
                lines(metrics, targetRegion.width - 24).forEachIndexed { index, line ->
                    g.drawString(line, targetRegion.x + 12, targetRegion.y + 12 + metrics.ascent + index * metrics.height)
                }
            } finally {
                g.dispose()
            }
        }

        private fun lines(metrics: FontMetrics, width: Int): List<String> {
            val result = mutableListOf<String>()
            text.lines().forEach { paragraph ->
                if (paragraph.isEmpty()) {
                    result += ""
                    return@forEach
                }
                val line = StringBuilder()
                paragraph.forEach { char ->
                    if (line.isNotEmpty() && metrics.stringWidth(line.toString() + char) > width) {
                        result += line.toString()
                        line.clear()
                    }
                    line.append(char)
                }
                result += line.toString()
            }
            return result.ifEmpty { listOf("") }
        }
    }
}
