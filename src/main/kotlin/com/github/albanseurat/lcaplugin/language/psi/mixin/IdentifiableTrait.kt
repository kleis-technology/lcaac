package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.LcaFileType
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.lang.ASTNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory

sealed interface IdentifiableTrait {

    fun getNode() : ASTNode

    fun getProject() : Project

    fun getName() : String? {
        return (getNameIdentifier() as StringLiteralMixin?)?.name
    }

    fun setName(name: String): PsiElement {

        val identifierNode : ASTNode? = getNode().findChildByType(LcaTypes.STRING_LITERAL)
        if (identifierNode != null) {
            val newIdentifier = PsiFileFactory.getInstance(getProject())
                .createFileFromText("_Dummy_.${LcaFileType.INSTANCE.defaultExtension}", LcaFileType.INSTANCE, name)
            getNode().replaceChild(identifierNode, newIdentifier.node)
        }
        return this as PsiElement;
    }

    fun getNameIdentifier(): PsiElement? {
        return getNode().findChildByType(LcaTypes.STRING_LITERAL)?.psi
    }
}