package ch.kleis.lcaplugin.language.psi.type

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiExchange : PsiElement {
    fun getQuantity(): PsiQuantity {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity
    }

    fun getProduct(): PsiVariable {
        return node.findChildByType(LcaTypes.VARIABLE)?.psi as PsiVariable
    }
}
