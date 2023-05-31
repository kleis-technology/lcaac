package ch.kleis.lcaplugin.language.psi.mixin.spec

import ch.kleis.lcaplugin.language.psi.reference.OutputProductReference
import ch.kleis.lcaplugin.language.psi.type.spec.PsiInputProductSpec
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiInputProductSpecMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiInputProductSpec {
    override fun getReference(): OutputProductReference {
        return super<PsiInputProductSpec>.getReference()
    }

    override fun getName(): String {
        return super<PsiInputProductSpec>.getName()
    }
}
