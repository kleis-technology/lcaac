package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiInputExchange
import ch.kleis.lcaplugin.language.reference.ProductExchangeReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

abstract class PsiInputExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiInputExchange {

    override fun getReference(): PsiReference? {
        return ProductExchangeReference(this)
    }

    override fun getName(): String? = super<PsiInputExchange>.getName()

}
