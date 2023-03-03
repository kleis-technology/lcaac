package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiEmissionFactors: PsiElement {
    fun getExchanges(): Collection<PsiExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.EXCHANGE))
            .map { it.psi as PsiExchange }
    }
}