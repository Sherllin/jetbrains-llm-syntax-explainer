package com.hesl.syntaxexplainer.editor

import kotlin.test.Test
import kotlin.test.assertEquals

class FollowUpInputTest {
    @Test
    fun `enabled input returns trimmed question`() {
        assertEquals("为什么这样写？", followUpQuestion("  为什么这样写？  ", inputEnabled = true))
    }

    @Test
    fun `blank or disabled input is ignored`() {
        assertEquals(null, followUpQuestion("   ", inputEnabled = true))
        assertEquals(null, followUpQuestion("追问", inputEnabled = false))
    }
}
