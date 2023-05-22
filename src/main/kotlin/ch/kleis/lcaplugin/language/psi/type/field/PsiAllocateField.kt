package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.psi.LcaQuantityExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

interface PsiAllocateField : PsiElement {
    fun getValue(): LcaQuantityExpression {
        return PsiTreeUtil.getChildOfType(this, LcaQuantityExpression::class.java)!!
    }
}