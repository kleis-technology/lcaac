package ch.kleis.lcaplugin.language.psi

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitDefinition
import ch.kleis.lcaplugin.psi.*
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil

class LcaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LcaLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return LcaFileType.INSTANCE
    }

    override fun toString(): String {
        return "Lca File"
    }

    fun getPackageName(): String {
        return PsiTreeUtil.findChildOfType(this, LcaPackage::class.java)?.name
            ?: "default"
    }

    fun getImports(): Collection<LcaImport> {
        return PsiTreeUtil.findChildrenOfType(this, LcaImport::class.java)
    }

    fun getProcesses(): Collection<PsiProcess> {
        return node.getChildren(TokenSet.create(LcaTypes.PROCESS))
            .map { it.psi as PsiProcess }
    }

    fun getSubstances(): Collection<PsiSubstance> {
        return node.getChildren(TokenSet.create(LcaTypes.SUBSTANCE))
            .map { it.psi as PsiSubstance }
    }

    fun getGlobalAssignments(): Collection<Pair<String, LcaQuantityExpression>> {
        return PsiTreeUtil.findChildrenOfType(this, LcaGlobalVariables::class.java)
            .flatMap {
                it.globalAssignmentList
                    .map { a -> a.getQuantityRef().name to a.getValue() }
            }
    }

    fun getUnitDefinitions(): Collection<PsiUnitDefinition> {
        return node.getChildren(TokenSet.create(LcaTypes.UNIT_DEFINITION))
            .map { it.psi as PsiUnitDefinition }
    }

    fun getLcaGlobalVariables(): Collection<LcaGlobalVariables> {
        return PsiTreeUtil.findChildrenOfType(this, LcaGlobalVariables::class.java)
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        for (block in getLcaGlobalVariables()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }

        return true
    }
}
