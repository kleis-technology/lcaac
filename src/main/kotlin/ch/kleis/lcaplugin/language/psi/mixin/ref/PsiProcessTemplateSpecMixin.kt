package ch.kleis.lcaplugin.language.psi.mixin.ref

import ch.kleis.lcaplugin.language.psi.reference.ProcessReferenceFromPsiProcessTemplateSpec
import ch.kleis.lcaplugin.language.psi.type.spec.PsiProcessTemplateSpec
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

abstract class PsiProcessTemplateSpecMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiProcessTemplateSpec {
    override fun getReference(): ProcessReferenceFromPsiProcessTemplateSpec {
        return super<PsiProcessTemplateSpec>.getReference()
    }

    override fun getName(): String {
        return super<PsiProcessTemplateSpec>.getName()
    }
}
