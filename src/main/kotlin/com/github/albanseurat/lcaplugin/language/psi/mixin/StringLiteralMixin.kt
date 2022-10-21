package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

abstract class StringLiteralMixin(node: ASTNode) : ASTWrapperPsiElement(node) {

    override fun getName(): String? {
        return this.firstChild.text.trim('"')
    }
}