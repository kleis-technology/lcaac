package ch.kleis.lcaplugin.language.psi.mixin.trait

import ch.kleis.lcaplugin.language.psi.type.trait.PsiUrnOwner
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUrnOwnerMixin(node: ASTNode): ASTWrapperPsiElement(node), PsiUrnOwner {
    override fun getName(): String {
        return super<PsiUrnOwner>.getName()
    }
}
