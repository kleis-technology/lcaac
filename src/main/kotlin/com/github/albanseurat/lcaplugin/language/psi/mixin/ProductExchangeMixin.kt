package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.language.psi.PsiExchangeElement
import com.github.albanseurat.lcaplugin.language.psi.PsiUnitElement
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement

abstract class ProductExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), IdentifiableTrait, PsiExchangeElement {

    override fun getName() : String? = super<IdentifiableTrait>.getName()

    override fun setName(name: String): PsiElement = super.setName(name)

    override fun getNameIdentifier(): PsiElement? = super.getNameIdentifier()


    override fun getUnitElement(): PsiUnitElement? {
        return getNode().findChildByType(LcaTypes.QUANTITY)?.psi as PsiUnitElement?
    }


}