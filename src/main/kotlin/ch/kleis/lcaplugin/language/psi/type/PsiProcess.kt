package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStub
import ch.kleis.lcaplugin.language.psi.type.block.PsiBlockProducts
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoInputExchange
import ch.kleis.lcaplugin.language.psi.type.exchange.PsiTechnoProductExchange
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import ch.kleis.lcaplugin.language.psi.type.trait.BlockMetaOwner
import ch.kleis.lcaplugin.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.ResolveState
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil

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
        return PsiTreeUtil.findChildrenOfType(this, LcaParams::class.java)
            .flatMap {
                it.assignmentList.map { a ->
                    a.getQuantityRef().name to a.getValue()
                }
            }
            .toMap()
    }

    fun getProducts(): Collection<PsiTechnoProductExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_PRODUCTS))
            .map { it.psi as PsiBlockProducts }
            .flatMap { it.getExchanges() }
    }

    fun getInputs(): Collection<PsiTechnoInputExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_INPUTS))
            .map { it.psi as LcaBlockInputs }
            .flatMap { it.technoInputExchangeList }
    }

    fun getEmissions(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_EMISSIONS))
            .map { it.psi as LcaBlockEmissions }
            .flatMap { it.bioExchangeList }
    }

    fun getLandUse(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_LAND_USE))
            .map { it.psi as LcaBlockLandUse }
            .flatMap { it.bioExchangeList }
    }

    fun getResources(): Collection<PsiBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_RESOURCES))
            .map { it.psi as LcaBlockResources }
            .flatMap { it.bioExchangeList }
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

    fun getLcaParams(): Collection<LcaParams> {
        return PsiTreeUtil.findChildrenOfType(this, LcaParams::class.java)
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

        for (block in getLcaParams()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }

        return true
    }
}
