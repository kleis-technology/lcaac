package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiGlobalAssignment
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiGlobalAssignmentMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiGlobalAssignment {
    override fun getName(): String {
        return super<PsiGlobalAssignment>.getName()
    }
}
