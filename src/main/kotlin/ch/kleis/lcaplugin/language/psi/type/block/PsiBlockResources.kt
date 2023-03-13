package ch.kleis.lcaplugin.language.psi.type.block

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiBlockResources : PsiElement {
    fun getExchanges(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BIO_EXCHANGE))
            .map { it.psi as PsiBioExchange }
    }
}
