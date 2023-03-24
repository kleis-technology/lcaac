package ch.kleis.lcaplugin.language.psi.type.field

import ch.kleis.lcaplugin.language.psi.type.quantity.PsiQuantity
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiAliasForField : PsiElement {
    fun getValue(): PsiQuantity {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity
    }
}