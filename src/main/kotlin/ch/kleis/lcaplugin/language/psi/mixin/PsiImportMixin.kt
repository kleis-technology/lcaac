package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiImport
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiImportMixin(node: ASTNode): ASTWrapperPsiElement(node), PsiImport {
    override fun getName(): String = super<PsiImport>.getName()
}
