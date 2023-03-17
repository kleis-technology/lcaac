package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiMetaAssignment
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiMetaAssignmentMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiMetaAssignment {
    override fun getName(): String? {
        return super<PsiMetaAssignment>.getName()
    }
}
