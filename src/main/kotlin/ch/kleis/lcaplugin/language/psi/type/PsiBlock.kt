package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

enum class Polarity {
    POSITIVE, NEGATIVE,
}

enum class BlockType {
    PRODUCTS, INPUTS, EMISSIONS, RESOURCES
}

interface PsiBlock : PsiElement {
    fun getType() : BlockType {
        return node.findChildByType(LcaTypes.INPUTS_KEYWORD)?.let { BlockType.INPUTS }
            ?: node.findChildByType(LcaTypes.PRODUCTS_KEYWORD)?.let { BlockType.PRODUCTS }
            ?: node.findChildByType(LcaTypes.EMISSIONS_KEYWORD)?.let { BlockType.EMISSIONS }
            ?: node.findChildByType(LcaTypes.RESOURCES_KEYWORD)?.let { BlockType.RESOURCES }
            ?: throw IllegalStateException("invalid psi block")
    }

    fun getPolarity(): Polarity {
        return node.findChildByType(LcaTypes.INPUTS_KEYWORD)?.let { Polarity.NEGATIVE }
            ?: node.findChildByType(LcaTypes.PRODUCTS_KEYWORD)?.let { Polarity.POSITIVE }
            ?: node.findChildByType(LcaTypes.EMISSIONS_KEYWORD)?.let { Polarity.NEGATIVE }
            ?: node.findChildByType(LcaTypes.RESOURCES_KEYWORD)?.let { Polarity.NEGATIVE }
            ?: throw IllegalStateException("invalid psi block")
    }

    fun getExchanges(): Collection<PsiExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.EXCHANGE))
            .map { it.psi as PsiExchange }
    }
}
