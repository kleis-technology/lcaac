package com.github.albanseurat.lcaplugin.language.psi.mixin

import com.github.albanseurat.lcaplugin.language.psi.type.PsiUnitElement
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import tech.units.indriya.AbstractUnit
import tech.units.indriya.format.SimpleUnitFormat
import javax.measure.Unit

abstract class QuantityMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiUnitElement {

    companion object {
        val parser: SimpleUnitFormat = SimpleUnitFormat.getInstance()
    }

    private var unit: Unit<*> = AbstractUnit.ONE;

    init {
        unit = parser.parse(node.text)
    }

    override fun getName(): String {
        return node.text
    }

    override fun getQuantityUnit(): Unit<*> {
        return unit;
    }

    override fun setName(name: String): PsiElement {
        throw NotImplementedError()
    }
}