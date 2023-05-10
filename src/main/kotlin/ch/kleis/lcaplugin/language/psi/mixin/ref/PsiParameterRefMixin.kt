package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.reference.ParameterReference
import ch.kleis.lcaplugin.language.psi.type.ref.PsiParameterRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiParameterRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiParameterRef {
    override fun getReference(): ParameterReference {
        return super<PsiParameterRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiParameterRef>.getName()
    }
}
