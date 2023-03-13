package ch.kleis.lcaplugin.language.psi.type.block

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiImpactExchange
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiBlockImpacts : PsiElement {
    fun getExchanges(): Collection<PsiImpactExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.IMPACT_EXCHANGE))
            .map { it.psi as PsiImpactExchange }
    }
}
