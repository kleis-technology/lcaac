package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.psi.LcaQuantityExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

interface PsiQuantityField : PsiElement {
    fun getValue(): LcaQuantityExpression {
        return PsiTreeUtil.findChildOfType(this, LcaQuantityExpression::class.java)!!
    }
}
