package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

interface PsiFormulaExpression : PsiElement {
    fun getContentType(): IElementType {
        val numberNode = node.findChildByType(LcaTypes.NUMBER)
        if (numberNode != null) {
            return LcaTypes.NUMBER
        }
        val formulaNode = node.findChildByType(LcaTypes.FORMULA_CONTENT)
        if (formulaNode != null) {
            return LcaTypes.FORMULA_CONTENT
        }
        throw IllegalStateException()
    }

    fun getContent(): String {
        val number = node.findChildByType(LcaTypes.NUMBER)?.psi?.text
        val content = node.findChildByType(LcaTypes.FORMULA_CONTENT)?.psi?.text
        return listOfNotNull(
            number,
            content
        ).first()
    }
}
