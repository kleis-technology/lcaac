package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.reference.UnitReference
import ch.kleis.lcaplugin.language.psi.type.ref.PsiUnitRef
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiUnitRefMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUnitRef {
    override fun getReference(): UnitReference {
        return super<PsiUnitRef>.getReference()
    }

    override fun getName(): String {
        return super<PsiUnitRef>.getName()
    }
}
