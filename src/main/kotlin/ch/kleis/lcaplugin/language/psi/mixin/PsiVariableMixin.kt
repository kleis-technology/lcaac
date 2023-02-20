package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiVariable
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiVariableMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiVariable {
    override fun getName(): String? {
        return super<PsiVariable>.getName()
    }
}
