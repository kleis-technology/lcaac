package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet

abstract class SubstanceIdMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiNamedElement {

    override fun setName(name: String): PsiElement {
        throw NotImplementedError()
    }

    override fun getName(): String? {
        return node.getChildren(TokenSet.create(LcaTypes.STRING_LITERAL)).map { it.psi as StringLiteralMixin }
            .map { it.name }.joinToString()
    }
}