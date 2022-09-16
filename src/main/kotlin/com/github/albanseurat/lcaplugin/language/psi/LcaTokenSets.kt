package com.github.albanseurat.lcaplugin.language.psi

import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.psi.tree.TokenSet

object LcaTokenSets {
    val IDENTIFIERS = TokenSet.create(LcaTypes.IDENTIFIER)
}
