package com.github.albanseurat.lcaplugin.language.ide.syntax

import com.github.albanseurat.lcaplugin.psi.LcaDatasetDefinition
import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType


class LcaFoldingBuilder : FoldingBuilderEx(), DumbAware {


    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean):
            Array<FoldingDescriptor> {

        val descriptors: MutableList<FoldingDescriptor> = ArrayList()

        val lcaDatasetDefinitions: Collection<LcaDatasetDefinition> =
            PsiTreeUtil.findChildrenOfType(root, LcaDatasetDefinition::class.java);

        for (definition in lcaDatasetDefinitions) {
            val braces = PsiTreeUtil.collectElements(definition)
            { e -> e.elementType == LcaTypes.LBRACE || e.elementType == LcaTypes.RBRACE }

            if (braces.size > 1) {
                descriptors.add(
                    FoldingDescriptor(
                        definition,
                        TextRange(braces[0].textOffset, braces[braces.size - 1].textOffset + 1)
                    )
                )
            }
        }
        return descriptors.toTypedArray();

    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return "0 kg co2";
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false;
    }
}