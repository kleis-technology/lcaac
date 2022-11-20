package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiSubstanceId
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiSubstanceIdMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiSubstanceId {
    override fun getName(): String? = super<PsiSubstanceId>.getName()
}
