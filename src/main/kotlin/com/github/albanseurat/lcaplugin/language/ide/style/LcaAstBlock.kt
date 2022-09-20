package com.github.albanseurat.lcaplugin.language.ide.style

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.TokenSet.*

class LcaAstBlock(node: ASTNode, wrap: Wrap, alignment: Alignment, private val spaceBuilder: SpacingBuilder) :
    AbstractBlock(node, wrap, alignment) {

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spaceBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return myNode.getChildren(ANY).isNotEmpty()
    }

    override fun buildChildren(): List<Block> {
        val alignment = Alignment.createAlignment()
        return myNode.getChildren(andNot(ANY, WHITE_SPACE)).map {
            LcaAstBlock(
                it,
                Wrap.createWrap(WrapType.NONE, false),
                alignment,
                spaceBuilder
            )
        }
    }
}