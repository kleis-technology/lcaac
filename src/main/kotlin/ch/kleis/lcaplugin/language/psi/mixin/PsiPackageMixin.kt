package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiPackage
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiPackageMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiPackage {
    override fun getName(): String? = super<PsiPackage>.getName()
}
