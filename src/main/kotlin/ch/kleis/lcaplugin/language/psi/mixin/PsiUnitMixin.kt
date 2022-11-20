package ch.kleis.lcaplugin.language.psi.mixin

import ch.kleis.lcaplugin.language.psi.type.PsiUnit
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import tech.units.indriya.AbstractUnit
import tech.units.indriya.format.SimpleUnitFormat

abstract class PsiUnitMixin(node: ASTNode) : ASTWrapperPsiElement(node),
    PsiUnit {

    companion object {
        val parser: SimpleUnitFormat = SimpleUnitFormat.getInstance()
    }

    private var unit: javax.measure.Unit<*> = AbstractUnit.ONE;

    init {
        unit = parser.parse(node.text)
    }

    override fun getName(): String {
        return node.text
    }

    override fun getUnit(): javax.measure.Unit<*> {
        return unit;
    }

    override fun setName(name: String): PsiElement {
        throw NotImplementedError()
    }
}
