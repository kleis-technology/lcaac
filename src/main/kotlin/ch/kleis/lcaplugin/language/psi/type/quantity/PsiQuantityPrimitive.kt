package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.language.psi.type.unit.PsiUnit
import ch.kleis.lcaplugin.language.psi.type.PsiVariable
import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement
import java.lang.Double.parseDouble

interface PsiQuantityPrimitive : PsiElement {
    fun getAmount(): Double? {
        return node.findChildByType(LcaTypes.NUMBER)?.psi?.text?.let { parseDouble(it) }
    }

    fun getUnit(): PsiUnit? {
        return node.findChildByType(LcaTypes.UNIT)?.psi as PsiUnit?
    }

    fun getVariable(): PsiVariable? {
        return node.findChildByType(LcaTypes.VARIABLE)?.psi as PsiVariable?
    }

    fun getQuantityInParen(): PsiQuantity? {
        return node.findChildByType(LcaTypes.QUANTITY)?.psi as PsiQuantity?
    }

}
