package com.hesl.syntaxexplainer.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import kotlin.test.Test
import kotlin.test.assertEquals

class InlayDisposerTest {
    @Test
    fun `disposing an inlay runs its registered component cleanup`() {
        val inlay = Disposer.newDisposable()
        var cleanupCount = 0
        Disposer.register(inlay, Disposable { cleanupCount++ })

        disposeInlay(inlay)

        assertEquals(1, cleanupCount)
    }
}
