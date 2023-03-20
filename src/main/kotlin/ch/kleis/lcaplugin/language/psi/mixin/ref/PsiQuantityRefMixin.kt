package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

abstract class PsiQuantityRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiQuantityRef {
    override fun getReference(): PsiReference? {
        return super<PsiQuantityRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiQuantityRef>.getName()
    }
}
