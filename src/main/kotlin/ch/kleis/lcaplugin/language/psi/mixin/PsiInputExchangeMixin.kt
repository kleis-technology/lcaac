package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiInputExchange
import ch.kleis.lcaplugin.language.psi.type.PsiUnit
import ch.kleis.lcaplugin.language.reference.ProductExchangeReference
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

abstract class PsiInputExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiInputExchange {

    override fun getReference(): PsiReference? {
        return nameIdentifier?.let { ProductExchangeReference(this, it.textRangeInParent) }
    }

    override fun getName(): String? = super<PsiInputExchange>.getName()

}
