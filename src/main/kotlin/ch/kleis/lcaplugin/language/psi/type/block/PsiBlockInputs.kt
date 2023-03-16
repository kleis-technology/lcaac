package ch.kleis.lcaplugin.language.psi.type.block

import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiBlockInputs : PsiElement {
    fun getExchanges(): Collection<PsiTechnoInputExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.TECHNO_INPUT_EXCHANGE))
            .map { it.psi as PsiTechnoInputExchange }
    }
}
