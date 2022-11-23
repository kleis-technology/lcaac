package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiParameter
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiParameterMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiParameter {
    override fun getName(): String? = super<PsiParameter>.getName()
}
