package com.github.albanseurat.lcaplugin.language.ide.style

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock

class LcaDatasetDefinitionBlock(node: ASTNode, wrap: Wrap, alignment: Alignment) :
    AbstractBlock(node, wrap, alignment) {

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return null;
    }

    override fun isLeaf(): Boolean {
        return true
    }

    override fun buildChildren(): List<Block> {
        return emptyList()
    }
}