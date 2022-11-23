package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiFormulaExpression : PsiElement {
    fun getContent(): String {
        val number = node.findChildByType(LcaTypes.NUMBER)?.psi?.text
        val content = node.findChildByType(LcaTypes.FORMULA_CONTENT)?.psi?.text
        return listOfNotNull(
            number,
            content
        ).first()
    }
}
