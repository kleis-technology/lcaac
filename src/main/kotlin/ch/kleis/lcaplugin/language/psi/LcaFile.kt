package ch.kleis.lcaplugin.language.psi

import ch.kleis.lcaplugin.LcaFileType
import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.type.*
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnitLiteral
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.TokenSet

class LcaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LcaLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return LcaFileType.INSTANCE
    }

    override fun toString(): String {
        return "Lca File"
    }

    fun getPackageName(): String {
        return (node.findChildByType(LcaTypes.PACKAGE)?.psi as PsiPackage?)?.name
            ?: "default"
    }

    fun getImports(): Collection<PsiImport> {
        return node.getChildren(TokenSet.create(LcaTypes.IMPORT))
            .map { it.psi as PsiImport }
    }

    fun getProcesses(): Collection<PsiProcess> {
        return node.getChildren(TokenSet.create(LcaTypes.PROCESS))
            .map { it.psi as PsiProcess }
    }

    fun getSubstances(): Collection<PsiSubstance> {
        return node.getChildren(TokenSet.create(LcaTypes.SUBSTANCE))
            .map { it.psi as PsiSubstance }
    }

    fun getAssignments(): Collection<Pair<String, PsiQuantity>> {
        return node.getChildren(TokenSet.create(LcaTypes.VARIABLES))
            .map { it.psi as PsiVariables }
            .flatMap { it.getEntries() }
    }

    fun getUnitLiterals(): Collection<PsiUnitLiteral> {
        return node.getChildren(TokenSet.create(LcaTypes.UNIT_LITERAL))
            .map { it.psi as PsiUnitLiteral }
    }

    fun getPsiVariablesBlocks(): Collection<PsiVariables> {
        return node.getChildren(TokenSet.create(LcaTypes.VARIABLES))
            .map { it.psi as PsiVariables }
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

        return true
    }
}
