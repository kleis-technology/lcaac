package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.psi.LcaQuantityExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

interface PsiExchange : PsiElement {
    fun getQuantity(): LcaQuantityExpression {
        return PsiTreeUtil.findChildOfType(this, LcaQuantityExpression::class.java)!!
    }
}
