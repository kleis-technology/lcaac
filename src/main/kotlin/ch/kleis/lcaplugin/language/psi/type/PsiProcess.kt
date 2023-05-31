package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStub
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
    fun getProcessRef(): LcaProcessRef {
        return PsiTreeUtil.getChildOfType(this, LcaProcessRef::class.java) as LcaProcessRef
    }

    fun buildUniqueKey(): String = if (this.getLabels().isEmpty()) this.name else "${this.name}${this.getLabels()}"

    override fun getName(): String {
        return getProcessRef().name
    }

    override fun setName(name: String): PsiElement {
        getProcessRef().name = name
        return this
    }

    override fun getNameIdentifier(): PsiElement? {
        return getProcessRef().nameIdentifier
    }

    fun getParameters(): Map<String, LcaDataExpression> {
        return PsiTreeUtil.findChildrenOfType(this, LcaParams::class.java)
            .flatMap {
                it.assignmentList.map { a ->
                    a.getDataRef().name to a.getValue()
                }
            }
            .toMap()
    }

    fun getProducts(): Collection<LcaTechnoProductExchange> {
        return PsiTreeUtil.findChildrenOfType(this, LcaBlockProducts::class.java)
            .flatMap { it.technoProductExchangeList }
    }

    fun getInputs(): Collection<LcaTechnoInputExchange> {
        return PsiTreeUtil.findChildrenOfType(this, LcaBlockInputs::class.java)
            .flatMap { it.technoInputExchangeList }
    }

    fun getLabels(): Map<String, String> {
        return PsiTreeUtil.findChildrenOfType(this, LcaLabels::class.java)
            .flatMap { it.labelAssignmentList }
            .associate { it.name to it.getValue() }
    }

    fun getEmissions(): Collection<LcaBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_EMISSIONS))
            .map { it.psi as LcaBlockEmissions }
            .flatMap { it.bioExchangeList }
    }

    fun getLandUse(): Collection<LcaBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_LAND_USE))
            .map { it.psi as LcaBlockLandUse }
            .flatMap { it.bioExchangeList }
    }

    fun getResources(): Collection<LcaBioExchange> {
        return node.getChildren(TokenSet.create(LcaTypes.BLOCK_RESOURCES))
            .map { it.psi as LcaBlockResources }
            .flatMap { it.bioExchangeList }
    }

    fun getVariables(): Map<String, LcaDataExpression> {
        return PsiTreeUtil.findChildrenOfType(this, LcaVariables::class.java)
            .flatMap {
                it.assignmentList.map { a ->
                    a.getDataRef().name to a.getValue()
                }
            }
            .toMap()
    }

    fun getLcaVariables(): Collection<LcaVariables> {
        return PsiTreeUtil.findChildrenOfType(this, LcaVariables::class.java)
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
        for (block in getLcaVariables()) {
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
