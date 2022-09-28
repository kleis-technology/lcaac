package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.language.psi.PsiUnitElement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import tech.units.indriya.format.SimpleUnitFormat
import javax.measure.Unit

abstract class QuantityMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUnitElement {

    companion object {
        val parser: SimpleUnitFormat = SimpleUnitFormat.getInstance()
    }

    private var unit: Unit<*> = parser.parse("kg")

    override fun getName(): String {
        return node.text
    }

    override fun getQuantityUnit(): Unit<*> {
        return this.unit
    }
}