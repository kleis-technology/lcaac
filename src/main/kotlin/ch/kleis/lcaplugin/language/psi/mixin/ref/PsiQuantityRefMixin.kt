package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.reference.QuantityReference
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiQuantityRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiQuantityRef {
    override fun getReference(): QuantityReference {
        return super<PsiQuantityRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiQuantityRef>.getName()
    }
}
