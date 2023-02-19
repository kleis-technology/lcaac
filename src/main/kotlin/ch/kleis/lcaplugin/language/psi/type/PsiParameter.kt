package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiParameter : PsiElement {
    fun getUid(): PsiUID {
        return node.findChildByType(LcaTypes.UID)?.psi as PsiUID
    }

    fun getCoreExpression(): PsiCoreExpression? {
        return node.findChildByType(LcaTypes.CORE_EXPR)?.psi as PsiCoreExpression?
    }
}
