package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiProcessTemplateRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiProcessTemplateRef {
    override fun getName(): String {
        return super<PsiProcessTemplateRef>.getName()
    }
}
