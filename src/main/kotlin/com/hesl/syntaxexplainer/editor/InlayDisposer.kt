package com.hesl.syntaxexplainer.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer

internal fun disposeInlay(inlay: Disposable?) {
    if (inlay != null) Disposer.dispose(inlay)
}
