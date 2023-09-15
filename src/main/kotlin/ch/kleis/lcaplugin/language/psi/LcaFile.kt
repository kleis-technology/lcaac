package ch.kleis.lcaplugin.language.psi

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.core.prelude.Prelude
import ch.kleis.lcaplugin.psi.*
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
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

    fun getImportNames(): Collection<String> {
        return listOf(Prelude.pkgName) + PsiTreeUtil.findChildrenOfType(this, LcaImport::class.java).map { it.name }
    }

    fun getProcesses(): Collection<LcaProcess> {
        return PsiTreeUtil.findChildrenOfType(this, LcaProcess::class.java)
    }

    fun getSubstances(): Collection<LcaSubstance> {
        return PsiTreeUtil.findChildrenOfType(this, LcaSubstance::class.java)
    }

    fun getGlobalAssignments(): Collection<Pair<String, LcaDataExpression>> {
        return PsiTreeUtil.findChildrenOfType(this, LcaGlobalVariables::class.java)
            .flatMap {
                it.globalAssignmentList
                    .map { a -> a.getDataRef().name to a.getValue() }
            }
    }

    fun getUnitDefinitions(): Collection<LcaUnitDefinition> {
        return PsiTreeUtil.findChildrenOfType(this, LcaUnitDefinition::class.java)
    }

    fun getBlocksOfGlobalVariables(): Collection<LcaGlobalVariables> {
        return PsiTreeUtil.findChildrenOfType(this, LcaGlobalVariables::class.java)
    }

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean {
        for (block in getBlocksOfGlobalVariables()) {
            if (!processor.execute(block, state)) {
                return false
            }
        }

        return true
    }
}
