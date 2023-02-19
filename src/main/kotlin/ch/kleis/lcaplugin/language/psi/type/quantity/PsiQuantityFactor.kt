package ch.kleis.lcaplugin.language.psi.type.quantity

import ch.kleis.lcaplugin.psi.LcaTypes
import com.intellij.psi.PsiElement

interface PsiQuantityFactor : PsiElement {
    fun getPrimitive(): PsiQuantityPrimitive? {
        return node.findChildByType(LcaTypes.QUANTITY_PRIMITIVE)?.psi as PsiQuantityPrimitive?
    }

    fun getExponent(): Double? {
        return node.findChildByType(LcaTypes.NUMBER)?.psi?.text?.let { java.lang.Double.parseDouble(it) }
    }
}
