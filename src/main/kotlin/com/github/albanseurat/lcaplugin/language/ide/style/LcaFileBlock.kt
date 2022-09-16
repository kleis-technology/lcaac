package com.github.albanseurat.lcaplugin.language.ide.style

import com.github.albanseurat.lcaplugin.psi.LcaTypes
import com.intellij.formatting.*
import com.intellij.formatting.WrapType.NONE
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.TokenSet

class LcaFileBlock(
    node: ASTNode, wrap: Wrap, alignment: Alignment,
    private val spaceBuilder: SpacingBuilder,
) :
    AbstractBlock(node, wrap, alignment) {

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spaceBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun buildChildren(): List<Block> {
        val alignment = Alignment.createAlignment();
        return myNode.getChildren(TokenSet.create(LcaTypes.DATASET_DEFINITION)).map {
            LcaDatasetDefinitionBlock(
                it,
                Wrap.createWrap(NONE, false),
                alignment
            )
        }
    }
}