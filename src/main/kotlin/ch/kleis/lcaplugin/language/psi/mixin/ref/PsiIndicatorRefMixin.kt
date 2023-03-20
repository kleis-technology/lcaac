package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.type.ref.PsiIndicatorRef
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiIndicatorRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiIndicatorRef {
    override fun getName(): String {
        return super<PsiIndicatorRef>.getName()
    }
}
