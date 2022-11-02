package ch.kleis.lcaplugin.language.psi.mixin

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

abstract class ProcessDefinitionMixin(node: ASTNode) : ASTWrapperPsiElement(node), IdentifiableTrait, PsiNameIdentifierOwner {


    override fun getName() : String? = super<IdentifiableTrait>.getName()

    override fun setName(name: String): PsiElement = super.setName(name)

    override fun getNameIdentifier(): PsiElement? = super.getNameIdentifier()
}
