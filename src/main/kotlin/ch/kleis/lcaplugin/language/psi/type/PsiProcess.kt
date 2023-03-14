package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockEmissions
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockInputs
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockProducts
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockResources
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.TokenSet

interface PsiProcess : PsiElement {
    fun getUid(): PsiUID? {
        return node.findChildByType(LcaTypes.UID)?.psi as PsiUID?
    }

    fun getParameters(): Map<String, PsiQuantity> {
        return node.getChildren(TokenSet.create(LcaTypes.PARAMS))
            .map { it.psi as PsiParameters }
            .flatMap { it.getEntries() }
            .toMap()
    }

    fun getProducts(): Collection<PsiTechnoProductExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_PRODUCTS))
            .map { it.psi as PsiBlockProducts }
            .flatMap { it.getExchanges() }
    }

    fun getInputs(): Collection<PsiTechnoInputExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_INPUTS))
            .map { it.psi as PsiBlockInputs }
            .flatMap { it.getExchanges() }
    }

    fun getEmissions(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_EMISSIONS))
            .map { it.psi as PsiBlockEmissions }
            .flatMap { it.getExchanges() }
    }

    fun getResources(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_RESOURCES))
            .map { it.psi as PsiBlockResources }
            .flatMap { it.getExchanges() }
    }

    fun getVariables(): Map<String, PsiQuantity> {
        return node.getChildren(TokenSet.create(LcaTypes.VARIABLES))
            .map { it.psi as PsiVariables }
            .flatMap { it.getEntries() }
            .toMap()
    }

    fun getPsiVariablesBlocks(): Collection<PsiVariables> {
        return node.getChildren(TokenSet.create(LcaTypes.VARIABLES))
            .map { it.psi as PsiVariables }
    }

    fun getPsiParametersBlocks(): Collection<PsiParameters> {
        return node.getChildren(TokenSet.create(LcaTypes.PARAMS))
            .map { it.psi as PsiParameters }
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        for (block in getPsiVariablesBlocks()) {
            if (!processor.execute(block, state)){
               return false
            }
        }

        for (block in getPsiParametersBlocks()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }

        return true
    }
}
