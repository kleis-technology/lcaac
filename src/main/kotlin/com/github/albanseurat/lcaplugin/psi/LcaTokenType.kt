package com.github.albanseurat.lcaplugin.psi

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class LcaTokenType(debugName: @NonNls String) : IElementType(debugName, LcaLanguage.INSTANCE) {
    override fun toString(): String {
        return "LcaTokenType." + super.toString()
    }
}