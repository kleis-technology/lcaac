package com.github.albanseurat.lcaplugin.language.ide.syntax

import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class LcaBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> {
        return arrayOf(BracePair(LcaTypes.LBRACE, LcaTypes.RBRACE, true))
    }

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int {
        return openingBraceOffset;
    }
}