package ch.kleis.lcaplugin.language.psi.type.traits

import ch.kleis.lcaplugin.language.psi.type.PsiFormulaExpression
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiFormulaExpressionOwner: PsiElement {
    fun getExpression(): PsiFormulaExpression {
        return node.findChildByType(LcaTypes.F_EXPR)?.psi as PsiFormulaExpression?
            ?: throw IllegalStateException()
    }
}
