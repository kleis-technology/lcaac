package ch.kleis.lcaplugin.language.psi.type.block

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiBlockProducts : PsiElement {
    fun getExchanges(): Collection<PsiTechnoProductExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.TECHNO_PRODUCT_EXCHANGE))
            .map { it.psi as PsiTechnoProductExchange }
    }
}
