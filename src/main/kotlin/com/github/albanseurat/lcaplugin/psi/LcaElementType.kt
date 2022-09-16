package com.github.albanseurat.lcaplugin.psi

import com.github.albanseurat.lcaplugin.LcaLanguage
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

class LcaElementType(debugName: @NonNls String) : IElementType(debugName, LcaLanguage.INSTANCE) {

}