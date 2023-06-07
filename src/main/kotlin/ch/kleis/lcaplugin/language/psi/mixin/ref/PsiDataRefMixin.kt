package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.reference.DataReference
import ch.kleis.lcaplugin.language.psi.type.ref.PsiDataRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiDataRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiDataRef {
    override fun getReference(): DataReference {
        return super<PsiDataRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiDataRef>.getName()
    }
}
