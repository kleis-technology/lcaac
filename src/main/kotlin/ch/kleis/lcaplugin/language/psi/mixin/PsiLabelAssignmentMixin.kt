package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiLabelAssignment
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiLabelAssignmentMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiLabelAssignment {
    override fun getName(): String {
        return super<PsiLabelAssignment>.getName()
    }
}
