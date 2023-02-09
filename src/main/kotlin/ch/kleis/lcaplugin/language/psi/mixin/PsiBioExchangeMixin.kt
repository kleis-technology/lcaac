package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiBioExchange
import ch.kleis.lcaplugin.language.psi.type.PsiUniqueId
import ch.kleis.lcaplugin.language.reference.SubstanceReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

abstract class PsiBioExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiBioExchange {
    override fun getReference(): PsiReference? {
        return SubstanceReference(this)
    }

    override fun getName(): String? = super<PsiBioExchange>.getName()
}
