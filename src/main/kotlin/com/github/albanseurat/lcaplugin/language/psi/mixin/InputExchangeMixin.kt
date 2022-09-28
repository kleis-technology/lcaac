package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.language.reference.ProductReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReference

abstract class InputExchangeMixin(node: ASTNode) : ASTWrapperPsiElement(node), IdentifiableTrait, PsiNameIdentifierOwner {

    override fun getName() : String? = super<IdentifiableTrait>.getName()

    override fun setName(name: String): PsiElement = super.setName(name)

    override fun getNameIdentifier(): PsiElement? = super.getNameIdentifier()

    override fun getReference(): PsiReference? {
        return nameIdentifier?.let { ProductReference(this, it.textRangeInParent) }
    }
}