package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.language.psi.PsiExchangeElement
import com.github.albanseurat.lcaplugin.language.psi.PsiUnitElement
import com.github.albanseurat.lcaplugin.language.reference.ProductReference
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.model.psi.PsiSymbolService.getInstance
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

abstract class InputExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), IdentifiableTrait, PsiExchangeElement {

    override fun getName() : String? = super<IdentifiableTrait>.getName()

    override fun setName(name: String): PsiElement = super.setName(name)

    override fun getNameIdentifier(): PsiElement? = super.getNameIdentifier()

    override fun getReference(): PsiReference? {
        return nameIdentifier?.let { ProductReference(this, it.textRangeInParent) }
    }

    override fun getUnitElement(): PsiUnitElement? {
        return getNode().findChildByType(LcaTypes.QUANTITY)?.psi as PsiUnitElement?
    }

}