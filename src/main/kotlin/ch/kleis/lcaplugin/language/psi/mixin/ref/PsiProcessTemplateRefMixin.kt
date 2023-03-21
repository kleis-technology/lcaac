package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.type.ref.PsiProcessTemplateRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

abstract class PsiProcessTemplateRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiProcessTemplateRef {
    override fun getReference(): PsiReference? {
        return super<PsiProcessTemplateRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiProcessTemplateRef>.getName()
    }
}
