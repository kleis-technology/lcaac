package ch.kleis.lcaplugin.language.psi.mixin.spec

import ch.kleis.lcaplugin.language.psi.reference.SubstanceReference
import ch.kleis.lcaplugin.language.psi.type.spec.PsiSubstanceSpec
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiSubstanceSpecMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiSubstanceSpec {
    override fun getReference(): SubstanceReference {
        return super<PsiSubstanceSpec>.getReference()
    }
}
