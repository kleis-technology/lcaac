package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiExchangeElement
import ch.kleis.lcaplugin.language.psi.type.PsiUnitElement
import ch.kleis.lcaplugin.language.reference.ProductExchangeReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

abstract class InputExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), IdentifiableTrait, PsiExchangeElement {

    override fun getName() : String? = super<IdentifiableTrait>.getName()

    override fun setName(name: String): PsiElement = super.setName(name)

    override fun getNameIdentifier(): PsiElement? = super.getNameIdentifier()

    override fun getReference(): PsiReference? {
        return nameIdentifier?.let { ProductExchangeReference(this, it.textRangeInParent) }
    }

    override fun getUnitElement(): PsiUnitElement? {
        return node.findChildByType(ch.kleis.lcaplugin.psi.LcaTypes.UNIT)?.psi as PsiUnitElement?
    }

}
