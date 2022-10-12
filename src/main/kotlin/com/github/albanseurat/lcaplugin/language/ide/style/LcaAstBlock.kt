package com.github.albanseurat.lcaplugin.language.ide.style

import com.github.albanseurat.lcaplugin.psi.LcaTypes.*
import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.tree.TokenSet.*

class LcaAstBlock(node: ASTNode, private val spaceBuilder: SpacingBuilder) :
    AbstractBlock(node, null, Alignment.createAlignment()) {

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spaceBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return myNode.getChildren(ANY).isNotEmpty()
    }

    override fun getIndent(): Indent? {

        if (node.firstChildNode == null) {
            return Indent.getNoneIndent()
        }
        return when(node.elementType) {
            DATASET_BODY, PRODUCT_EXCHANGE, INPUT_EXCHANGE, BIO_EXCHANGE, PROPERTY -> Indent.getNormalIndent(true)
            else -> Indent.getNoneIndent()
        }
    }

    override fun buildChildren(): List<Block> {
        return myNode.getChildren(andNot(ANY, WHITE_SPACE)).map {
            LcaAstBlock(
                it,
                spaceBuilder
            )
        }
    }
}
