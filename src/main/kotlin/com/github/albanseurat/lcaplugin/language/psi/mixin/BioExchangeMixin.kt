package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.language.psi.type.PsiExchangeElement
import com.github.albanseurat.lcaplugin.language.psi.type.PsiUnitElement
import com.github.albanseurat.lcaplugin.language.reference.SubstanceReference
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference

abstract class BioExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node),  SubstanceIdTrait, PsiExchangeElement {


    override fun getReference(): PsiReference? {
        return nameIdentifier?.let { SubstanceReference(this, it.textRangeInParent) }
    }

    override fun getName(): String? = super<SubstanceIdTrait>.getName()
    override fun setName(name: String): PsiElement = super.setName(name)
    override fun getNameIdentifier(): PsiElement? = super.getNameIdentifier()

    override fun getUnitElement(): PsiUnitElement? {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnitElement?
    }


}