package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.type.ref.PsiLabelRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiLabelRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiLabelRef {
    override fun getName(): String {
        return super<PsiLabelRef>.getName()
    }
}
