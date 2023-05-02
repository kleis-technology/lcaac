package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.reference.SubstanceReference
import ch.kleis.lcaplugin.language.psi.type.ref.PsiSubstanceRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiSubstanceRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiSubstanceRef {
    override fun getName(): String {
        return super<PsiSubstanceRef>.getName()
    }
}
