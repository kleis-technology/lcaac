package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiParameters : PsiElement {
    fun getParameters(): Collection<PsiParameter> {
        return node.getChildren(TokenSet.create(LcaTypes.PARAMETER))
            .map { it.psi as PsiParameter }
    }
}
