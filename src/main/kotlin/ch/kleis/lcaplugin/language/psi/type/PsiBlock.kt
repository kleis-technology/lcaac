package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.core.lang.Polarity
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

interface PsiBlock : PsiElement {
    fun getPolarity(): Polarity {
        return node.findChildByType(LcaTypes.INPUTS_KEYWORD)?.let { Polarity.NEGATIVE }
            ?: node.findChildByType(LcaTypes.PRODUCTS_KEYWORD)?.let { Polarity.POSITIVE }
            ?: node.findChildByType(LcaTypes.COPRODUCTS_KEYWORD)?.let { Polarity.POSITIVE }
            ?: node.findChildByType(LcaTypes.EMISSIONS_KEYWORD)?.let { Polarity.POSITIVE }
            ?: node.findChildByType(LcaTypes.RESOURCES_KEYWORD)?.let { Polarity.POSITIVE }
            ?: throw IllegalStateException("invalid psi block")
    }

    fun getExchanges(): Collection<PsiExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.EXCHANGE))
            .map { it.psi as PsiExchange }
    }
}
