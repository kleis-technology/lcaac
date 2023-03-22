package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

abstract class PsiUnitRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUnitRef {
    override fun getReference(): PsiReference? {
        return super<PsiUnitRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiUnitRef>.getName()
    }
}
