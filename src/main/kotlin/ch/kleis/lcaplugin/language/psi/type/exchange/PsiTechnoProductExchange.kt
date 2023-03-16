package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiTechnoProductExchange : PsiElement {
    fun getProductRef(): PsiProductRef {
        return node.findChildByType(LcaTypes.PRODUCT_REF)?.psi as PsiProductRef
    }

    fun getQuantity(): PsiQuantity {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity
    }
}
