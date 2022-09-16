package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

abstract class DatasetDefinitionMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiNamedElement {

    override fun getName() : String? {
        return node.findChildByType(LcaTypes.IDENTIFIER)?.text
    }

    override fun setName(name: String): PsiElement {
        return this;
    }
}