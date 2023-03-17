package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiProductRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiProductRef {
    override fun getName(): String {
        return super<PsiProductRef>.getName()
    }
}
