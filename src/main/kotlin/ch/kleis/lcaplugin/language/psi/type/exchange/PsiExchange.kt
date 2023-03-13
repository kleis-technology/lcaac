package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiQuantityRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiExchange: PsiElement {
    fun getQuantity(): PsiQuantity {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity
    }
}
