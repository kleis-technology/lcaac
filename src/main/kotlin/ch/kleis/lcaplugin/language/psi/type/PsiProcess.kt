package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.traits.PsiUniqueIdOwner
import ch.kleis.lcaplugin.psi.*
import com.intellij.psi.tree.TokenSet

interface PsiProcess : PsiUniqueIdOwner {
    fun getParameters(): Collection<PsiParameter> {
        val blocks = node.getChildren(TokenSet.create(LcaTypes.PARAMETERS))
        return blocks.map { block -> block.psi as LcaParameters }
            .flatMap { block -> block.parameterList }
            .map { it as PsiParameter }
    }

    fun getProductExchanges(): Collection<PsiProductExchange> {
        val blocks = node.getChildren(TokenSet.create(LcaTypes.PRODUCTS))
        return blocks.map { block -> block.psi as LcaProducts }
            .flatMap { block -> block.productExchangeList }
            .map { it as PsiProductExchange }
    }

    fun getInputExchanges(): Collection<PsiInputExchange> {
        val blocks = node.getChildren(TokenSet.create(LcaTypes.INPUTS))
        return blocks.map { block -> block.psi as LcaInputs }
            .flatMap { block -> block.inputExchangeList }
            .map { it as PsiInputExchange }
    }

    fun getResourceExchanges(): Collection<PsiBioExchange> {
        val blocks = node.getChildren(TokenSet.create(LcaTypes.RESOURCES))
        return blocks.map { block -> block.psi as LcaResources }
            .flatMap { block -> block.bioExchangeList }
            .map { it as PsiBioExchange }
    }

    fun getEmissionExchanges(): Collection<PsiBioExchange> {
        val blocks = node.getChildren(TokenSet.create(LcaTypes.EMISSIONS))
        return blocks.map { block -> block.psi as LcaEmissions }
            .flatMap { block -> block.bioExchangeList }
            .map { it as PsiBioExchange }
    }
}

