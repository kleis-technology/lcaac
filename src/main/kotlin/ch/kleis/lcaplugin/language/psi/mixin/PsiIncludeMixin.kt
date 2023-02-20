package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiInclude
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiIncludeMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiInclude {
    override fun getName(): String? {
        return super<PsiInclude>.getName()
    }
}
