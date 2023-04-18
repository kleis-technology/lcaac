package ch.kleis.lcaplugin.language.psi.type.exchange

import ch.kleis.lcaplugin.language.psi.type.field.PsiAllocateField
import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.language.psi.type.ref.PsiProductRef
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

interface PsiTechnoProductExchangeWithAllocateField : PsiElement {
    fun getTechnoProductExchange(): PsiTechnoProductExchange {
        return node.findChildByType(LcaTypes.TECHNO_PRODUCT_EXCHANGE)?.psi as PsiTechnoProductExchange
    }
    fun getAllocateField(): PsiAllocateField {
        return node.findChildByType(LcaTypes.ALLOCATE_FIELD)?.psi as PsiAllocateField
    }
}