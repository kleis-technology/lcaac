package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStub
import ch.kleis.lcaplugin.language.psi.type.block.*
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import ch.kleis.lcaplugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaplugin.psi.LcaQuantityExpression
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.ResolveState
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.TokenSet

interface PsiProcess : StubBasedPsiElement<ProcessStub>, PsiNameIdentifierOwner, BlockMetaOwner {
    fun getProcessTemplateRef(): PsiProcessTemplateRef {
        return node.findChildByType(LcaTypes.PROCESS_TEMPLATE_REF)?.psi as PsiProcessTemplateRef
    }

    override fun getName(): String {
        return getProcessTemplateRef().name
    }

    override fun setName(name: String): PsiElement {
        getProcessTemplateRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getProcessTemplateRef().nameIdentifier
    }

    fun getParameters(): Map<String, LcaQuantityExpression> {
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

    fun getLandUse(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_LAND_USE))
            .map { it.psi as PsiBlockLandUse }
            .flatMap { it.getExchanges() }
    }

    fun getResources(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_RESOURCES))
            .map { it.psi as PsiBlockResources }
            .flatMap { it.getExchanges() }
    }

    fun getVariables(): Map<String, LcaQuantityExpression> {
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
            if (!processor.execute(block, state)) {
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
