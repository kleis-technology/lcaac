package ch.kleis.lcaplugin.language.psi

import ch.kleis.lcaplugin.LcaLanguage
import ch.kleis.lcaplugin.language.psi.type.PsiImport
import ch.kleis.lcaplugin.language.psi.type.PsiPackage
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.language.psi.type.PsiSubstance
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.TokenSet

class LcaFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LcaLanguage.INSTANCE) {
    override fun getFileType(): FileType {
        return ch.kleis.lcaplugin.LcaFileType.INSTANCE
    }

    override fun toString(): String {
        return "Lca File"
    }

    fun getPackage(): PsiPackage {
        return node.findChildByType(LcaTypes.PACKAGE)?.psi as PsiPackage? ?: throw IllegalStateException()
    }

    fun getImports(): Collection<PsiImport> {
        return node.getChildren(TokenSet.create(LcaTypes.IMPORT)).map { it.psi as PsiImport }
    }

    fun getProcesses(): Collection<PsiProcess> {
        return node.getChildren(TokenSet.create(LcaTypes.PROCESS)).map { it.psi as PsiProcess }
    }

    fun getSubstances(): Collection<PsiSubstance> {
        return node.getChildren(TokenSet.create(LcaTypes.SUBSTANCE)).map { it.psi as PsiSubstance }
    }
}
